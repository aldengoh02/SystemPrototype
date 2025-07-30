package com.bookstore.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.bookstore.db.*;
import com.bookstore.records.*;
import com.bookstore.SecUtils;

/**
 * Handles email verification for user account activation.
 * Endpoint: /api/verify-email
 * Method: GET
 */
public class EmailVerificationServlet extends HttpServlet {
    private VerificationTokenDatabase tokenDB = new VerificationTokenDatabase();
    private UserDatabase userDB = new UserDatabase();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Get the verification token from the request
            String token = request.getParameter("token");
            
            if (token == null || token.trim().isEmpty()) {
                sendErrorResponse(response, 400, "Verification token is required");
                return;
            }
            
            // Connect to database
            if (!tokenDB.connectDb()) {
                sendErrorResponse(response, 500, "Failed to connect to database");
                return;
            }
            
            // Validate the token
            if (!tokenDB.isTokenValid(token)) {
                tokenDB.disconnectDb();
                sendErrorResponse(response, 400, "Invalid or expired verification token");
                return;
            }
            
            // Get the token record
            VerificationTokenRecords tokenRecord = tokenDB.findTokenByToken(token);
            if (tokenRecord == null) {
                tokenDB.disconnectDb();
                sendErrorResponse(response, 400, "Verification token not found");
                return;
            }
            
            int userId = tokenRecord.getUserId();
            
            // Connect to user database
            if (!userDB.connectDb()) {
                tokenDB.disconnectDb();
                sendErrorResponse(response, 500, "Failed to connect to user database");
                return;
            }
            
            // Get the user record
            UserRecords user = SecUtils.findUserForVerification(userDB, userId);
            if (user == null) {
                tokenDB.disconnectDb();
                userDB.disconnectDb();
                sendErrorResponse(response, 400, "User not found");
                return;
            }
            
            // Check if user is already active
            if ("active".equals(user.getStatus())) {
                tokenDB.disconnectDb();
                userDB.disconnectDb();
                sendErrorResponse(response, 400, "Account is already activated");
                return;
            }
            
            // Update user status to active
            UserRecords updatedUser = new UserRecords(
                user.getUserID(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPassword(),
                user.getPhone(),
                "active", // Set status to active
                user.isEnrollForPromotions(),
                user.getUserTypeID()
            );
            
            String updateResult = userDB.updateUser(updatedUser);
            if (!updateResult.contains("Updated")) {
                tokenDB.disconnectDb();
                userDB.disconnectDb();
                sendErrorResponse(response, 500, "Failed to activate account: " + updateResult);
                return;
            }
            
            // Delete the verification token
            tokenDB.deleteTokenByToken(token);
            
            // Clean up expired tokens
            tokenDB.deleteExpiredTokens();
            
            // Disconnect from databases
            tokenDB.disconnectDb();
            userDB.disconnectDb();
            
            jakarta.servlet.http.HttpSession session = request.getSession(true);
            session.setAttribute("userID", user.getUserID());
            session.setAttribute("user_email", user.getEmail());
            session.setAttribute("user_name", user.getFirstName() + " " + user.getLastName());
            session.setAttribute("user_role", com.bookstore.SecUtils.getUserRoleName(user.getUserTypeID()).toLowerCase());
            session.setMaxInactiveInterval(30 * 60); // arbitrary session timeout

            // Redirects to books page change as needed if you change the
            // structuring of the frontend
            response.sendRedirect("http://localhost:3000/home");
            return;
            
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        JsonObject errorResponse = new JsonObject();
        errorResponse.addProperty("success", false);
        errorResponse.addProperty("error", message);
        
        response.setStatus(statusCode);
        response.getWriter().write(gson.toJson(errorResponse));
    }
} 