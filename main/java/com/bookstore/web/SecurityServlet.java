/* 
Servlet for handling security in regards to password hashing,
credit card encryption, and decryption.
current api endpoint is /api/security/ 
use post requests whenever using this servlet.
/api/security/hash-password
/api/security/verify-password
/api/security/encrypt-card
/api/security/decrypt-card
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

public class SecurityServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String requestBody = request.getReader().lines().collect(Collectors.joining());
        JsonObject jsonRequest = gson.fromJson(requestBody, JsonObject.class);
        JsonObject jsonResponse = new JsonObject();
        
                // all of our relevant endpoints/cases
        try {
            switch (pathInfo) {
                case "/hash-password":
                    String password = jsonRequest.get("password").getAsString();
                    String hashedPassword = SecUtils.hashPassword(password);
                    jsonResponse.addProperty("hashedPassword", hashedPassword);
                    break;

                case "/verify-password":
                    String plainPassword = jsonRequest.get("password").getAsString();
                    String storedHash = jsonRequest.get("hashedPassword").getAsString();
                    boolean isValid = SecUtils.verifyPassword(plainPassword, storedHash);
                    jsonResponse.addProperty("isValid", isValid);
                    break;

                case "/encrypt-card":
                    String cardNumber = jsonRequest.get("cardNumber").getAsString();
                    String encryptedCard = SecUtils.encryptCreditCardSimple(cardNumber);
                    jsonResponse.addProperty("encryptedCard", encryptedCard);
                    break;

                case "/decrypt-card":
                    String encryptedData = jsonRequest.get("encryptedCard").getAsString();
                    String decryptedCard = SecUtils.decryptCreditCardSimple(encryptedData);
                    jsonResponse.addProperty("decryptedCard", decryptedCard);
                    break;

                default:
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    jsonResponse.addProperty("error", "Unknown endpoint");
                    break;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("error", e.getMessage());
        }

        // Send response
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(jsonResponse));
        out.flush();
    }
} 