/*
 * Servlet for password resets
 * handles sending forgot password email 
 * and actually updating the password
 * endpoint is /password-reset
 *  which supports different functions
 * depending on the parameters
 * methods are POST
 */


package com.bookstore.web;

import com.bookstore.db.UserDatabase;
import com.bookstore.db.VerificationTokenDatabase;
import com.bookstore.db.VerificationTokenRecords;
import com.bookstore.records.UserRecords;
import com.bookstore.Email;
import com.bookstore.SecUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;

@WebServlet("/password-reset")
public class PasswordResetServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String token = req.getParameter("token");
        String newPassword = req.getParameter("newPassword");
        System.out.println("DEBUG PasswordResetServlet: email=" + email + ", token=" + token + ", newPassword=" + newPassword);
        resp.setContentType("application/json");

        if (email != null && token == null && newPassword == null) {
            // generate token and send email
            UserDatabase userDb = new UserDatabase();
            userDb.connectDb();
            UserRecords user = userDb.findUserByEmail(email);
            if (user == null) {
                userDb.disconnectDb();
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"User not found\"}");
                return;
            }
            VerificationTokenDatabase tokenDb = new VerificationTokenDatabase();
            tokenDb.connectDb();
            String resetToken = UUID.randomUUID().toString();
            Timestamp expiry = new Timestamp(System.currentTimeMillis() + 60 * 60 * 1000); // 1 hour
            VerificationTokenRecords tokenRecord = new VerificationTokenRecords(0, user.getUserID(), resetToken, expiry, "password_reset");
            tokenDb.addToken(tokenRecord);
            // Construct frontend reset link
            String scheme = req.getScheme();
            String serverName = req.getServerName();
            int serverPort = req.getServerPort();
            String frontendPort = "3000"; // Change to your frontend port if needed
            String resetLink = scheme + "://" + serverName + ":" + frontendPort + "/reset-password?token=" + resetToken;
            String baseUrl = scheme + "://" + serverName + ":" + frontendPort;
            Email.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetToken, baseUrl);
            tokenDb.disconnectDb();
            userDb.disconnectDb();
            resp.getWriter().write("{\"message\":\"Reset email sent\"}");
        } else if (token != null && newPassword != null) {
            // Step 2: Reset password - validate token and update password
            VerificationTokenDatabase tokenDb = new VerificationTokenDatabase();
            tokenDb.connectDb();
            VerificationTokenRecords tokenRecord = tokenDb.findTokenByToken(token);
            if (tokenRecord == null || tokenRecord.getExpiryDate().before(new Timestamp(System.currentTimeMillis()))) {
                tokenDb.disconnectDb();
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Invalid or expired token\"}");
                return;
            }
            UserDatabase userDb = new UserDatabase();
            userDb.connectDb();
            UserRecords user = SecUtils.findUserByID(userDb, tokenRecord.getUserId());
            if (user == null) {
                userDb.disconnectDb();
                tokenDb.disconnectDb();
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"User not found\"}");
                return;
            }
            String hashed = SecUtils.hashPassword(newPassword);
            user.setPassword(hashed);
            userDb.updateUser(user);
            tokenDb.deleteTokenByToken(token);
            tokenDb.disconnectDb();
            userDb.disconnectDb();
            resp.getWriter().write("{\"message\":\"Password reset successful\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Invalid request\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String token = req.getParameter("token");
        resp.setContentType("text/html");
        resp.getWriter().write(
            "<html><body>" +
            "<h2>Reset Password</h2>" +
            "<form method='POST' action='/password-reset'>" +
            "<input type='hidden' name='token' value='" + (token != null ? token : "") + "'/>" +
            "<label>New Password: <input type='password' name='newPassword' required></label><br>" +
            "<button type='submit'>Reset Password</button>" +
            "</form>" +
            "</body></html>"
        );
    }
} 