package com.bookstore.web;

import com.bookstore.proxy.AdminVerificationProxy;
import com.bookstore.proxy.AdminVerificationProxyImpl;
import com.bookstore.records.UserRecords;
import com.bookstore.SecUtils;
import com.bookstore.db.UserDatabase;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Base servlet class that provides admin verification functionality.
 * All admin-secured servlets should extend this class to ensure
 * proper admin verification before allowing access to admin functionality.
 */
public abstract class AdminSecuredServlet extends HttpServlet {
    
    protected final AdminVerificationProxy adminVerifier;
    protected final UserDatabase userDatabase;
    
    public AdminSecuredServlet() {
        this.adminVerifier = new AdminVerificationProxyImpl();
        this.userDatabase = new UserDatabase();
    }
    
    /**
     * Verifies if the current user has admin privileges
     * @param request The HTTP request
     * @param response The HTTP response
     * @return true if user is admin, false otherwise
     */
    protected boolean verifyAdminAccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("DEBUG: Starting admin verification...");
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            System.out.println("DEBUG: No active session found");
            sendUnauthorizedResponse(response, "No active session found");
            return false;
        }
        System.out.println("DEBUG: Session found: " + session.getId());
        
        Integer userId = (Integer) session.getAttribute("userID");
        if (userId == null) {
            System.out.println("DEBUG: No userID in session");
            sendUnauthorizedResponse(response, "User not logged in");
            return false;
        }
        System.out.println("DEBUG: User ID found in session: " + userId);
        
        // Debug: Log the user ID being checked
        System.out.println("DEBUG: Checking admin access for user ID: " + userId);
        
        if (!adminVerifier.isAdmin(userId)) {
            System.out.println("DEBUG: Admin verification failed for user ID: " + userId);
            sendUnauthorizedResponse(response, "Admin privileges required for user ID: " + userId);
            return false;
        }
        
        System.out.println("DEBUG: Admin access granted for user ID: " + userId);
        return true;
    }
    
    /**
     * Verifies admin access and returns the current user if verification passes
     * @param request The HTTP request
     * @param response The HTTP response
     * @return UserRecords if admin verification passes, null otherwise
     */
    protected UserRecords verifyAdminAndGetUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!verifyAdminAccess(request, response)) {
            return null;
        }
        
        HttpSession session = request.getSession(false);
        Integer userId = (Integer) session.getAttribute("userID");
        
        if (!userDatabase.connectDb()) {
            sendInternalServerErrorResponse(response, "Database connection failed");
            return null;
        }
        
        try {
            UserRecords user = SecUtils.findUserByID(userDatabase, userId);
            if (user == null) {
                sendUnauthorizedResponse(response, "User not found");
                return null;
            }
            return user;
        } catch (Exception e) {
            sendInternalServerErrorResponse(response, "Error retrieving user: " + e.getMessage());
            return null;
        } finally {
            userDatabase.disconnectDb();
        }
    }
    
    /**
     * Sends an unauthorized response
     * @param response The HTTP response
     * @param message The error message
     */
    protected void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        out.print("{\"error\": \"Unauthorized\", \"message\": \"" + message + "\"}");
        out.flush();
    }
    
    /**
     * Sends an internal server error response
     * @param response The HTTP response
     * @param message The error message
     */
    protected void sendInternalServerErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        out.print("{\"error\": \"Internal Server Error\", \"message\": \"" + message + "\"}");
        out.flush();
    }
    
    /**
     * Sets CORS headers for admin endpoints
     * @param response The HTTP response
     */
    protected void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }
} 