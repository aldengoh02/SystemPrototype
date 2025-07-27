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
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class PromotionServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PromotionDatabase promotionDb = new PromotionDatabase();
        
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
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
        PromotionDatabase promotionDb = new PromotionDatabase();
        
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();

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
        PromotionDatabase promotionDb = new PromotionDatabase();
        
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

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
        PromotionDatabase promotionDb = new PromotionDatabase();
        
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

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
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // Helper class for parsing JSON requests
    private static class PromotionRequest {
        String promoCode;
        float discount;
        String startDate;
        String endDate;
    }
}