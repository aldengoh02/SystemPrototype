/*
 * Handles login, logout, and session checking
 * POST /api/auth/login and /api/auth/logout
 * GET /api/auth/check-session
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

@WebServlet("/api/auth/*")
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
                case "/check-session": sessionCheck(request, json); break;
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
        
        String password = req.get("password").getAsString();
        
        if (identifier == null || identifier.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.addProperty("success", false);
            json.addProperty("error", "Email or Account ID is required");
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
            
            if (user != null && SecUtils.verifyPassword(password, user.getPassword())) {
                String role = SecUtils.getUserRoleName(user.getUserTypeID());
                String name = user.getFirstName() + " " + user.getLastName();
                
                HttpSession session = request.getSession(true);
                session.setAttribute("user_id", user.getUserID());
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
                json.addProperty("success", false);
                json.addProperty("error", "Invalid email/account ID or password");
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
        
        if (session != null && session.getAttribute("user_id") != null) {
            Integer userId = (Integer) session.getAttribute("user_id");
            
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