package com.bookstore;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.bookstore.db.UserDatabase;
import com.bookstore.records.UserRecords;
import com.bookstore.SecUtils;

/**
 * Class for sending emails to users
 * for both verification
 * and profile change notifications
 * uses a dummy email acc since this will never go to prod
 */
public class Email {
    private static final Logger LOGGER = Logger.getLogger(Email.class.getName());
    

    /**
     * Variables for email config
     * username and pass allow for access to email acc
     * do not modify these values since it will break functionality
     */
    private static final String EMAIL = "csci4050mockbookstore@gmail.com";
    private static final String HOST = "smtp.gmail.com";
    private static final String PORT = "587";
    private static final String USERNAME = "csci4050mockbookstore@gmail.com";
    private static final String PASSWORD = "qtla dblc juor vqzn "; 
    
    /**
     * Sends a profile change notification email to the user by user ID
     * @param userID The user's ID to retrieve from database
     * @return true if email sent successfully, false otherwise
     */
    public static boolean sendProfileChangeNotification(int userID) {
        UserDatabase db = new UserDatabase();
        try {
            if (!db.connectDb()) {
                LOGGER.warning("Cannot send email: database connection failed");
                return false;
            }
            
            UserRecords user = SecUtils.findUserByID(db, userID);
            if (user == null) {
                LOGGER.warning("Cannot send email: user not found with ID: " + userID);
                return false;
            }
            
            return sendProfileChangeNotification(user.getEmail(), user.getFirstName());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve user data for email notification, userID: " + userID, e);
            return false;
        } finally {
            db.disconnectDb();
        }
    }
    
   /*
    * Email for profile change notifications
    */
    public static boolean sendProfileChangeNotification(String userEmail, String userName) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            LOGGER.warning("Cannot send email: user email is null or empty");
            return false;
        }
        
        try {
            // Set up mail server properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", HOST);
            props.put("mail.smtp.port", PORT);
            
            // Create session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USERNAME, PASSWORD);
                }
            });
            
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
            message.setSubject("Profile Information Updated - Bookstore");
            
            // Create email body
            String emailBody = createEmailBody(userName);
            message.setText(emailBody);
            
            // Send message
            Transport.send(message);
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send profile change notification email to: " + userEmail, e);
            return false;
        }
    }
    

    
    /**
     * Creates the email body content
     * @param userName The user's name for personalization
     * @return The formatted email body
     */
    private static String createEmailBody(String userName) {
        return userName + ", your profile information has been changed.";
    }
    
    /**
     * Test method to verify email configuration
     * This should be called during application startup or in a test environment
     */
    public static boolean testEmailConfiguration() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", HOST);
            props.put("mail.smtp.port", PORT);
            
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USERNAME, PASSWORD);
                }
            });
            
            // Test connection
            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.close();
            
            LOGGER.info("Email configuration test successful");
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Email configuration test failed", e);
            return false;
        }
    }
} 