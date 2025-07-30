/**
 * REST endpoint for checkout operations
 * 
 * Base URL: http://localhost:8080/api/checkout
 * 
 * Endpoints:
 *  
 *  GET /api/checkout/user-data
 *    Returns: User's stored payment cards, billing addresses, and shipping addresses
 *    Requires: Active session
 * 
 *  POST /api/checkout/process
 *    Body: {cartItems: array, paymentInfo: object, billingAddress: object, shippingAddress: object}
 *    Action: Processes checkout and sends confirmation email
 *    Requires: Active session
 */

package com.bookstore.web;

import com.bookstore.db.*;
import com.bookstore.records.*;
import com.bookstore.Email;
import com.bookstore.SecUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;

public class CheckoutServlet extends HttpServlet {
    private final Gson gson = new Gson();

    private void setupResponse(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        setupResponse(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private Integer getUserIdFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        return (Integer) session.getAttribute("userID");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        setupResponse(response);
        PrintWriter out = response.getWriter();
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || !pathInfo.equals("/user-data")) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\": \"Endpoint not found\"}");
            return;
        }
        
        Integer userId = getUserIdFromSession(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"User not authenticated\"}");
            return;
        }

        try {
            // Get user's stored payment cards, billing addresses, and shipping addresses
            PaymentCardDatabase cardDb = new PaymentCardDatabase();
            BillingAddressDatabase billingDb = new BillingAddressDatabase();
            ShippingAddressDatabase shippingDb = new ShippingAddressDatabase();
            UserDatabase userDb = new UserDatabase();
            
            if (!cardDb.connectDb() || !billingDb.connectDb() || !shippingDb.connectDb() || !userDb.connectDb()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Database connection failed\"}");
                return;
            }

            // Get user's payment cards
            ArrayList<PaymentCardRecords> paymentCards = cardDb.getCardsByUserID(userId);
            
            // Get user's shipping addresses
            ArrayList<ShippingAddressRecords> shippingAddresses = shippingDb.getAddressesByUserID(userId);
            
            // Get all billing addresses (cards reference billing addresses by ID)
            ArrayList<BillingAddressRecords> allBillingAddresses = billingDb.getAllAddresses();
            
            // Create response object
            JsonObject responseObj = new JsonObject();
            JsonArray paymentCardsArray = new JsonArray();
            JsonArray shippingAddressesArray = new JsonArray();
            JsonArray billingAddressesArray = new JsonArray();
            
            // Add payment cards with masked card numbers
            for (PaymentCardRecords card : paymentCards) {
                JsonObject cardObj = new JsonObject();
                cardObj.addProperty("cardID", card.getCardID());
                // Mask the card number for security (show only last 4 digits)
                String maskedCardNo = "**** **** **** " + card.getCardNo().substring(Math.max(0, card.getCardNo().length() - 4));
                cardObj.addProperty("maskedCardNo", maskedCardNo);
                cardObj.addProperty("type", card.getType());
                cardObj.addProperty("expirationDate", card.getExpirationDate());
                cardObj.addProperty("billingAddressID", card.getBillingAddressID());
                paymentCardsArray.add(cardObj);
            }
            
            // Add shipping addresses
            for (ShippingAddressRecords address : shippingAddresses) {
                JsonObject addrObj = new JsonObject();
                addrObj.addProperty("addressID", address.getAddressID());
                addrObj.addProperty("street", address.getStreet());
                addrObj.addProperty("city", address.getCity());
                addrObj.addProperty("state", address.getState());
                addrObj.addProperty("zipCode", address.getZipCode());
                shippingAddressesArray.add(addrObj);
            }
            
            // Add billing addresses
            for (BillingAddressRecords address : allBillingAddresses) {
                JsonObject addrObj = new JsonObject();
                addrObj.addProperty("addressID", address.getAddressID());
                addrObj.addProperty("street", address.getStreet());
                addrObj.addProperty("city", address.getCity());
                addrObj.addProperty("state", address.getState());
                addrObj.addProperty("zipCode", address.getZipCode());
                billingAddressesArray.add(addrObj);
            }
            
            responseObj.add("paymentCards", paymentCardsArray);
            responseObj.add("shippingAddresses", shippingAddressesArray);
            responseObj.add("billingAddresses", billingAddressesArray);
            
            out.print(gson.toJson(responseObj));
            
            cardDb.disconnectDb();
            billingDb.disconnectDb();
            shippingDb.disconnectDb();
            userDb.disconnectDb();
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to retrieve user data: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        setupResponse(response);
        PrintWriter out = response.getWriter();
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || !pathInfo.equals("/process")) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\": \"Endpoint not found\"}");
            return;
        }
        
        Integer userId = getUserIdFromSession(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"User not authenticated\"}");
            return;
        }

        try {
            // Read request body
            String body = request.getReader().lines().collect(Collectors.joining("\n"));
            JsonObject requestData = gson.fromJson(body, JsonObject.class);
            
            JsonArray cartItems = requestData.getAsJsonArray("cartItems");
            JsonObject paymentInfo = requestData.getAsJsonObject("paymentInfo");
            JsonObject billingAddress = requestData.getAsJsonObject("billingAddress");
            JsonObject shippingAddress = requestData.getAsJsonObject("shippingAddress");
            double totalAmount = requestData.get("totalAmount").getAsDouble();
            
            // Get user information for email
            UserDatabase userDb = new UserDatabase();
            if (!userDb.connectDb()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Database connection failed\"}");
                return;
            }
            
            UserRecords user = SecUtils.findUserByID(userDb, userId);
            if (user == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"User not found\"}");
                userDb.disconnectDb();
                return;
            }
            
            // Format order details for email
            StringBuilder orderDetails = new StringBuilder();
            for (JsonElement item : cartItems) {
                JsonObject itemObj = item.getAsJsonObject();
                orderDetails.append("- ").append(itemObj.get("title").getAsString())
                           .append(" x ").append(itemObj.get("quantity").getAsInt())
                           .append(" - $").append(String.format("%.2f", itemObj.get("sellingPrice").getAsDouble() * itemObj.get("quantity").getAsInt()))
                           .append("\n");
            }
            
            // Format payment info for email (mask card number)
            String paymentInfoStr;
            if (paymentInfo.has("maskedCardNo")) {
                paymentInfoStr = "Card: " + paymentInfo.get("maskedCardNo").getAsString() + 
                               "\nType: " + paymentInfo.get("type").getAsString() +
                               "\nExpiry: " + paymentInfo.get("expirationDate").getAsString();
            } else {
                // Manual entry - mask the card number
                String cardNo = paymentInfo.get("cardNumber").getAsString().replaceAll("\\s", "");
                String maskedCardNo = "**** **** **** " + cardNo.substring(Math.max(0, cardNo.length() - 4));
                paymentInfoStr = "Card: " + maskedCardNo + 
                               "\nCardholder: " + paymentInfo.get("cardholderName").getAsString() +
                               "\nExpiry: " + paymentInfo.get("expiryDate").getAsString();
            }
            
            // Format billing address for email
            String billingAddressStr = billingAddress.get("street").getAsString() + "\n" +
                                     billingAddress.get("city").getAsString() + ", " +
                                     billingAddress.get("state").getAsString() + " " +
                                     billingAddress.get("zipCode").getAsString();
            
            // Format shipping address for email
            String shippingAddressStr = shippingAddress.get("street").getAsString() + "\n" +
                                      shippingAddress.get("city").getAsString() + ", " +
                                      shippingAddress.get("state").getAsString() + " " +
                                      shippingAddress.get("zipCode").getAsString();
            
            // Send confirmation email
            boolean emailSent = Email.sendOrderConfirmationEmail(
                user.getEmail(),
                user.getFirstName(),
                orderDetails.toString(),
                paymentInfoStr,
                billingAddressStr,
                shippingAddressStr,
                totalAmount
            );
            
            JsonObject responseObj = new JsonObject();
            responseObj.addProperty("success", true);
            responseObj.addProperty("emailSent", emailSent);
            responseObj.addProperty("message", "Order processed successfully");
            
            out.print(gson.toJson(responseObj));
            
            userDb.disconnectDb();
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to process checkout: " + e.getMessage() + "\"}");
        }
    }
}