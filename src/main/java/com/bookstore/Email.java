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
     * currently quite generic but can be changed
     * to provide more detail either here or inside the actual servlets
     * 
     */
    private static String createEmailBody(String userName) {
        return userName + ", your profile information has been changed.";
    }

    /**
     * Sends an email verification email to the user
     * 
     */
    public static boolean sendVerificationEmail(String userEmail, String userName, String verificationToken, String baseUrl, int userId) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            LOGGER.warning("Cannot send verification email: user email is null or empty");
            return false;
        }
        
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
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
            message.setSubject("Verify Your Email - Bookstore Account");
            
            String emailBody = createVerificationEmailBody(userName, verificationToken, baseUrl, userId);
            message.setText(emailBody);
            
            Transport.send(message);
            LOGGER.info("Verification email sent successfully to: " + userEmail);
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send verification email to: " + userEmail, e);
            return false;
        }
    }

    /**
     * Sends the verification email 
     * to the user as well as a link to verify 
     * their account
     */
    private static String createVerificationEmailBody(String userName, String verificationToken, String baseUrl, int userId) {
        String body = "Hello " + userName + ",\n\n"
                + "Thank you for registering with our bookstore!\n\n"
                + "Your User ID is: " + userId + "\n\n"
                + "Please write down and remember this ID since it can be used to login to your account\n\n"
                + "To activate your account, please click on the following link:\n\n"
                + baseUrl + "/api/verify-email?token=" + verificationToken + "\n\n";
        return body;
    }
    
    public static boolean sendPasswordResetEmail(String userEmail, String userName, String resetToken, String baseUrl) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            LOGGER.warning("Cannot send password reset email: user email is null or empty");
            return false;
        }
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

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
            message.setSubject("Password Reset - Bookstore");

            String emailBody = "Hello " + userName + ",\n\n"
                + "You requested a password reset. Click the link below to reset your password:\n\n"
                + baseUrl + "/reset-password?token=" + resetToken + "\n\n"
                + "If you did not request this, please ignore this email.\n";
            message.setText(emailBody);

            Transport.send(message);
            LOGGER.info("Password reset email sent successfully to: " + userEmail);
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send password reset email to: " + userEmail, e);
            return false;
        }
    }
    
    
            
            
    
} 