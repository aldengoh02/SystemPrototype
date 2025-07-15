/*
 * Handles login, logout, and session checking
 * POST /api/auth/login and /api/auth/logout
 * GET /api/auth/check-session
 */

package com.bookstore.web;

import com.bookstore.SecUtils;
import com.bookstore.db.UserDatabase;
import com.bookstore.db.UserRecords;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet("/api/auth/*")
public class LoginServlet extends HttpServlet {
    private final Gson gson = new Gson();
    
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
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000"); // Allow React dev server
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }
    
    private void login(HttpServletRequest request, JsonObject jsonRequest, 
                      JsonObject jsonResponse, HttpServletResponse response) {
        
        String email = jsonRequest.get("email").getAsString();
        String password = jsonRequest.get("password").getAsString();
        
        UserDatabase userDb = new UserDatabase();
        
        try {
            if (!userDb.connectDb()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("error", "Database connection failed");
                return;
            }
            
            // Find user by email using SecUtils (one-time lookup)
            UserRecords user = SecUtils.findUserForLogin(userDb, email);
            
            if (user != null && SecUtils.verifyPassword(password, user.getPassword())) {
                String userRole = SecUtils.getUserRoleName(user.getUserTypeID());
                String fullName = user.getFirstName() + " " + user.getLastName();
                
                // Create session with userID as the key identifier
                HttpSession session = request.getSession(true);
                session.setAttribute("user_id", user.getUserID());  // Store ID for fast lookups
                session.setAttribute("user_email", user.getEmail());
                session.setAttribute("user_name", fullName);
                session.setAttribute("user_role", userRole.toLowerCase());
                session.setMaxInactiveInterval(30 * 60); // 30 minutes
                
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Login successful");
                jsonResponse.addProperty("user_id", user.getUserID());
                jsonResponse.addProperty("user_name", fullName);
                jsonResponse.addProperty("user_email", user.getEmail());
                jsonResponse.addProperty("user_role", userRole.toLowerCase());
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("error", "Invalid email or password");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("error", "Login failed: " + e.getMessage());
        } finally {
            userDb.disconnectDb();
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
            Integer userId = (Integer) session.getAttribute("user_id");
            
            // Fast lookup by userID to verify user is still active
            UserDatabase userDb = new UserDatabase();
            try {
                if (userDb.connectDb()) {
                    UserRecords user = SecUtils.findUserByID(userDb, userId);
                    
                    if (user != null) {
                        String userRole = SecUtils.getUserRoleName(user.getUserTypeID());
                        String fullName = user.getFirstName() + " " + user.getLastName();
                        
                        jsonResponse.addProperty("authenticated", true);
                        jsonResponse.addProperty("user_id", user.getUserID());
                        jsonResponse.addProperty("user_name", fullName);
                        jsonResponse.addProperty("user_email", user.getEmail());
                        jsonResponse.addProperty("user_role", userRole.toLowerCase());
                    } else {
                        // User not found or inactive - invalidate session
                        session.invalidate();
                        jsonResponse.addProperty("authenticated", false);
                    }
                } else {
                    // Database connection failed - fall back to session data
                    jsonResponse.addProperty("authenticated", true);
                    jsonResponse.addProperty("user_name", (String) session.getAttribute("user_name"));
                    jsonResponse.addProperty("user_email", (String) session.getAttribute("user_email"));
                    jsonResponse.addProperty("user_role", (String) session.getAttribute("user_role"));
                }
            } catch (Exception e) {
                // Error occurred - fall back to session data
                jsonResponse.addProperty("authenticated", true);
                jsonResponse.addProperty("user_name", (String) session.getAttribute("user_name"));
                jsonResponse.addProperty("user_email", (String) session.getAttribute("user_email"));
                jsonResponse.addProperty("user_role", (String) session.getAttribute("user_role"));
            } finally {
                userDb.disconnectDb();
            }
        } else {
            jsonResponse.addProperty("authenticated", false);
        }
    }
    

} 