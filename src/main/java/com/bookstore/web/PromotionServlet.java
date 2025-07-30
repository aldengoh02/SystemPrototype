/**
 * REST endpoint for promotion management
 * Following javadoc shows format and examples of endpoints
 * currently in use
 * 
 * Base URL: http://localhost:8080/api/promotions
 * 
 * Current endpoints:
 *  
 *  GET /api/promotions
 *    Returns: Array of all promotions
 *    Example: GET http://localhost:8080/api/promotions
 * 
 *  GET /api/promotions/{id}
 *    Returns: Single promotion object
 *    Example: GET http://localhost:8080/api/promotions/1
 * 
 *  POST /api/promotions
 *    Body: PromotionRecords object
 *    Returns: Success/error message
 *    Example: POST http://localhost:8080/api/promotions
 *             Body: {"promoCode": "SAVE20", "discount": 20.0, "startDate": "2025-01-01", "endDate": "2025-03-31"}
 * 
 *  PUT /api/promotions/{id}
 *    Body: PromotionRecords object
 *    Returns: Success/error message
 *    Example: PUT http://localhost:8080/api/promotions/1
 * 
 *  DELETE /api/promotions/{id}
 *    Returns: Success/error message
 *    Example: DELETE http://localhost:8080/api/promotions/1
 */

package com.bookstore.web;

import com.bookstore.db.PromotionDatabase;
import com.bookstore.records.PromotionRecords;
import com.bookstore.Email;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class PromotionServlet extends AdminSecuredServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("DEBUG: GET request received for promotions");
        setCorsHeaders(resp);
        
        PromotionDatabase promotionDb = new PromotionDatabase();
        
        PrintWriter out = resp.getWriter();

        if (promotionDb.connectDb()) {
            try {
                String pathInfo = req.getPathInfo();
                
                if (pathInfo != null && !pathInfo.equals("/")) {
                    // Get specific promotion by ID
                    try {
                        int promoId = Integer.parseInt(pathInfo.substring(1));
                        promotionDb.loadResults();
                        ArrayList<PromotionRecords> promotions = promotionDb.getResults();
                        
                        PromotionRecords promotion = promotions.stream()
                                .filter(p -> p.getPromoID() == promoId)
                                .findFirst()
                                .orElse(null);
                        
                        if (promotion != null) {
                            out.print(gson.toJson(promotion));
                        } else {
                            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            out.print("{\"error\": \"Promotion not found\"}");
                        }
                    } catch (NumberFormatException e) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\": \"Invalid promotion ID\"}");
                    }
                } else {
                    // Get all promotions
                    promotionDb.loadResults();
                    ArrayList<PromotionRecords> promotions = promotionDb.getResults();
                    out.print(gson.toJson(promotions));
                }
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Error processing request: " + e.getMessage() + "\"}");
                e.printStackTrace();
            } finally {
                promotionDb.disconnectDb();
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to connect to the database\"}");
        }
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        
        // Verify admin access for all POST operations
        if (!verifyAdminAccess(req, resp)) {
            return;
        }
        
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();
        
        // Check if this is a request to send promotional emails
        if (pathInfo != null && pathInfo.equals("/send-email")) {
            handleSendPromotionalEmail(req, resp, out);
            return;
        }
        
        // Check if this is a request to push a specific promotion
        if (pathInfo != null && pathInfo.contains("/push/")) {
            handlePushPromotion(req, resp, out);
            return;
        }
        
        // Original promotion creation logic
        PromotionDatabase promotionDb = new PromotionDatabase();

        if (promotionDb.connectDb()) {
            try {
                String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                
                // Parse the promotion data from request body
                PromotionRequest promotionRequest = gson.fromJson(requestBody, PromotionRequest.class);
                
                // Convert string dates to SQL dates
                Date startDate = Date.valueOf(promotionRequest.startDate);
                Date endDate = Date.valueOf(promotionRequest.endDate);
                
                // Create PromotionRecords object (ID will be auto-generated)
                PromotionRecords newPromotion = new PromotionRecords(
                    0, // ID will be auto-generated by database
                    promotionRequest.promoCode,
                    promotionRequest.discount,
                    startDate,
                    endDate
                );
                
                String result = promotionDb.addPromotion(newPromotion);
                
                if (result.equals("Promotion Added.")) {
                    resp.setStatus(HttpServletResponse.SC_CREATED);
                    out.print("{\"message\": \"" + result + "\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"error\": \"" + result + "\"}");
                }

            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Error processing the request: " + e.getMessage() + "\"}");
                e.printStackTrace();
            } finally {
                promotionDb.disconnectDb();
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to connect to the database\"}");
        }
        out.flush();
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        
        // Verify admin access for all PUT operations
        if (!verifyAdminAccess(req, resp)) {
            return;
        }
        
        PromotionDatabase promotionDb = new PromotionDatabase();
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Promotion ID is required for update\"}");
            out.flush();
            return;
        }

        if (promotionDb.connectDb()) {
            try {
                int promoId = Integer.parseInt(pathInfo.substring(1));
                String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                
                PromotionRequest promotionRequest = gson.fromJson(requestBody, PromotionRequest.class);
                
                Date startDate = Date.valueOf(promotionRequest.startDate);
                Date endDate = Date.valueOf(promotionRequest.endDate);
                
                PromotionRecords updatedPromotion = new PromotionRecords(
                    promoId,
                    promotionRequest.promoCode,
                    promotionRequest.discount,
                    startDate,
                    endDate
                );
                
                String result = promotionDb.updatePromotion(updatedPromotion);
                
                if (result.equals("Promotion Updated.")) {
                    out.print("{\"message\": \"" + result + "\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"error\": \"" + result + "\"}");
                }

            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid promotion ID\"}");
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Error processing the request: " + e.getMessage() + "\"}");
                e.printStackTrace();
            } finally {
                promotionDb.disconnectDb();
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to connect to the database\"}");
        }
        out.flush();
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("DEBUG: DELETE request received for promotions");
        setCorsHeaders(resp);
        
        // Verify admin access for all DELETE operations
        System.out.println("DEBUG: About to verify admin access");
        if (!verifyAdminAccess(req, resp)) {
            System.out.println("DEBUG: Admin verification failed");
            return;
        }
        System.out.println("DEBUG: Admin verification passed, proceeding with delete");
        
        PromotionDatabase promotionDb = new PromotionDatabase();
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Promotion ID is required for deletion\"}");
            out.flush();
            return;
        }

        if (promotionDb.connectDb()) {
            try {
                int promoId = Integer.parseInt(pathInfo.substring(1));
                String result = promotionDb.deletePromotion(promoId);
                
                if (result.equals("Promotion Deleted.")) {
                    out.print("{\"message\": \"" + result + "\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"error\": \"" + result + "\"}");
                }

            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid promotion ID\"}");
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Error processing the request: " + e.getMessage() + "\"}");
                e.printStackTrace();
            } finally {
                promotionDb.disconnectDb();
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to connect to the database\"}");
        }
        out.flush();
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Handle pre-flight CORS requests from the browser
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Handles push promotion requests - sends targeted promotional email for a specific promotion
     */
    private void handlePushPromotion(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            // Extract promotion ID from path like "/push/1"
            String promoIdStr = pathInfo.substring(pathInfo.lastIndexOf("/") + 1);
            int promoId = Integer.parseInt(promoIdStr);
            
            String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            PushRequest pushRequest = gson.fromJson(requestBody, PushRequest.class);
            
            // Validate input
            if (pushRequest.message == null || pushRequest.message.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Message is required\"}");
                return;
            }
            
            // Get promotion details
            PromotionDatabase promotionDb = new PromotionDatabase();
            if (!promotionDb.connectDb()) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Failed to connect to database\"}");
                return;
            }
            
            try {
                promotionDb.loadResults();
                ArrayList<PromotionRecords> promotions = promotionDb.getResults();
                
                PromotionRecords promotion = promotions.stream()
                        .filter(p -> p.getPromoID() == promoId)
                        .findFirst()
                        .orElse(null);
                
                if (promotion == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\": \"Promotion not found\"}");
                    return;
                }
                
                // Create email content with promotion details
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                String emailSubject = "Special Promotion: " + promotion.getPromoCode();
                String emailMessage = pushRequest.message + "\n\n" +
                        "Promotion Code: " + promotion.getPromoCode() + "\n" +
                        "Discount: " + promotion.getDiscount() + "%\n" +
                        "Valid from: " + dateFormat.format(promotion.getStartDate()) + "\n" +
                        "Valid until: " + dateFormat.format(promotion.getEndDate()) + "\n\n" +
                        "Use this code at checkout to save!";
                
                // Send the promotional email
                boolean success = Email.sendPromotionalEmails(emailMessage, emailSubject);
                
                if (success) {
                    // Mark promotion as pushed
                    String updateResult = promotionDb.updatePromotionPushedStatus(promoId, true);
                    if (updateResult.equals("Promotion push status updated.")) {
                        resp.setStatus(HttpServletResponse.SC_OK);
                        out.print("{\"message\": \"Promotion pushed successfully\"}");
                    } else {
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        out.print("{\"error\": \"Email sent but failed to update promotion status\"}");
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"error\": \"Failed to send promotional emails\"}");
                }
                
            } finally {
                promotionDb.disconnectDb();
            }
            
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid promotion ID\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Error processing push request: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    /**
     * Handles promotional email sending requests
     */
    private void handleSendPromotionalEmail(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws IOException {
        try {
            String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            EmailRequest emailRequest = gson.fromJson(requestBody, EmailRequest.class);
            
            if (emailRequest.subject == null || emailRequest.subject.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Email subject is required\"}");
                return;
            }
            
            if (emailRequest.message == null || emailRequest.message.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Email message is required\"}");
                return;
            }
            
            boolean success = Email.sendPromotionalEmails(emailRequest.message, emailRequest.subject);
            
            if (success) {
                resp.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"message\": \"Promotional emails sent successfully\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Failed to send some or all promotional emails\"}");
            }
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Error processing email request: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    // Helper class for parsing JSON requests
    private static class PromotionRequest {
        String promoCode;
        float discount;
        String startDate;
        String endDate;
    }
    
    // Helper class for parsing email requests
    private static class EmailRequest {
        String subject;
        String message;
    }
    
    // Helper class for parsing push requests
    private static class PushRequest {
        String message;
    }
}