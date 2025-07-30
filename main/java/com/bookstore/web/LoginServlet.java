/*
 * Handles login, logout, and session checking
 *
 * Endpoints:
 *  - POST /api/auth/login: Authenticates a user and creates a session.
 *  - POST /api/auth/logout: Invalidates the current user session.
 *  - GET /api/auth/check-session: Checks if a valid session exists for the user.
 *  Supports loging via either providing both email and password
 *  or just providing userID
 */

package com.bookstore.web;

import com.bookstore.SecUtils;
import com.bookstore.db.UserDatabase;
import com.bookstore.records.UserRecords;
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

//@WebServlet("/api/auth/*")
public class LoginServlet extends HttpServlet {
    private final Gson gson = new Gson();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        setupResponse(response);
        String path = request.getPathInfo();
        JsonObject json = new JsonObject();
        
        try {
            String body = request.getReader().lines().collect(Collectors.joining());
            JsonObject req = gson.fromJson(body, JsonObject.class);
            
            switch (path) {
                case "/login": login(request, req, json, response); break;
                case "/logout": logout(request, json); break;
                default:
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    json.addProperty("error", "Unknown endpoint");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.addProperty("error", "Invalid request: " + e.getMessage());
        }
        
        response.getWriter().print(gson.toJson(json));
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        setupResponse(response);
        JsonObject json = new JsonObject();
        
        if ("/check-session".equals(request.getPathInfo())) {
            sessionCheck(request, json);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            json.addProperty("error", "Endpoint not found");
        }
        
        response.getWriter().print(gson.toJson(json));
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        setupResponse(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    private void setupResponse(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }
    
    private void login(HttpServletRequest request, JsonObject req, 
                      JsonObject json, HttpServletResponse response) {
        
        String identifier = null;
        if (req.has("identifier")) {
            identifier = req.get("identifier").getAsString();
        } else if (req.has("email")) {
            identifier = req.get("email").getAsString();
        }
        
        String password = req.has("password") ? req.get("password").getAsString() : null;
        
        if (identifier == null || identifier.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.addProperty("success", false);
            json.addProperty("error", "Email or Account ID is required");
            return;
        }
        
        boolean isUserId = identifier.trim().matches("\\d+");
        if (!isUserId && (password == null || password.trim().isEmpty())) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.addProperty("success", false);
            json.addProperty("error", "Password is required for email login");
            return;
        }
        
        UserDatabase db = new UserDatabase();
        
        try {
            if (!db.connectDb()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                json.addProperty("success", false);
                json.addProperty("error", "Database connection failed");
                return;
            }
            
            UserRecords user = SecUtils.findUserForLoginFlexible(db, identifier);
            
            boolean authenticated = false;
            if (user != null) {
                if (isUserId) {
                    // Login by userID, no password required
                    authenticated = true;
                } else if (SecUtils.verifyPassword(password, user.getPassword())) {
                    // Login by email + password
                    authenticated = true;
                }
            }
            
            if (authenticated) {
                String role = SecUtils.getUserRoleName(user.getUserTypeID());
                String name = user.getFirstName() + " " + user.getLastName();
                
                HttpSession session = request.getSession(true);
                session.setAttribute("userID", user.getUserID());
                session.setAttribute("user_email", user.getEmail());
                session.setAttribute("user_name", name);
                session.setAttribute("user_role", role.toLowerCase());
                session.setMaxInactiveInterval(30 * 60);
                
                json.addProperty("success", true);
                json.addProperty("message", "Login successful");
                json.addProperty("user_id", user.getUserID());
                json.addProperty("user_name", name);
                json.addProperty("user_email", user.getEmail());
                json.addProperty("user_role", role.toLowerCase());
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                if (isUserId) {
                    json.addProperty("success", false);
                    json.addProperty("error", "Invalid account ID");
                } else {
                    json.addProperty("success", false);
                    json.addProperty("error", "Invalid email or password");
                }
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.addProperty("success", false);
            json.addProperty("error", "Login failed: " + e.getMessage());
        } finally {
            db.disconnectDb();
        }
    }
    
    private void logout(HttpServletRequest request, JsonObject json) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        json.addProperty("success", true);
        json.addProperty("message", "Logged out successfully");
    }
    
    private void sessionCheck(HttpServletRequest request, JsonObject json) {
        HttpSession session = request.getSession(false);
        
        if (session != null && session.getAttribute("userID") != null) {
            Integer userId = (Integer) session.getAttribute("userID");
            
            UserDatabase db = new UserDatabase();
            try {
                if (db.connectDb()) {
                    UserRecords user = SecUtils.findUserByID(db, userId);
                    
                    if (user != null) {
                        String role = SecUtils.getUserRoleName(user.getUserTypeID());
                        String name = user.getFirstName() + " " + user.getLastName();
                        
                        json.addProperty("authenticated", true);
                        json.addProperty("user_id", user.getUserID());
                        json.addProperty("user_name", name);
                        json.addProperty("user_email", user.getEmail());
                        json.addProperty("user_role", role.toLowerCase());
                    } else {
                        session.invalidate();
                        json.addProperty("authenticated", false);
                    }
                } else {
                    json.addProperty("authenticated", true);
                    json.addProperty("user_name", (String) session.getAttribute("user_name"));
                    json.addProperty("user_email", (String) session.getAttribute("user_email"));
                    json.addProperty("user_role", (String) session.getAttribute("user_role"));
                }
            } catch (Exception e) {
                json.addProperty("authenticated", true);
                json.addProperty("user_name", (String) session.getAttribute("user_name"));
                json.addProperty("user_email", (String) session.getAttribute("user_email"));
                json.addProperty("user_role", (String) session.getAttribute("user_role"));
            } finally {
                db.disconnectDb();
            }
        } else {
            json.addProperty("authenticated", false);
        }
    }
} 