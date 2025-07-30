/*
 * Handles user registration and account creation.
 * Currently only missing email verification, forgot/rest password
 * can be handled by methods in other classes so might be a good idea
 * to move them to here for cohesion
 * /api/register/ endpoint
 * Post Method
 */

package com.bookstore.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.bookstore.db.*;
import com.bookstore.records.*;
import com.bookstore.SecUtils;
import com.bookstore.Email;
import java.sql.Timestamp;
import java.util.UUID;

public class RegistrationServlet extends HttpServlet {
    private UserDatabase userDB = new UserDatabase();
    private ShippingAddressDatabase shippingDB = new ShippingAddressDatabase();
    private BillingAddressDatabase billingDB = new BillingAddressDatabase();
    private PaymentCardDatabase paymentDB = new PaymentCardDatabase();
    private VerificationTokenDatabase tokenDB = new VerificationTokenDatabase();
    private Gson gson = new Gson();

    // Email validation pattern
    private static final Pattern emailPattern = Pattern.compile(
    "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$" //regex for email validation
);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Parse request body
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            JsonObject requestData = gson.fromJson(sb.toString(), JsonObject.class);
            
            // Extract required fields
            String firstName = getStringField(requestData, "firstName");
            String lastName = getStringField(requestData, "lastName");
            String email = getStringField(requestData, "email");
            String password = getStringField(requestData, "password");
            String phone = getStringField(requestData, "phone");
            
            // Validate required fields
            String validationError = validateRequiredFields(firstName, lastName, email, password, phone);
            if (validationError != null) {
                sendErrorResponse(response, 400, validationError);
                return;
            }
            
            // Check if email already exists
            if (isEmailTaken(email)) {
                sendErrorResponse(response, 409, "Email address is already registered");
                return;
            }
            
            // Hash password
            String hashedPassword = SecUtils.hashPassword(password);
            
            // Create user record
            UserRecords newUser = new UserRecords(
                0, // userID will be auto-generated
                firstName,
                lastName,
                email.toLowerCase(),
                hashedPassword,
                phone,
                "inactive", // Set status to inactive until email verification
                requestData.has("enrollForPromotions") ? requestData.get("enrollForPromotions").getAsBoolean() : false,
                2 // Default to customer type
            );
            
            // Connect to database and insert user
            if (!userDB.connectDb()) {
                sendErrorResponse(response, 500, "Failed to connect to database");
                return;
            }
            
            String userResult = userDB.addUser(newUser);
            userDB.disconnectDb();
            
            if (!userResult.contains("Added")) {
                sendErrorResponse(response, 500, "Failed to create user account: " + userResult);
                return;
            }
            
            // Get the newly created user ID
            if (!userDB.connectDb()) {
                sendErrorResponse(response, 500, "Failed to reconnect to database to retrieve user ID");
                return;
            }
            
            System.out.println("DEBUG: Looking for user with email: " + email);
            UserRecords createdUser = userDB.findUserByEmail(email);
            System.out.println("DEBUG: Found user: " + (createdUser != null ? "Yes (ID: " + createdUser.getUserID() + ")" : "No"));
            userDB.disconnectDb();
            
            if (createdUser == null) {
                sendErrorResponse(response, 500, "User created but could not retrieve user ID");
                return;
            }
            
            int userId = createdUser.getUserID();
            
            // Handle optional shipping address
            if (requestData.has("shippingAddress")) {
                JsonObject shippingData = requestData.getAsJsonObject("shippingAddress");
                String shippingResult = addShippingAddress(userId, shippingData);
                if (shippingResult != null) {
                    sendErrorResponse(response, 500, "User created but shipping address failed: " + shippingResult);
                    return;
                }
            }
            
            // Handle optional payment information
            if (requestData.has("paymentCard")) {
                JsonObject paymentData = requestData.getAsJsonObject("paymentCard");
                String paymentResult = addPaymentCard(userId, paymentData, billingDB, paymentDB);
                if (paymentResult != null) {
                    sendErrorResponse(response, 500, "User created but payment card failed: " + paymentResult);
                    return;
                }
            }
            
            // Generate and send verification email
            String verificationResult = generateAndSendVerificationEmail(userId, email, firstName);
            if (verificationResult != null) {
                // Log the error but don't fail registration
                System.err.println("Warning: Failed to send verification email: " + verificationResult);
            }
            
            // Success response
            JsonObject successResponse = new JsonObject();
            successResponse.addProperty("success", true);
            successResponse.addProperty("message", "Registration successful. Please check your email to verify your account.");
            successResponse.addProperty("userID", userId);
            successResponse.addProperty("email", email);
            
            response.setStatus(201);
            response.getWriter().write(gson.toJson(successResponse));
            
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    public static String getStringField(JsonObject json, String fieldName) {
        if (json.has(fieldName) && !json.get(fieldName).isJsonNull()) {
            return json.get(fieldName).getAsString().trim();
        }
        return null;
    }
    
    private String validateRequiredFields(String firstName, String lastName, String email, String password, String phone) {
        if (firstName == null || firstName.isEmpty()) {
            return "First name is required";
        }
        if (lastName == null || lastName.isEmpty()) {
            return "Last name is required";
        }
        if (email == null || email.isEmpty()) {
            return "Email address is required";
        }
        if (!emailPattern.matcher(email).matches()) {
            return "Invalid email address format";
        }
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        // pass length check, not neccessarily needed but just good practice
        if (password.length() < 6) {
            return "Password must be at least 6 characters long";
        }
        if (phone == null || phone.isEmpty()) {
            return "Phone number is required";
        }
        return null; // All validations passed
    }
    
    private boolean isEmailTaken(String email) {
        UserRecords existingUser = SecUtils.findUserForLogin(userDB, email, false);
        return existingUser != null;
    }
    
    private String addShippingAddress(int userId, JsonObject shippingData) {
        try {
            String street = getStringField(shippingData, "street");
            String city = getStringField(shippingData, "city");
            String state = getStringField(shippingData, "state");
            String zipCode = getStringField(shippingData, "zipCode");
            
            if (street == null || city == null || state == null || zipCode == null) {
                return "All shipping address fields are required: street, city, state, zipCode";
            }
            
            ShippingAddressRecords shippingAddress = new ShippingAddressRecords(
                0, // addressID will be auto-generated
                userId,
                street,
                city,
                state,
                zipCode
            );
            
            if (!shippingDB.connectDb()) {
                return "Failed to connect to shipping database";
            }
            
            String result = shippingDB.addAddress(shippingAddress);
            shippingDB.disconnectDb();
            
            if (!result.contains("Added")) {
                return result;
            }
            
            return null; // Success
        } catch (Exception e) {
            return "Error adding shipping address: " + e.getMessage();
        }
    }
    
    public static String addPaymentCard(int userId, JsonObject paymentData, BillingAddressDatabase billingDB, PaymentCardDatabase paymentDB) {
        try {
            String cardNumber = getStringField(paymentData, "cardNumber");
            String cardType = getStringField(paymentData, "cardType");
            String expirationDate = getStringField(paymentData, "expirationDate");
            if (cardNumber == null || cardType == null || expirationDate == null) {
                return "All payment card fields are required: cardNumber, cardType, expirationDate";
            }
            // Billing address is required for payment cards
            if (!paymentData.has("billingAddress")) {
                return "Billing address is required for payment cards";
            }
            JsonObject billingData = paymentData.getAsJsonObject("billingAddress");
            String street = getStringField(billingData, "street");
            String city = getStringField(billingData, "city");
            String state = getStringField(billingData, "state");
            String zipCode = getStringField(billingData, "zipCode");
            if (street == null || city == null || state == null || zipCode == null) {
                return "All billing address fields are required: street, city, state, zipCode";
            }
            // Insert billing address first
            BillingAddressRecords billingAddress = new BillingAddressRecords(
                0, // addressID will be auto-generated
                street,
                city,
                state,
                zipCode
            );
            if (!billingDB.connectDb()) {
                return "Failed to connect to billing database";
            }
            String billingResult = billingDB.addAddress(billingAddress);
            if (!billingResult.contains("Added")) {
                billingDB.disconnectDb();
                return "Failed to add billing address: " + billingResult;
            }
            // Get the billing address ID by matching all fields
            billingDB.loadResults();
            BillingAddressRecords createdBilling = null;
            for (BillingAddressRecords addr : billingDB.getResults()) {
                if (addr.getStreet().equals(street) && addr.getCity().equals(city) && addr.getState().equals(state) && addr.getZipCode().equals(zipCode)) {
                    createdBilling = addr;
                    break;
                }
            }
            billingDB.disconnectDb();
            if (createdBilling == null) {
                return "Billing address created but could not retrieve ID";
            }
            // Encrypt card number with simple encryption (no external keys needed)
            String encryptedCardNumber;
            try {
                System.out.println("DEBUG: Attempting to encrypt card number for user " + userId);
                encryptedCardNumber = SecUtils.encryptCreditCardSimple(cardNumber);
                System.out.println("DEBUG: Successfully encrypted card number. Length: " + encryptedCardNumber.length());
            } catch (Exception e) {
                System.err.println("ERROR: Failed to encrypt card number: " + e.getMessage());
                e.printStackTrace();
                return "Failed to encrypt payment card: " + e.getMessage();
            }
            // Insert payment card
            PaymentCardRecords paymentCard = new PaymentCardRecords(
                0, // cardID will be auto-generated
                cardNumber, // <-- plain card number, not encrypted
                userId,
                cardType,
                expirationDate,
                createdBilling.getAddressID()
            );
            if (!paymentDB.connectDb()) {
                return "Failed to connect to payment database";
            }
            String paymentResult = paymentDB.addCard(paymentCard);
            paymentDB.disconnectDb();
            if (!paymentResult.contains("Added")) {
                return "Failed to add payment card: " + paymentResult;
            }
            return null; // Success
        } catch (Exception e) {
            return "Error adding payment card: " + e.getMessage();
        }
    }
    
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        JsonObject errorResponse = new JsonObject();
        errorResponse.addProperty("success", false);
        errorResponse.addProperty("error", message);
        
        response.setStatus(statusCode);
        response.getWriter().write(gson.toJson(errorResponse));
    }
    
    /**
     * Generates a verification token and sends verification email
     * tokens are temporary
     * and stored in database
     */
    private String generateAndSendVerificationEmail(int userId, String userEmail, String userName) {
        try {
            // generate token and set expiration to 24 hours
            String verificationToken = UUID.randomUUID().toString();
            Timestamp expiryDate = new Timestamp(System.currentTimeMillis() + (24 * 60 * 60 * 1000));
            VerificationTokenRecords tokenRecord = new VerificationTokenRecords(
                0, 
                userId,
                verificationToken,
                expiryDate,
                "email_verification"
            );
            
            if (!tokenDB.connectDb()) {
                return "Failed to connect to token database";
            }
            
            // Add token to database
            String tokenResult = tokenDB.addToken(tokenRecord);
            if (!tokenResult.contains("Added")) {
                tokenDB.disconnectDb();
                return "Failed to create verification token: " + tokenResult;
            }
            
            tokenDB.disconnectDb();
            
            // Retrieve the user to get the loginUserID (which was auto-assigned by the database trigger)
            if (!userDB.connectDb()) {
                return "Failed to connect to user database";
            }
            
            UserRecords createdUser = userDB.findUserByEmail(userEmail);
            userDB.disconnectDb();
            
            if (createdUser == null) {
                return "Failed to retrieve created user";
            }
            
            // url for verification so that clicking actually verifies
            String baseUrl = "http://localhost:8080"; 
            
            boolean emailSent = Email.sendVerificationEmail(userEmail, userName, verificationToken, baseUrl, createdUser.getLoginUserID());
            if (!emailSent) {
                return "Failed to send verification email";
            }
            
            return null; // worked
            
        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating verification email: " + e.getMessage();
        }
    }
} 