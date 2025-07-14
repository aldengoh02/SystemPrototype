/*
 * Security features for user passwords and credit cards
 * Uses AES encryption for credit cards and PBKDF2WithHmacSHA256 for passwords
 * Uses various java security features for other purposes.
 * Supports encryption and decryption as well as hashing
 * Note, in order to use this, you must have a .env file with
 * a salt and encryption key (see explanation.txt for details)
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
} 