/*
 * Handles login, logout, and session checking
 * POST /api/auth/login and /api/auth/logout
 * GET /api/auth/check-session
 */

package com.bookstore.web;

import com.bookstore.SecUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/api/auth/*")
public class LoginServlet extends HttpServlet {
    private final Gson gson = new Gson();
    
    // Test users  password is password123 for all
    private static final Map<String, UserInfo> testUsers = new HashMap<>();
    
    static {
        String hashedPass = SecUtils.hashPassword("password123");
        testUsers.put("admin@bookstore.com", new UserInfo("admin@bookstore.com", "Admin User", hashedPass, "admin"));
        testUsers.put("user@test.com", new UserInfo("user@test.com", "Test User", hashedPass, "customer"));
        testUsers.put("john@example.com", new UserInfo("john@example.com", "John Doe", hashedPass, "customer"));
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        setupResponse(response);
        String pathInfo = request.getPathInfo();
        JsonObject jsonResponse = new JsonObject();
        
        try {
            String requestBody = request.getReader().lines().collect(Collectors.joining());
            JsonObject jsonRequest = gson.fromJson(requestBody, JsonObject.class);
            
            // switch for pathInfo that handles 3 current endpoints
            switch (pathInfo) {
                case "/login": login(request, jsonRequest, jsonResponse, response); break;
                case "/logout": logout(request, jsonResponse); break;
                case "/check-session": sessionCheck(request, jsonResponse); break;
                default:
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    jsonResponse.addProperty("error", "Unknown endpoint");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("error", "Invalid request: " + e.getMessage());
        }
        
        response.getWriter().print(gson.toJson(jsonResponse));
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        setupResponse(response);
        JsonObject jsonResponse = new JsonObject();
        
        if ("/check-session".equals(request.getPathInfo())) {
            sessionCheck(request, jsonResponse);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            jsonResponse.addProperty("error", "Endpoint not found");
        }
        
        response.getWriter().print(gson.toJson(jsonResponse));
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        setupResponse(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    private void setupResponse(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "null");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }
    
    private void login(HttpServletRequest request, JsonObject jsonRequest, 
                      JsonObject jsonResponse, HttpServletResponse response) {
        
        String email = jsonRequest.get("email").getAsString();
        String password = jsonRequest.get("password").getAsString();
        UserInfo user = testUsers.get(email.toLowerCase());
        
        if (user != null && SecUtils.verifyPassword(password, user.hashedPassword)) {
            HttpSession session = request.getSession(true);
            session.setAttribute("user_id", user.email);
            session.setAttribute("user_email", user.email);
            session.setAttribute("user_name", user.name);
            session.setAttribute("user_role", user.role);
            session.setMaxInactiveInterval(30 * 60);
            
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("message", "Login successful");
            jsonResponse.addProperty("user_name", user.name);
            jsonResponse.addProperty("user_email", user.email);
            jsonResponse.addProperty("user_role", user.role);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("error", "Invalid email or password");
        }
    }
    
    private void logout(HttpServletRequest request, JsonObject jsonResponse) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        jsonResponse.addProperty("success", true);
        jsonResponse.addProperty("message", "Logged out successfully");
    }
    
    private void sessionCheck(HttpServletRequest request, JsonObject jsonResponse) {
        HttpSession session = request.getSession(false);
        
        if (session != null && session.getAttribute("user_id") != null) {
            jsonResponse.addProperty("authenticated", true);
            jsonResponse.addProperty("user_name", (String) session.getAttribute("user_name"));
            jsonResponse.addProperty("user_email", (String) session.getAttribute("user_email"));
            jsonResponse.addProperty("user_role", (String) session.getAttribute("user_role"));
        } else {
            jsonResponse.addProperty("authenticated", false);
        }
    }
    
    /*
     * Temp class to use for testing
     * will be scrapped and replaced with database connection later
     */
    private static class UserInfo {
        final String email, name, hashedPassword, role;
        
        UserInfo(String email, String name, String hashedPassword, String role) {
            this.email = email;
            this.name = name;
            this.hashedPassword = hashedPassword;
            this.role = role;
        }
    }
} 