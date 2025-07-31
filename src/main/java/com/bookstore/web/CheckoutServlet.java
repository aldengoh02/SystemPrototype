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
            PaymentCardDatabase cardDb = new PaymentCardDatabase();
            BillingAddressDatabase billingDb = new BillingAddressDatabase();
            ShippingAddressDatabase shippingDb = new ShippingAddressDatabase();
            UserDatabase userDb = new UserDatabase();
            
            if (!cardDb.connectDb() || !billingDb.connectDb() || !shippingDb.connectDb() || !userDb.connectDb()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Database connection failed\"}");
                return;
            }

            ArrayList<PaymentCardRecords> paymentCards = cardDb.getCardsByUserID(userId);
            ArrayList<ShippingAddressRecords> shippingAddresses = shippingDb.getAddressesByUserID(userId);
            ArrayList<BillingAddressRecords> allBillingAddresses = billingDb.getAllAddresses();
            
            JsonObject responseObj = new JsonObject();
            JsonArray paymentCardsArray = new JsonArray();
            JsonArray shippingAddressesArray = new JsonArray();
            JsonArray billingAddressesArray = new JsonArray();
            
            for (PaymentCardRecords card : paymentCards) {
                JsonObject cardObj = new JsonObject();
                cardObj.addProperty("cardID", card.getCardID());
                String maskedCardNo = "**** **** **** " + card.getCardNo().substring(Math.max(0, card.getCardNo().length() - 4));
                cardObj.addProperty("maskedCardNo", maskedCardNo);
                cardObj.addProperty("type", card.getType());
                cardObj.addProperty("expirationDate", card.getExpirationDate());
                cardObj.addProperty("billingAddressID", card.getBillingAddressID());
                paymentCardsArray.add(cardObj);
            }
            
            for (ShippingAddressRecords address : shippingAddresses) {
                JsonObject addrObj = new JsonObject();
                addrObj.addProperty("addressID", address.getAddressID());
                addrObj.addProperty("street", address.getStreet());
                addrObj.addProperty("city", address.getCity());
                addrObj.addProperty("state", address.getState());
                addrObj.addProperty("zipCode", address.getZipCode());
                shippingAddressesArray.add(addrObj);
            }
            
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
            String body = request.getReader().lines().collect(Collectors.joining("\n"));
            JsonObject requestData = gson.fromJson(body, JsonObject.class);

            JsonArray cartItems = requestData.getAsJsonArray("cartItems");
            JsonObject paymentInfo = requestData.getAsJsonObject("paymentInfo");
            JsonObject billingAddress = requestData.getAsJsonObject("billingAddress");
            JsonObject shippingAddress = requestData.has("shippingAddress") ? requestData.getAsJsonObject("shippingAddress") : null;
            double totalAmount = requestData.get("totalAmount").getAsDouble();

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

            StringBuilder orderDetails = new StringBuilder();
            for (JsonElement item : cartItems) {
                JsonObject itemObj = item.getAsJsonObject();
                orderDetails.append("- ").append(itemObj.get("title").getAsString())
                           .append(" x ").append(itemObj.get("quantity").getAsInt())
                           .append(" - $").append(String.format("%.2f", itemObj.get("sellingPrice").getAsDouble() * itemObj.get("quantity").getAsInt()))
                           .append("\n");
            }

            // ----------- FIXED payment info parsing -----------
            String paymentInfoStr = "";
            String billingAddressStr = "";

            if (paymentInfo.has("cardID")) {
                String maskedCardNo = paymentInfo.has("maskedCardNo")
                    ? paymentInfo.get("maskedCardNo").getAsString()
                    : "**** **** **** ";
                String type = paymentInfo.has("type") ? paymentInfo.get("type").getAsString() : "";
                String expirationDate = paymentInfo.has("expirationDate") ? paymentInfo.get("expirationDate").getAsString() : "";

                paymentInfoStr = "Card: " + maskedCardNo +
                                 "\nType: " + type +
                                 "\nExpiry: " + expirationDate;

                if (billingAddress != null) {
                    String billingStreet = billingAddress.get("street").getAsString();
                    String billingCity = billingAddress.get("city").getAsString();
                    String billingState = billingAddress.get("state").getAsString();
                    String billingZipCode = billingAddress.get("zipCode").getAsString();
                    billingAddressStr = billingStreet + "\n" +
                                        billingCity + ", " +
                                        billingState + " " +
                                        billingZipCode;
                }
            } else if (paymentInfo.has("cardNumber")) {
                String cardNo = paymentInfo.get("cardNumber").getAsString().replaceAll("\\s", "");
                String maskedCardNo = "**** **** **** " + cardNo.substring(Math.max(0, cardNo.length() - 4));
                String cardType = paymentInfo.get("cardType").getAsString();
                String expirationDate = paymentInfo.get("expirationDate").getAsString();
                paymentInfoStr = "Card: " + maskedCardNo +
                                 "\nType: " + cardType +
                                 "\nExpiry: " + expirationDate;

                if (billingAddress != null) {
                    String billingStreet = billingAddress.get("street").getAsString();
                    String billingCity = billingAddress.get("city").getAsString();
                    String billingState = billingAddress.get("state").getAsString();
                    String billingZipCode = billingAddress.get("zipCode").getAsString();
                    billingAddressStr = billingStreet + "\n" +
                                        billingCity + ", " +
                                        billingState + " " +
                                        billingZipCode;
                }
            } else {
                paymentInfoStr = "Unknown payment info format";
                billingAddressStr = "";
            }

            String shippingAddressStr = "";
            if (shippingAddress != null) {
                String shippingStreet = shippingAddress.get("street").getAsString();
                String shippingCity = shippingAddress.get("city").getAsString();
                String shippingState = shippingAddress.get("state").getAsString();
                String shippingZipCode = shippingAddress.get("zipCode").getAsString();
                shippingAddressStr = shippingStreet + "\n" +
                                      shippingCity + ", " +
                                      shippingState + " " +
                                      shippingZipCode;
            }

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