package test;

import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * Generates consistent BCrypt hashes that work across different machines
 * Uses pre-defined salts to ensure portability
 */
public class PortablePasswordHashes {
    
    public static void main(String[] args) {
        System.out.println("Generating PORTABLE BCrypt hashes for test passwords...\n");
        System.out.println("These hashes will work on ANY machine!\n");
        
        // Define test passwords and their fixed salts
        String[][] passwordData = {
            {"password123", "$2a$12$abcdefghijklmnopqrstuv"}, // Fixed salt for consistency
            {"securepass456", "$2a$12$zyxwvutsrqponmlkjihgfe"},
            {"testUser789", "$2a$12$1234567890abcdefghijkl"},
            {"admin123", "$2a$12$qwertyuiopasdfghjklzxc"},
            {"customer456", "$2a$12$mnbvcxzasdfghjklpoiuyt"}
        };
        
        System.out.println("-- PORTABLE User_Table.sql INSERT statements:");
        System.out.println("-- Copy these EXACT statements for consistent results across machines\n");
        
        for (String[] data : passwordData) {
            String password = data[0];
            String salt = data[1];
            String hash = BCrypt.hashpw(password, salt);
            
            System.out.println("-- Password: " + password);
            System.out.println("-- Hash: " + hash);
            System.out.println();
        }
        
        System.out.println("\n-- COMPLETE INSERT statements (copy these exactly):");
        System.out.println();
        
        String[] usernames = {"John Doe", "Jane Smith", "Test User", "Admin User", "Customer Demo"};
        String[] emails = {"john@example.com", "jane@example.com", "test@example.com", "admin@example.com", "customer@example.com"};
        String[] phones = {"555-555-5676", "555-555-1234", "555-555-9999", "555-555-0001", "555-555-0002"};
        
        for (int i = 0; i < passwordData.length; i++) {
            String password = passwordData[i][0];
            String salt = passwordData[i][1];
            String hash = BCrypt.hashpw(password, salt);
            String[] nameParts = usernames[i].split(" ");
            
            System.out.println("-- Password: " + password);
            System.out.printf("INSERT INTO Users (firstName, lastName, email, password, phone, status)%n");
            System.out.printf("VALUES ('%s', '%s', '%s', '%s', '%s', 'active');%n%n", 
                nameParts[0], nameParts[1], emails[i], hash, phones[i]);
        }
    }
} 