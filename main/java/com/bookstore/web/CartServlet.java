/**
 * REST endpoint for cart operations
 * 
 * Base URL: http://localhost:8080/api/cart
 * 
 * Endpoints:
 *  
 *  GET /api/cart
 *    Returns: Array of cart items for the logged-in user
 *    Requires: Active session
 * 
 *  POST /api/cart
 *    Body: {bookID: number, quantity: number}
 *    Action: Adds item to cart or updates quantity if item exists
 *    Requires: Active session
 * 
 *  PUT /api/cart
 *    Body: {bookID: number, quantity: number}
 *    Action: Updates quantity of existing cart item
 *    Requires: Active session
 * 
 *  DELETE /api/cart/{bookID}
 *    Action: Removes item from cart
 *    Requires: Active session
 * 
 *  DELETE /api/cart
 *    Action: Clears entire cart
 *    Requires: Active session
 * 
 *  POST /api/cart/merge
 *    Body: Array of {bookID: number, quantity: number}
 *    Action: Merges guest cart with user cart on login
 *    Requires: Active session
 */

package com.bookstore.web;

import com.bookstore.db.CartDatabase;
import com.bookstore.db.BookDatabase;
import com.bookstore.records.CartRecord;
import com.bookstore.records.BookRecords;
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

public class CartServlet extends HttpServlet {
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

        CartDatabase cartDb = new CartDatabase();
        BookDatabase bookDb = new BookDatabase();
        
        if (!cartDb.connectDb() || !bookDb.connectDb()) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Database connection failed\"}");
            return;
        }

        try {
            cartDb.loadResults();
            List<CartRecord> userCartItems = cartDb.getResults().stream()
                .filter(cart -> cart.getUserID() == userId)
                .collect(Collectors.toList());

            // Get book details for each cart item
            bookDb.loadResults();
            JsonArray cartArray = new JsonArray();
            for (CartRecord cartItem : userCartItems) {
                // Find book by ID from loaded results
                BookRecords book = bookDb.getResults().stream()
                    .filter(b -> b.getId() == cartItem.getBookID())
                    .findFirst().orElse(null);
                
                if (book != null) {
                    JsonObject cartItemJson = new JsonObject();
                    cartItemJson.addProperty("id", book.getId());
                    cartItemJson.addProperty("title", book.getTitle());
                    cartItemJson.addProperty("author", book.getAuthor());
                    cartItemJson.addProperty("sellingPrice", book.getSellingPrice());
                    cartItemJson.addProperty("quantity", cartItem.getQuantity());
                    cartArray.add(cartItemJson);
                }
            }
            
            out.print(gson.toJson(cartArray));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Error fetching cart: " + e.getMessage() + "\"}");
            e.printStackTrace();
        } finally {
            cartDb.disconnectDb();
            bookDb.disconnectDb();
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

        String path = request.getPathInfo();
        
        try {
            String body = request.getReader().lines().collect(Collectors.joining());
            
            if ("/merge".equals(path)) {
                handleMergeCart(userId, body, response, out);
            } else {
                handleAddToCart(userId, body, response, out);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Error processing request: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    private void handleMergeCart(Integer userId, String body, HttpServletResponse response, PrintWriter out) {
        try {
            JsonArray guestCartArray = gson.fromJson(body, JsonArray.class);
            CartDatabase cartDb = new CartDatabase();
            
            if (!cartDb.connectDb()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Database connection failed\"}");
                return;
            }

            cartDb.loadResults();
            
            for (JsonElement element : guestCartArray) {
                JsonObject guestItem = element.getAsJsonObject();
                int bookId = guestItem.get("bookID").getAsInt();
                int quantity = guestItem.get("quantity").getAsInt();
                
                // Check if item already exists in user's cart
                boolean exists = cartDb.getResults().stream()
                    .anyMatch(cart -> cart.getUserID() == userId && cart.getBookID() == bookId);
                
                if (exists) {
                    // Update existing item (add quantities)
                    CartRecord existing = cartDb.getResults().stream()
                        .filter(cart -> cart.getUserID() == userId && cart.getBookID() == bookId)
                        .findFirst().orElse(null);
                    if (existing != null) {
                        existing.setQuantity(existing.getQuantity() + quantity);
                        cartDb.updateCartRecord(existing);
                    }
                } else {
                    // Add new item
                    CartRecord newItem = new CartRecord(userId, bookId, quantity);
                    cartDb.addCartRecord(newItem);
                }
            }
            
            cartDb.disconnectDb();
            out.print("{\"success\": \"Cart merged successfully\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Error merging cart: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    private void handleAddToCart(Integer userId, String body, HttpServletResponse response, PrintWriter out) {
        try {
            JsonObject requestData = gson.fromJson(body, JsonObject.class);
            int bookId = requestData.get("bookID").getAsInt();
            int quantity = requestData.get("quantity").getAsInt();
            
            CartDatabase cartDb = new CartDatabase();
            
            if (!cartDb.connectDb()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Database connection failed\"}");
                return;
            }

            cartDb.loadResults();
            
            // Check if item already exists
            CartRecord existing = cartDb.getResults().stream()
                .filter(cart -> cart.getUserID() == userId && cart.getBookID() == bookId)
                .findFirst().orElse(null);
            
            if (existing != null) {
                // Update existing item
                existing.setQuantity(existing.getQuantity() + quantity);
                cartDb.updateCartRecord(existing);
            } else {
                // Add new item
                CartRecord newItem = new CartRecord(userId, bookId, quantity);
                cartDb.addCartRecord(newItem);
            }
            
            cartDb.disconnectDb();
            out.print("{\"success\": \"Item added to cart\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Error adding to cart: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
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
            String body = request.getReader().lines().collect(Collectors.joining());
            JsonObject requestData = gson.fromJson(body, JsonObject.class);
            int bookId = requestData.get("bookID").getAsInt();
            int quantity = requestData.get("quantity").getAsInt();
            
            CartDatabase cartDb = new CartDatabase();
            
            if (!cartDb.connectDb()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Database connection failed\"}");
                return;
            }

            if (quantity <= 0) {
                cartDb.deleteCartRecord(userId, bookId);
                out.print("{\"success\": \"Item removed from cart\"}");
            } else {
                CartRecord cartItem = new CartRecord(userId, bookId, quantity);
                cartDb.updateCartRecord(cartItem);
                out.print("{\"success\": \"Cart updated\"}");
            }
            
            cartDb.disconnectDb();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Error updating cart: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        setupResponse(response);
        PrintWriter out = response.getWriter();
        
        Integer userId = getUserIdFromSession(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"User not authenticated\"}");
            return;
        }

        String path = request.getPathInfo();
        CartDatabase cartDb = new CartDatabase();
        
        if (!cartDb.connectDb()) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Database connection failed\"}");
            return;
        }

        try {
            if (path == null || path.equals("/")) {
                // Clear entire cart
                cartDb.loadResults();
                List<CartRecord> userItems = cartDb.getResults().stream()
                    .filter(cart -> cart.getUserID() == userId)
                    .collect(Collectors.toList());
                
                for (CartRecord item : userItems) {
                    cartDb.deleteCartRecord(userId, item.getBookID());
                }
                
                out.print("{\"success\": \"Cart cleared\"}");
            } else {
                // Remove specific item
                String bookIdStr = path.substring(1); // Remove leading "/"
                int bookId = Integer.parseInt(bookIdStr);
                cartDb.deleteCartRecord(userId, bookId);
                out.print("{\"success\": \"Item removed from cart\"}");
            }
            
            cartDb.disconnectDb();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Error removing from cart: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}