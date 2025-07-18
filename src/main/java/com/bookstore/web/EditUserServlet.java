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
 * 
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
import com.bookstore.SecUtils;

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
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String pathInfo = request.getPathInfo();
        String requestBody = request.getReader().lines().collect(Collectors.joining());
        JsonObject jsonRequest = gson.fromJson(requestBody, JsonObject.class);
        JsonObject jsonResponse = new JsonObject();

        try {
            switch (pathInfo) {
                case "/update-name":
                    updateName(jsonRequest, jsonResponse);
                    break;
                case "/update-billing":
                    updateBilling(jsonRequest, jsonResponse);
                    break;
                case "/update-payment":
                    updatePayment(jsonRequest, jsonResponse);
                    break;
                case "/update-password":
                    updatePassword(jsonRequest, jsonResponse);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    jsonResponse.addProperty("error", "Not Found");
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
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
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
        db.disconnectDb();
        
        res.addProperty("message", result);
    }

    private void updateBilling(JsonObject req, JsonObject res) {
        int userID = req.get("userID").getAsInt();
        int addressID = req.get("addressID").getAsInt();
        String street = req.get("street").getAsString();
        String city = req.get("city").getAsString();
        String state = req.get("state").getAsString();
        String zipCode = req.get("zipCode").getAsString();
        
        BillingAddressDatabase db = new BillingAddressDatabase();
        db.connectDb();
        String result = updateBillingAddress(db, userID, addressID, street, city, state, zipCode);
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
        db.disconnectDb();

        res.addProperty("message", result);
    }

        /*
         * Updates password of user
         * checks current password against databse
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
        db.disconnectDb();

        res.addProperty("message", result);
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
    public static String updateBillingAddress(BillingAddressDatabase db, int userID, int addressID, String street, String city, String state, String zipCode) {
        if (street == null || street.trim().isEmpty() || city == null || city.trim().isEmpty() || state == null || state.trim().isEmpty() || zipCode == null || zipCode.trim().isEmpty()) {
            return "Street, city, state, and zip code cannot be empty";
        }
        
        try {
            BillingAddressRecords address = new BillingAddressRecords(addressID, userID, street, city, state, zipCode);
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
} 