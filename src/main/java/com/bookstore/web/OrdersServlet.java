/**
 * REST endpoint for order operations
 * 
 * Base URL: http://localhost:8080/api/orders
 * 
 * Endpoints:
 *  
 *  GET /api/orders
 *    Returns: Array of user's orders with order details
 *    Requires: Active session
 * 
 *  POST /api/orders
 *    Body: {cartItems: array, totalAmount: number, appliedPromo: object}
 *    Action: Saves order to database
 *    Requires: Active session
 */

package com.bookstore.web;

import com.bookstore.db.*;
import com.bookstore.records.*;
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
import java.sql.Timestamp;

public class OrdersServlet extends HttpServlet {
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
        
        Integer userId = getUserIdFromSession(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"User not authenticated\"}");
            return;
        }

        try {
            // Get user's orders from database
            OrdersDatabase ordersDb = new OrdersDatabase();
            UserDatabase userDb = new UserDatabase();
            BookDatabase bookDb = new BookDatabase();
            TransactionDatabase transactionDb = new TransactionDatabase();
            
            if (!ordersDb.connectDb() || !userDb.connectDb() || !bookDb.connectDb() || !transactionDb.connectDb()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Database connection failed\"}");
                return;
            }

            // Load all orders and filter by user ID
            ordersDb.loadResults();
            ArrayList<OrdersRecords> allOrders = ordersDb.getResults();
            
            JsonArray ordersArray = new JsonArray();
            
            for (OrdersRecords order : allOrders) {
                if (order.getUserID() == userId) {
                    JsonObject orderObj = new JsonObject();
                    orderObj.addProperty("id", order.getOrderID());
                    orderObj.addProperty("total", order.getGrandTotal());
                    orderObj.addProperty("date", order.getOrderDateTime().toString());
                    orderObj.addProperty("status", "Processed"); // Default status
                    
                    // Get transaction items for this order
                    transactionDb.loadResults();
                    ArrayList<TransactionRecords> allTransactions = transactionDb.getResults();
                    JsonArray itemsArray = new JsonArray();
                    
                    for (TransactionRecords transaction : allTransactions) {
                        if (transaction.getOrderID() == order.getOrderID()) {
                            // Get book details
                            BookRecords book = BookActions.getBookById(bookDb.getConnection(), transaction.getBookID());
                            if (book != null) {
                                JsonObject itemObj = new JsonObject();
                                itemObj.addProperty("id", book.getId());
                                itemObj.addProperty("title", book.getTitle());
                                itemObj.addProperty("quantity", transaction.getQuantity());
                                itemObj.addProperty("price", book.getSellingPrice());
                                itemsArray.add(itemObj);
                            }
                        }
                    }
                    
                    orderObj.add("items", itemsArray);
                    ordersArray.add(orderObj);
                }
            }
            
            out.print(gson.toJson(ordersArray));
            
            ordersDb.disconnectDb();
            userDb.disconnectDb();
            bookDb.disconnectDb();
            transactionDb.disconnectDb();
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to retrieve orders: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        setupResponse(response);
        PrintWriter out = response.getWriter();
        
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
            double totalAmount = requestData.get("totalAmount").getAsDouble();
            JsonObject appliedPromo = requestData.has("appliedPromo") && !requestData.get("appliedPromo").isJsonNull() 
                                     ? requestData.getAsJsonObject("appliedPromo") : null;
            
            // Connect to databases
            OrdersDatabase ordersDb = new OrdersDatabase();
            TransactionDatabase transactionDb = new TransactionDatabase();
            
            if (!ordersDb.connectDb() || !transactionDb.connectDb()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Database connection failed\"}");
                return;
            }
            
            // Create order record
            Integer promoId = null;
            if (appliedPromo != null && appliedPromo.has("id")) {
                promoId = appliedPromo.get("id").getAsInt();
            }
            
            OrdersRecords order = new OrdersRecords(
                0, // orderID (auto-generated)
                userId,
                1, // Default cardID - this should ideally come from payment info
                promoId,
                totalAmount,
                new Timestamp(System.currentTimeMillis())
            );
            
            // Add order to database
            String orderResult = ordersDb.addOrder(order);
            if (!orderResult.equals("Order Added.")) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Failed to save order: " + orderResult + "\"}");
                return;
            }
            
            // Get the order ID of the newly created order
            ordersDb.loadResults();
            ArrayList<OrdersRecords> orders = ordersDb.getResults();
            int newOrderId = orders.get(orders.size() - 1).getOrderID(); // Get last order ID
            
            // Create transaction records for each cart item
            for (JsonElement item : cartItems) {
                JsonObject itemObj = item.getAsJsonObject();
                int bookId = itemObj.get("id").getAsInt();
                int quantity = itemObj.get("quantity").getAsInt();
                
                TransactionRecords transaction = new TransactionRecords(
                    0, // transactionID (auto-generated)
                    newOrderId,
                    bookId,
                    quantity
                );
                
                String transactionResult = transactionDb.addTransaction(transaction);
                if (!transactionResult.equals("Transaction Added.")) {
                    System.err.println("Failed to add transaction: " + transactionResult);
                }
            }
            
            JsonObject responseObj = new JsonObject();
            responseObj.addProperty("success", true);
            responseObj.addProperty("orderId", newOrderId);
            responseObj.addProperty("message", "Order saved successfully");
            
            out.print(gson.toJson(responseObj));
            
            ordersDb.disconnectDb();
            transactionDb.disconnectDb();
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to save order: " + e.getMessage() + "\"}");
        }
    }
}