/*
 * Security features for user passwords and credit cards
 * Uses AES encryption for credit cards and PBKDF2WithHmacSHA256 for passwords
 * Uses various java security features for other purposes.
 * Supports encryption and decryption as well as hashing
 * Note, in order to use this, you must have a .env file with
 * a salt and encryption key (see explanation.txt for details)
 * Also allows updating of and setting of new passwords for users
 * (will be used in registration servlet eventually)
 */

package com.bookstore;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class SecUtils {
    private static final String encryptionKey = getSecureProperty("encryptionKey");
    private static final String Salt = getSecureProperty("Salt");
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    // Helper method to get property from environment or system properties
    private static String getSecureProperty(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            value = System.getProperty(name);
        }
        return value;
    }

    // For password hashing
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        String salt = BCrypt.gensalt(12);
        return BCrypt.hashpw(plainTextPassword, salt);
    }

    // For password verification
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // For credit card encryption
    public static String encryptCreditCard(String creditCardNumber) throws Exception {
        validateCreditCardNumber(creditCardNumber);
        validateEnvironmentVariables();

        SecretKey key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        byte[] iv = generateIv();
        
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(creditCardNumber.getBytes());
        
        // Combine IV and encrypted data
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        
        return Base64.getEncoder().encodeToString(combined);
    }

    // For credit card decryption
    public static String decryptCreditCard(String encryptedData) throws Exception {
        validateEnvironmentVariables();
        
        byte[] combined = Base64.getDecoder().decode(encryptedData);
        byte[] iv = new byte[16];
        byte[] encrypted = new byte[combined.length - 16];
        
        System.arraycopy(combined, 0, iv, 0, 16);
        System.arraycopy(combined, 16, encrypted, 0, encrypted.length);
        
        SecretKey key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private static SecretKey generateKey() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(encryptionKey.toCharArray(), Salt.getBytes(), 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private static byte[] generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private static void validateEnvironmentVariables() {
        if (encryptionKey == null || Salt == null) {
            throw new IllegalStateException("Encryption secret key and salt must be set in environment variables");
        }
    }

    private static void validateCreditCardNumber(String creditCardNumber) {
        if (creditCardNumber == null || creditCardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Credit card number cannot be null or empty");
        }
    }

    // User Authentication Methods
    
    /**
     * Find user by email for login (one-time lookup)
     * 
     */
    public static com.bookstore.db.UserRecords findUserForLogin(com.bookstore.db.UserDatabase userDatabase, String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        try {
            java.sql.Connection connection = userDatabase.getConnection();
            if (connection == null) {
                return null;
            }
            
            String query = "SELECT * FROM Users WHERE email = ? AND status = 'active'";
            java.sql.PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, email.toLowerCase());
            java.sql.ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new com.bookstore.db.UserRecords(
                    rs.getInt("userID"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("phone"),
                    rs.getString("status"),
                    rs.getBoolean("enrollForPromotions"),
                    rs.getInt("userTypeID")
                );
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * search for user by ID in backend
     * since String search is slow
     */
    public static com.bookstore.db.UserRecords findUserByID(com.bookstore.db.UserDatabase userDatabase, int userID) {
        try {
            java.sql.Connection connection = userDatabase.getConnection();
            if (connection == null) {
                return null;
            }
            
            String query = "SELECT * FROM Users WHERE userID = ? AND status = 'active'";
            java.sql.PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userID);
            java.sql.ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new com.bookstore.db.UserRecords(
                    rs.getInt("userID"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("phone"),
                    rs.getString("status"),
                    rs.getBoolean("enrollForPromotions"),
                    rs.getInt("userTypeID")
                );
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get user role via their userTypeID
     */
    public static String getUserRoleName(int userTypeID) {
        switch(userTypeID) {
            case 1: return "Admin";
            case 2: return "Customer"; 
            case 3: return "Employee";
            default: return "Customer";
        }
    }

    /**
     * Update user password with verification
     * For use in edit profile field eventually
     */
    public static String updatePassword(com.bookstore.db.UserDatabase userDatabase, int userID, 
                                      String currentPassword, String newPassword) {
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            return "Current password is required";
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return "New password cannot be empty";
        }
        
        
        
        try {
            // Get current user
            com.bookstore.db.UserRecords user = findUserByID(userDatabase, userID);
            if (user == null) {
                return "User not found";
            }
            
            // Verify current password
            if (!verifyPassword(currentPassword, user.getPassword())) {
                return "Current password is incorrect";
            }
            
            // Hash the new password
            String hashedNewPassword = hashPassword(newPassword);
            
            // Update in database
            user.setPassword(hashedNewPassword);
            String updateResult = userDatabase.updateUser(user);
            
            if (updateResult.contains("Updated")) {
                return "Password updated successfully";
            } else {
                return "Failed to update password: " + updateResult;
            }
            
        } catch (Exception e) {
            return "Error updating password: " + e.getMessage();
        }
    }

    /**
     * Sets password based off of userID
     *  Useful for things like changing password
     */
    public static String setPassword(com.bookstore.db.UserDatabase userDatabase, int userID, 
                                   String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return "Password cannot be empty";
        }
        
        
        
        try {
            // Get user
            com.bookstore.db.UserRecords user = findUserByID(userDatabase, userID);
            if (user == null) {
                return "User not found";
            }
            
            // Hash the password
            String hashedPassword = hashPassword(newPassword);
            
            // Update in database
            user.setPassword(hashedPassword);
            String updateResult = userDatabase.updateUser(user);
            
            if (updateResult.contains("Updated")) {
                return "Password set successfully";
            } else {
                return "Failed to set password: " + updateResult;
            }
            
        } catch (Exception e) {
            return "Error setting password: " + e.getMessage();
        }
    }

    /**
     * Set password based soleyl off of email
     * Useful for forgot password feature (not implemented yet)
     */
    public static String setPasswordByEmail(com.bookstore.db.UserDatabase userDatabase, String email, 
                                          String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return "Password cannot be empty";
        }
        
       
        
        try {
            java.sql.Connection connection = userDatabase.getConnection();
            if (connection == null) {
                return "Database connection failed";
            }
            
            // Hash the password
            String hashedPassword = hashPassword(newPassword);
            
            // Update password directly by email
            String query = "UPDATE Users SET password = ? WHERE email = ?";
            java.sql.PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, hashedPassword);
            ps.setString(2, email.toLowerCase());
            
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                return "Password set successfully";
            } else {
                return "User not found";
            }
            
        } catch (Exception e) {
            return "Error setting password: " + e.getMessage();
        }
    }
} 