/*
 * Handles editing of user information
 * allows for changing of name,
 * billing address, payment card,
 * and password
 * Endpoints:
 * /api/edit/update-name
 * /api/edit/update-billing
 * /api/edit/update-payment
 * /api/edit/update-password
 * /api/edit/info
 * All endpoints are POST methods
 * 
 */

package com.bookstore.web;

import com.bookstore.db.UserDatabase;
import com.bookstore.records.UserRecords;
import com.bookstore.db.BillingAddressDatabase;
import com.bookstore.records.BillingAddressRecords;
import com.bookstore.db.PaymentCardDatabase;
import com.bookstore.records.PaymentCardRecords;
import com.bookstore.db.ShippingAddressDatabase;
import com.bookstore.records.ShippingAddressRecords;
import com.bookstore.SecUtils;
import com.bookstore.Email;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.stream.Collectors;

public class EditUserServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String pathInfo = request.getPathInfo();
        String requestBody = request.getReader().lines().collect(Collectors.joining());
        JsonObject jsonRequest = gson.fromJson(requestBody, JsonObject.class);
        JsonObject jsonResponse = new JsonObject();

        System.out.println("DEBUG: EditUserServlet received pathInfo: " + pathInfo);

        try {
            switch (pathInfo) {
                case "/update-name":
                    updateName(jsonRequest, jsonResponse);
                    break;
                case "/update-billing":
                    updateBilling(jsonRequest, jsonResponse);
                    break;
                case "/update-shipping":
                    updateShipping(jsonRequest, jsonResponse);
                    break;
                case "/update-payment":
                    updatePayment(jsonRequest, jsonResponse);
                    break;
                case "/update-password":
                    updatePassword(jsonRequest, jsonResponse);
                    break;
                case "/update-promotions":
                    updatePromotions(jsonRequest, jsonResponse);
                    break;
                case "/add-payment":
                    addPayment(jsonRequest, jsonResponse);
                    break;
                case "/delete-payment":
                    deletePayment(jsonRequest, jsonResponse);
                    break;
                case "/add-shipping":
                    addShipping(jsonRequest, jsonResponse);
                    break;
                default:
                    System.out.println("DEBUG: Unknown endpoint: " + pathInfo);
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    jsonResponse.addProperty("error", "Not Found: " + pathInfo);
                    break;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("error", "An unexpected error occurred: " + e.getMessage());
        }

        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JsonObject jsonResponse = new JsonObject();
        try {
            jakarta.servlet.http.HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userID") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                jsonResponse.addProperty("error", "Not authenticated");
                response.getWriter().write(jsonResponse.toString());
                return;
            }
            int userID = (int) session.getAttribute("userID");

            // Get user info
            UserDatabase userDb = new UserDatabase();
            BillingAddressDatabase billingDb = new BillingAddressDatabase();
            PaymentCardDatabase cardDb = new PaymentCardDatabase();
            com.bookstore.db.ShippingAddressDatabase shippingDb = new com.bookstore.db.ShippingAddressDatabase();
            userDb.connectDb();
            billingDb.connectDb();
            cardDb.connectDb();
            shippingDb.connectDb();
            cardDb.loadResults(); // <-- Load payment cards from DB
            UserRecords user = com.bookstore.SecUtils.findUserByID(userDb, userID);
            // Get all billing addresses referenced by user's payment cards (direct mapping, DB fetch)
            java.util.List<com.bookstore.records.BillingAddressRecords> billingAddresses = new java.util.ArrayList<>();
            java.util.Set<Integer> addedBillingAddressIDs = new java.util.HashSet<>();
            for (com.bookstore.records.PaymentCardRecords card : cardDb.getResults()) {
                if (card.getUserID() == userID) {
                    int billingAddressID = card.getBillingAddressID();
                    if (!addedBillingAddressIDs.contains(billingAddressID)) {
                        com.bookstore.records.BillingAddressRecords addr = billingDb.findByAddressID(billingAddressID);
                        if (addr != null) {
                            billingAddresses.add(addr);
                            addedBillingAddressIDs.add(billingAddressID);
                        }
                    }
                }
            }
            // Get all payment cards for user
            java.util.List<com.bookstore.records.PaymentCardRecords> paymentCards = new java.util.ArrayList<>();
            for (com.bookstore.records.PaymentCardRecords card : cardDb.getResults()) {
                if (card.getUserID() == userID) paymentCards.add(card);
            }
            userDb.disconnectDb();
            billingDb.disconnectDb();
            cardDb.disconnectDb();
            // Get shipping address
            com.bookstore.records.ShippingAddressRecords shippingAddress = shippingDb.findFirstByUserID(userID);
            shippingDb.disconnectDb();
            if (user == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("error", "User not found");
                response.getWriter().write(jsonResponse.toString());
                return;
            }
            JsonObject userJson = new JsonObject();
            userJson.addProperty("firstName", user.getFirstName());
            userJson.addProperty("lastName", user.getLastName());
            userJson.addProperty("email", user.getEmail());
            userJson.addProperty("userID", user.getUserID());
            // Add shipping address to response
            if (shippingAddress != null) {
                JsonObject shippingJson = new JsonObject();
                shippingJson.addProperty("addressID", shippingAddress.getAddressID());
                shippingJson.addProperty("street", shippingAddress.getStreet());
                shippingJson.addProperty("city", shippingAddress.getCity());
                shippingJson.addProperty("state", shippingAddress.getState());
                shippingJson.addProperty("zipCode", shippingAddress.getZipCode());
                userJson.add("shippingAddress", shippingJson);
            }
            // Billing addresses
            com.google.gson.JsonArray billingArr = new com.google.gson.JsonArray();
            for (com.bookstore.records.BillingAddressRecords addr : billingAddresses) {
                JsonObject addrJson = new JsonObject();
                addrJson.addProperty("addressID", addr.getAddressID());
                addrJson.addProperty("street", addr.getStreet());
                addrJson.addProperty("city", addr.getCity());
                addrJson.addProperty("state", addr.getState());
                addrJson.addProperty("zipCode", addr.getZipCode());
                billingArr.add(addrJson);
            }
            jsonResponse.add("billingAddresses", billingArr);
            // Payment cards
            com.google.gson.JsonArray cardArr = new com.google.gson.JsonArray();
            for (com.bookstore.records.PaymentCardRecords card : paymentCards) {
                JsonObject cardJson = new JsonObject();
                cardJson.addProperty("cardID", card.getCardID());
                cardJson.addProperty("cardType", card.getType()); // Use 'cardType' for frontend
                cardJson.addProperty("expirationDate", card.getExpirationDate());
                cardJson.addProperty("billingAddressID", card.getBillingAddressID());
                // Add masked card number for display
                try {
                    String decrypted = SecUtils.decryptCreditCardSimple(card.getCardNo());
                    String last4 = decrypted.length() > 4 ? decrypted.substring(decrypted.length() - 4) : decrypted;
                    String masked = "************" + last4;
                    cardJson.addProperty("cardNo", masked); // 12 asterisks + last 4 digits
                } catch (Exception e) {
                    cardJson.addProperty("cardNo", "************ERR");
                }
                cardArr.add(cardJson);
            }
            jsonResponse.add("user", userJson);
            jsonResponse.add("paymentCards", cardArr);
            jsonResponse.addProperty("passwordChangeRequiresCurrent", true);
            response.getWriter().write(jsonResponse.toString());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("error", "Failed to load user info: " + e.getMessage());
            response.getWriter().write(jsonResponse.toString());
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void updateName(JsonObject req, JsonObject res) {
        int userID = req.get("userID").getAsInt();
        String firstName = req.get("firstName").getAsString();
        String lastName = req.get("lastName").getAsString();
        
        UserDatabase db = new UserDatabase();
        db.connectDb();
        String result = updateUserName(db, userID, firstName, lastName);
        
        // Send email notification if update was successful
        if (result.contains("successfully")) {
            try {
                Email.sendProfileChangeNotification(userID);
            } catch (Exception e) {
                // Log error but don't fail the request
                System.err.println("Failed to send email notification: " + e.getMessage());
            }
        }
        
        db.disconnectDb();
        
        res.addProperty("message", result);
    }

    private void updateBilling(JsonObject req, JsonObject res) {
        int addressID = req.get("addressID").getAsInt();
        String street = req.get("street").getAsString();
        String city = req.get("city").getAsString();
        String state = req.get("state").getAsString();
        String zipCode = req.get("zipCode").getAsString();
        BillingAddressDatabase db = new BillingAddressDatabase();
        db.connectDb();
        String result = updateBillingAddress(db, addressID, street, city, state, zipCode);
        db.disconnectDb();
        res.addProperty("message", result);
    }

    private void updatePayment(JsonObject req, JsonObject res) {
        int userID = req.get("userID").getAsInt();
        int cardID = req.get("cardID").getAsInt();
        String cardNo = req.get("cardNo").getAsString();
        String type = req.get("type").getAsString();
        String expirationDate = req.get("expirationDate").getAsString();
        int billingAddressID = req.get("billingAddressID").getAsInt();

        PaymentCardDatabase db = new PaymentCardDatabase();
        db.connectDb();
        String result = updatePaymentCard(db, userID, cardID, cardNo, type, expirationDate, billingAddressID);
        
        // Send email notification if update was successful
        if (result.contains("successfully")) {
            try {
                Email.sendProfileChangeNotification(userID);
            } catch (Exception e) {
                // Log error but don't fail the request
                System.err.println("Failed to send email notification: " + e.getMessage());
            }
        }
        
        db.disconnectDb();

        res.addProperty("message", result);
    }

        /*
         * Updates password of user
         * checks current password against database
         * before allowing password change
         * and throws error if current password is incorrect
         */
    private void updatePassword(JsonObject req, JsonObject res) {
        int userID = req.get("userID").getAsInt();
        String currentPassword = req.get("currentPassword").getAsString();
        String newPassword = req.get("newPassword").getAsString();

        UserDatabase db = new UserDatabase();
        db.connectDb();
        String result = SecUtils.updatePassword(db, userID, currentPassword, newPassword);
        
        // Send email notification if update was successful
        if (result.contains("successfully")) {
            try {
                Email.sendProfileChangeNotification(userID);
            } catch (Exception e) {
                // Log error but don't fail the request
                System.err.println("Failed to send email notification: " + e.getMessage());
            }
        }
        
        db.disconnectDb();

        res.addProperty("message", result);
    }

    private void updatePromotions(JsonObject req, JsonObject res) {
        int userID = req.get("userID").getAsInt();
        boolean enrollForPromotions = req.get("enrollForPromotions").getAsBoolean();
        UserDatabase db = new UserDatabase();
        db.connectDb();
        String result;
        try {
            UserRecords user = com.bookstore.SecUtils.findUserByID(db, userID);
            if (user == null) {
                res.addProperty("error", "User not found");
                db.disconnectDb();
                return;
            }
            user.setEnrollForPromotions(enrollForPromotions);
            String updateResult = db.updateUser(user);
            if (updateResult.contains("Updated")) {
                result = enrollForPromotions ? "Enrolled in promotions successfully" : "Unenrolled from promotions successfully";
            } else {
                result = "Failed to update promotions: " + updateResult;
            }
        } catch (Exception e) {
            result = "Error updating promotions: " + e.getMessage();
        }
        db.disconnectDb();
        res.addProperty("message", result);
    }

    private void updateShipping(JsonObject req, JsonObject res) {
        int userID = req.get("userID").getAsInt();
        int addressID = req.get("addressID").getAsInt();
        String street = req.get("street").getAsString();
        String city = req.get("city").getAsString();
        String state = req.get("state").getAsString();
        String zipCode = req.get("zipCode").getAsString();

        System.out.println("DEBUG: updateShipping called with userID: " + userID + ", addressID: " + addressID);
        System.out.println("DEBUG: Address details - street: " + street + ", city: " + city + ", state: " + state + ", zipCode: " + zipCode);

        ShippingAddressDatabase db = new ShippingAddressDatabase();
        db.connectDb();
        String result = updateShippingAddress(db, userID, addressID, street, city, state, zipCode);
        db.disconnectDb();
        
        System.out.println("DEBUG: updateShipping result: " + result);
        res.addProperty("message", result);
    }

    /**
     * Adds a new payment card for a user, including billing address if needed
     */
    private void addPayment(JsonObject req, JsonObject res) {
        try {
            int userID = req.get("userID").getAsInt();
            String cardNo = req.get("cardNo").getAsString();
            String type = req.get("type").getAsString();
            String expirationDate = req.get("expirationDate").getAsString();
            // Extract billing address fields from billingAddress object if present
            String street = null, city = null, state = null, zipCode = null;
            if (req.has("billingAddress") && req.get("billingAddress").isJsonObject()) {
                JsonObject billingAddr = req.getAsJsonObject("billingAddress");
                street = billingAddr.has("street") ? billingAddr.get("street").getAsString() : null;
                city = billingAddr.has("city") ? billingAddr.get("city").getAsString() : null;
                state = billingAddr.has("state") ? billingAddr.get("state").getAsString() : null;
                zipCode = billingAddr.has("zipCode") ? billingAddr.get("zipCode").getAsString() : null;
            } else {
                street = req.has("street") ? req.get("street").getAsString() : null;
                city = req.has("city") ? req.get("city").getAsString() : null;
                state = req.has("state") ? req.get("state").getAsString() : null;
                zipCode = req.has("zipCode") ? req.get("zipCode").getAsString() : null;
            }
            Integer billingAddressID = req.has("billingAddressID") && !req.get("billingAddressID").isJsonNull() ? req.get("billingAddressID").getAsInt() : null;

            // Build paymentData JsonObject as expected by RegistrationServlet.addPaymentCard
            com.google.gson.JsonObject paymentData = new com.google.gson.JsonObject();
            paymentData.addProperty("cardNumber", cardNo);
            paymentData.addProperty("cardType", type);
            paymentData.addProperty("expirationDate", expirationDate);
            if (billingAddressID != null) {
                paymentData.addProperty("billingAddressID", billingAddressID);
            }
            com.google.gson.JsonObject billingData = new com.google.gson.JsonObject();
            if (street != null) billingData.addProperty("street", street);
            if (city != null) billingData.addProperty("city", city);
            if (state != null) billingData.addProperty("state", state);
            if (zipCode != null) billingData.addProperty("zipCode", zipCode);
            if (billingData.size() > 0) {
                paymentData.add("billingAddress", billingData);
            }

            BillingAddressDatabase billingDb = new BillingAddressDatabase();
            PaymentCardDatabase cardDb = new PaymentCardDatabase();
            String result = com.bookstore.web.RegistrationServlet.addPaymentCard(userID, paymentData, billingDb, cardDb);
            if (result == null || result.contains("Added")) {
                res.addProperty("message", "Card added successfully.");
            } else {
                res.addProperty("message", result);
            }
        } catch (Exception e) {
            res.addProperty("message", "Error adding card: " + e.getMessage());
        }
    }

    /**
     * Deletes a payment card by cardID
     */
    private void deletePayment(JsonObject req, JsonObject res) {
        if (!req.has("cardID")) {
            res.addProperty("message", "Missing cardID");
            return;
        }
        int cardID = req.get("cardID").getAsInt();
        PaymentCardDatabase db = new PaymentCardDatabase();
        db.connectDb();
        String result = db.deleteCard(cardID);
        db.disconnectDb();
        res.addProperty("message", result);
    }

    private void addShipping(JsonObject req, JsonObject res) {
        try {
            int userID = req.get("userID").getAsInt();
            String street = req.get("street").getAsString();
            String city = req.get("city").getAsString();
            String state = req.get("state").getAsString();
            String zipCode = req.get("zipCode").getAsString();

            ShippingAddressDatabase db = new ShippingAddressDatabase();
            db.connectDb();
            int newAddressID = db.insertAddress(userID, street, city, state, zipCode);
            db.disconnectDb();

            if (newAddressID > 0) {
                res.addProperty("message", "Shipping address added successfully.");
                res.addProperty("addressID", newAddressID);
            } else {
                res.addProperty("message", "Failed to add shipping address.");
            }
        } catch (Exception e) {
            res.addProperty("message", "Error adding shipping address: " + e.getMessage());
        }
    }

    /*
     * Update username of user
     */
    public static String updateUserName(UserDatabase db, int userID, String firstName, String lastName) {
        if (firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty()) {
            return "First and last names cannot be empty";
        }
        
        try {
            UserRecords user = SecUtils.findUserByID(db, userID);
            if (user == null) {
                return "User not found";
            }
            
            user.setFirstName(firstName);
            user.setLastName(lastName);
            String result = db.updateUser(user);
            
            if (result.contains("Updated")) {
                return "Name updated successfully";
            } else {
                return "Failed to update name: " + result;
            }
            
        } catch (Exception e) {
            return "Error updating name: " + e.getMessage();
        }
    }

    /*
     * Updates payment card of user 
     * Does not check if card is actually valid
     * but does check if card number is 16 digits
     * making a fair amount of assumptions about entries currently
     */
    public static String updatePaymentCard(PaymentCardDatabase db, int userID, int cardID, String cardNo, String type, String expirationDate, int billingAddressID) {
        if (cardNo.length() != 16 || cardNo == null || cardNo.trim().isEmpty() || type == null || type.trim().isEmpty() || expirationDate == null || expirationDate.trim().isEmpty()) {
            return "Card number, type, and expiration date cannot be empty, or card number is not 16 digits";
        }
        
        try {
            PaymentCardRecords card = new PaymentCardRecords(cardID, cardNo, userID, type, expirationDate, billingAddressID);
            String result = db.updateCard(card);
            
            if (result.contains("Updated")) {
                return "Payment card updated successfully";
            } else {
                return "Failed to update payment card: " + result;
            }
            
        } catch (Exception e) {
            return "Error updating payment card: " + e.getMessage();
        }
    }

    /*
     * Updates billing address of user
     * does not actually check if this address is valid
     * because I don't think it is required
     * if it is, can be added
     */
    public static String updateBillingAddress(BillingAddressDatabase db, int addressID, String street, String city, String state, String zipCode) {
        if (street == null || street.trim().isEmpty() || city == null || city.trim().isEmpty() || state == null || state.trim().isEmpty() || zipCode == null || zipCode.trim().isEmpty()) {
            return "Street, city, state, and zip code cannot be empty";
        }
        
        try {
            BillingAddressRecords address = new BillingAddressRecords(addressID, street, city, state, zipCode);
            String result = db.updateBillingAddress(address);
            
            if (result.contains("Updated")) {
                return "Billing address updated successfully";
            } else {
                return "Failed to update billing address: " + result;
            }
            
        } catch (Exception e) {
            return "Error updating billing address: " + e.getMessage();
        }
    }

    private static String updateShippingAddress(ShippingAddressDatabase db, int userID, int addressID, String street, String city, String state, String zipCode) {
        if (street == null || street.trim().isEmpty() || city == null || city.trim().isEmpty() || state == null || state.trim().isEmpty() || zipCode == null || zipCode.trim().isEmpty()) {
            return "Street, city, state, and zip code cannot be empty";
        }

        try {
            ShippingAddressRecords shippingAddress = new ShippingAddressRecords(addressID, userID, street, city, state, zipCode);
            String result = db.updateAddress(shippingAddress);

            if (result.contains("Updated")) {
                return "Shipping address updated successfully";
            } else {
                return "Failed to update shipping address: " + result;
            }

        } catch (Exception e) {
            return "Error updating shipping address: " + e.getMessage();
        }
    }
} 