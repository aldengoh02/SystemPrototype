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

    private static String getSecureProperty(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            value = System.getProperty(name);
        }
        return value;
    }

    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        String salt = BCrypt.gensalt(12);
        return BCrypt.hashpw(password, salt);
    }

    public static boolean verifyPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String encryptCreditCard(String cardNumber) throws Exception {
        validateCreditCardNumber(cardNumber);
        validateEnvironmentVariables();

        SecretKey key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        byte[] iv = generateIv();
        
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(cardNumber.getBytes());
        
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        
        return Base64.getEncoder().encodeToString(combined);
    }

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

    private static void validateCreditCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Credit card number cannot be null or empty");
        }
    }

    public static com.bookstore.db.UserRecords findUserForLogin(com.bookstore.db.UserDatabase db, String identifier, boolean isAccountId) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }
        
        try {
            java.sql.Connection conn = db.getConnection();
            if (conn == null) {
                return null;
            }
            
            String query;
            java.sql.PreparedStatement ps;
            
            if (isAccountId) {
                try {
                    int userId = Integer.parseInt(identifier.trim());
                    query = "SELECT * FROM Users WHERE userID = ? AND status = 'active'";
                    ps = conn.prepareStatement(query);
                    ps.setInt(1, userId);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else {
                query = "SELECT * FROM Users WHERE email = ? AND status = 'active'";
                ps = conn.prepareStatement(query);
                ps.setString(1, identifier.toLowerCase());
            }
            
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

    public static com.bookstore.db.UserRecords findUserForLoginFlexible(com.bookstore.db.UserDatabase db, String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }
        
        boolean isAccountId = identifier.trim().matches("\\d+");
        return findUserForLogin(db, identifier, isAccountId);
    }

    public static com.bookstore.db.UserRecords findUserByID(com.bookstore.db.UserDatabase db, int userID) {
        try {
            java.sql.Connection conn = db.getConnection();
            if (conn == null) {
                return null;
            }
            
            String query = "SELECT * FROM Users WHERE userID = ? AND status = 'active'";
            java.sql.PreparedStatement ps = conn.prepareStatement(query);
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

    public static String getUserRoleName(int userTypeID) {
        switch(userTypeID) {
            case 1: return "Admin";
            case 2: return "Customer"; 
            case 3: return "Employee";
            default: return "Customer";
        }
    }

    public static String updatePassword(com.bookstore.db.UserDatabase db, int userID, 
                                      String currentPassword, String newPassword) {
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            return "Current password is required";
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return "New password cannot be empty";
        }
        
        try {
            com.bookstore.db.UserRecords user = findUserByID(db, userID);
            if (user == null) {
                return "User not found";
            }
            
            if (!verifyPassword(currentPassword, user.getPassword())) {
                return "Current password is incorrect";
            }
            
            String hashedPassword = hashPassword(newPassword);
            user.setPassword(hashedPassword);
            String result = db.updateUser(user);
            
            if (result.contains("Updated")) {
                return "Password updated successfully";
            } else {
                return "Failed to update password: " + result;
            }
            
        } catch (Exception e) {
            return "Error updating password: " + e.getMessage();
        }
    }

    public static String setPassword(com.bookstore.db.UserDatabase db, int userID, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return "Password cannot be empty";
        }
        
        try {
            com.bookstore.db.UserRecords user = findUserByID(db, userID);
            if (user == null) {
                return "User not found";
            }
            
            String hashedPassword = hashPassword(newPassword);
            user.setPassword(hashedPassword);
            String result = db.updateUser(user);
            
            if (result.contains("Updated")) {
                return "Password set successfully";
            } else {
                return "Failed to set password: " + result;
            }
            
        } catch (Exception e) {
            return "Error setting password: " + e.getMessage();
        }
    }

    public static String setPasswordByEmail(com.bookstore.db.UserDatabase db, String email, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return "Password cannot be empty";
        }
        
        try {
            java.sql.Connection conn = db.getConnection();
            if (conn == null) {
                return "Database connection failed";
            }
            
            String hashedPassword = hashPassword(newPassword);
            String query = "UPDATE Users SET password = ? WHERE email = ?";
            java.sql.PreparedStatement ps = conn.prepareStatement(query);
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