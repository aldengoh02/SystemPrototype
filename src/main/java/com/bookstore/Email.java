package com.bookstore;

import java.util.Properties;
import java.util.ArrayList;
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
    public static boolean sendVerificationEmail(String userEmail, String userName, String verificationToken, String baseUrl, Integer loginUserID) {
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
            
            String emailBody = createVerificationEmailBody(userName, verificationToken, baseUrl, loginUserID);
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
    private static String createVerificationEmailBody(String userName, String verificationToken, String baseUrl, Integer loginUserID) {
        String accountIdText = loginUserID != null ? 
            "Your Account ID is: " + loginUserID + "\n\n" +
            "Please write down and remember this Account ID since it can be used to login to your account without a password.\n\n" :
            "";
            
        String body = "Hello " + userName + ",\n\n"
                + "Thank you for registering with our bookstore!\n\n"
                + accountIdText
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
    
    /**
     * Sends promotional emails to all users enrolled for promotions
     * @param promotionMessage The promotional message to send
     * @param subject The email subject
     * @return true if all emails were sent successfully, false if any failed
     */
    public static boolean sendPromotionalEmails(String promotionMessage, String subject) {
        UserDatabase userDB = new UserDatabase();
        boolean allEmailsSent = true;
        int successCount = 0;
        int failCount = 0;
        
        try {
            if (!userDB.connectDb()) {
                LOGGER.warning("Cannot send promotional emails: database connection failed");
                return false;
            }
            
            ArrayList<UserRecords> promotionUsers = userDB.getUsersEnrolledForPromotions();
            LOGGER.info("Found " + promotionUsers.size() + " users enrolled for promotions");
            
            if (promotionUsers.isEmpty()) {
                LOGGER.info("No users enrolled for promotions found");
                return true; // Not a failure, just no users to email
            }
            
            for (UserRecords user : promotionUsers) {
                boolean emailSent = sendPromotionalEmail(user.getEmail(), user.getFirstName(), promotionMessage, subject);
                if (emailSent) {
                    successCount++;
                } else {
                    failCount++;
                    allEmailsSent = false;
                }
            }
            
            LOGGER.info("Promotional email campaign completed. Success: " + successCount + ", Failed: " + failCount);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send promotional emails", e);
            return false;
        } finally {
            userDB.disconnectDb();
        }
        
        return allEmailsSent;
    }
    
    /**
     * Sends a promotional email to a single user
     * @param userEmail The user's email address
     * @param userName The user's first name
     * @param promotionMessage The promotional message
     * @param subject The email subject
     * @return true if email was sent successfully
     */
    private static boolean sendPromotionalEmail(String userEmail, String userName, String promotionMessage, String subject) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            LOGGER.warning("Cannot send promotional email: user email is null or empty");
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
            message.setSubject(subject);
            
            String emailBody = createPromotionalEmailBody(userName, promotionMessage);
            message.setText(emailBody);
            
            Transport.send(message);
            LOGGER.info("Promotional email sent successfully to: " + userEmail);
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send promotional email to: " + userEmail, e);
            return false;
        }
    }
    
    /**
     * Creates the promotional email body content
     */
    private static String createPromotionalEmailBody(String userName, String promotionMessage) {
        return "Hello " + userName + ",\n\n"
                + "We have exciting news and promotions for you!\n\n"
                + promotionMessage + "\n\n"
                + "Thank you for being a valued customer.\n\n"
                + "Best regards,\n"
                + "The Bookstore Team\n\n"
                + "If you no longer wish to receive promotional emails, please contact us.";
    }

    /**
     * Sends order confirmation email with all order details including
     * payment card info, billing address, shipping address, and order items
     */
    public static boolean sendOrderConfirmationEmail(String userEmail, String userName, 
            String orderDetails, String paymentInfo, String billingAddress, 
            String shippingAddress, double totalAmount) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            LOGGER.warning("Cannot send order confirmation email: user email is null or empty");
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
            message.setSubject("Order Confirmation - Your Bookstore Purchase");
            
            String emailBody = createOrderConfirmationEmailBody(userName, orderDetails, 
                    paymentInfo, billingAddress, shippingAddress, totalAmount);
            message.setText(emailBody);
            
            Transport.send(message);
            LOGGER.info("Order confirmation email sent successfully to: " + userEmail);
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send order confirmation email to: " + userEmail, e);
            return false;
        }
    }

    /**
     * Creates the order confirmation email body with all order details
     */
    private static String createOrderConfirmationEmailBody(String userName, String orderDetails,
            String paymentInfo, String billingAddress, String shippingAddress, double totalAmount) {
        return "Hello " + userName + ",\n\n"
                + "Thank you for your order! Here are your order details:\n\n"
                + "ORDER SUMMARY:\n"
                + orderDetails + "\n\n"
                + "PAYMENT INFORMATION:\n"
                + paymentInfo + "\n\n"
                + "BILLING ADDRESS:\n"
                + billingAddress + "\n\n"
                + "SHIPPING ADDRESS:\n"
                + shippingAddress + "\n\n"
                + "TOTAL AMOUNT: $" + String.format("%.2f", totalAmount) + "\n\n"
                + "Your order is being processed and you will receive tracking information soon.\n\n"
                + "Thank you for shopping with us!\n\n"
                + "Best regards,\n"
                + "The Bookstore Team";
    }
            
            
    
} 