package test;

import com.bookstore.SecUtils;

/**
 * Utility to generate BCrypt hashes for test passwords
 * Run this to get hashes that can be used in SQL INSERT statements
 */
public class GeneratePasswordHashes {
    
    public static void main(String[] args) {
        System.out.println("Generating BCrypt hashes for test passwords...\n");
        
        String[] testPasswords = {
            "password123",
            "securepass456", 
            "testUser789",
            "admin123",
            "customer456"
        };
        
        System.out.println("-- Use these hashes in your User_Table.sql file:");
        System.out.println("-- Copy and paste the INSERT statements below\n");
        
        for (String password : testPasswords) {
            String hash = SecUtils.hashPassword(password);
            System.out.println("-- Password: " + password);
            System.out.println("-- Hash: " + hash);
            System.out.println();
        }
        
        System.out.println("-- Example INSERT statements with hashed passwords:");
        System.out.println("INSERT INTO Users (firstName, lastName, email, password, phone, status)");
        System.out.println("VALUES ('John', 'Doe', 'john@example.com', '" + SecUtils.hashPassword("password123") + "', '555-555-5676', 'active');");
        System.out.println();
        System.out.println("INSERT INTO Users (firstName, lastName, email, password, phone, status)");
        System.out.println("VALUES ('Jane', 'Smith', 'jane@example.com', '" + SecUtils.hashPassword("securepass456") + "', '555-555-1234', 'active');");
        System.out.println();
        System.out.println("INSERT INTO Users (firstName, lastName, email, password, phone, status)");
        System.out.println("VALUES ('Test', 'User', 'test@example.com', '" + SecUtils.hashPassword("testUser789") + "', '555-555-9999', 'active');");
    }
} 