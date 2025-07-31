package com.bookstore.web;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet for handling admin page access verification.
 * This servlet uses the proxy pattern to verify admin privileges
 * before allowing access to admin pages and functionality.
 */
public class AdminAccessServlet extends AdminSecuredServlet {
    
    private final Gson gson = new Gson();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        
        // Verify admin access
        if (!verifyAdminAccess(req, resp)) {
            return;
        }
        
        PrintWriter out = resp.getWriter();
        
        // Return admin access confirmation
        AdminAccessResponse response = new AdminAccessResponse();
        response.setAuthorized(true);
        response.setMessage("Admin access granted");
        
        out.print(gson.toJson(response));
        out.flush();
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        
        // Verify admin access
        if (!verifyAdminAccess(req, resp)) {
            return;
        }
        
        PrintWriter out = resp.getWriter();
        
        // Handle specific admin page access requests
        String pathInfo = req.getPathInfo();
        
        if (pathInfo != null && pathInfo.equals("/verify")) {
            // Return detailed admin verification response
            AdminAccessResponse response = new AdminAccessResponse();
            response.setAuthorized(true);
            response.setMessage("Admin privileges verified");
            
            out.print(gson.toJson(response));
        } else {
            // Default admin access confirmation
            AdminAccessResponse response = new AdminAccessResponse();
            response.setAuthorized(true);
            response.setMessage("Admin access confirmed");
            
            out.print(gson.toJson(response));
        }
        
        out.flush();
    }
    
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
    
    /**
     * Response class for admin access verification
     */
    private static class AdminAccessResponse {
        private boolean authorized;
        private String message;
        
        public boolean isAuthorized() {
            return authorized;
        }
        
        public void setAuthorized(boolean authorized) {
            this.authorized = authorized;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
} 