/*
 * Security features for user passwords and credit cards
 * Uses Java's built in AESencryption and hashing libraries
 * and BCrypt
 */
package com.bookstore;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.bcrypt.BCrypt;
import com.bookstore.records.UserRecords;

public class SecUtils {

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



    private static void validateCreditCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Credit card number cannot be null or empty");
        }
    }

    private static UserRecords toUserRecord(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new UserRecords(
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

    /*
     * Finds a specific user for login
     * Note that if you try to login with an account that has not been activated
     * this method fails.
     * This is intentional!
     */
    public static UserRecords findUserForLogin(com.bookstore.db.UserDatabase db, String identifier, boolean isAccountId) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }
        
        if (isAccountId) {
            try {
                int userId = Integer.parseInt(identifier.trim());
                return findUserByID(db, userId);
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            try {
                java.sql.Connection conn = db.getConnection();
                if (conn == null) {
                    return null;
                }
                
                String query = "SELECT * FROM Users WHERE email = ? AND status = 'active'";
                java.sql.PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, identifier.toLowerCase());
                
                java.sql.ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    return toUserRecord(rs);
                }
            } catch (java.sql.SQLException e) {
                e.printStackTrace(); // debugging logging, candidate for removal
            }
        }
        return null;
    }

    public static UserRecords findUserForLoginFlexible(com.bookstore.db.UserDatabase db, String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }
        
        boolean isAccountId = identifier.trim().matches("\\d+");
        return findUserForLogin(db, identifier, isAccountId);
    }

    /*
     * Find a user by their userID
     * Only does so if they are an active user
     */
    public static UserRecords findUserByID(com.bookstore.db.UserDatabase db, int userID) {
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
                return toUserRecord(rs);
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /*
     * Find a user by their userID
     * Copies findUserByID but only returns inactive users
     * since those are the only users that need to be verified
     */
    public static UserRecords findUserForVerification(com.bookstore.db.UserDatabase db, int userID) {
        try {
            java.sql.Connection conn = db.getConnection();
            if (conn == null) {
                return null;
            }
            
            String query = "SELECT * FROM Users WHERE userID = ? AND status = 'inactive'";
            java.sql.PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, userID);
            java.sql.ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return toUserRecord(rs);
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return null;
    }




    /*
     * Get user role name with quick int check
     */
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
            UserRecords user = findUserByID(db, userID);
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
            UserRecords user = findUserByID(db, userID);
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

    public static String encryptCreditCardSimple(String cardNumber) throws Exception {
        validateCreditCardNumber(cardNumber);
        
        // Use a hardcoded key for simple encryption
        String keySource = "BookStoreApp2024SecretKey123456789012345678901234"; 
        SecretKeySpec key = new SecretKeySpec(keySource.substring(0, 32).getBytes(), "AES");
        
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(cardNumber.getBytes());
        
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decryptCreditCardSimple(String encryptedData) throws Exception {
        // Use the same hardcoded key
        String keySource = "BookStoreApp2024SecretKey123456789012345678901234";
        SecretKeySpec key = new SecretKeySpec(keySource.substring(0, 32).getBytes(), "AES");
        
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        
        return new String(decrypted, StandardCharsets.UTF_8);
    }
} 