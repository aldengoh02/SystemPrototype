package test;

import com.bookstore.SecUtils;
import com.bookstore.db.UserDatabase;

/**
 * One-time setup utility for new machines
 * Creates test users with known passwords for testing
 */
public class SetupTestUsers {
    
    public static void main(String[] args) {
        System.out.println("Setting up test users for password testing...");
        
        UserDatabase db = new UserDatabase();
        if (!db.connectDb()) {
            System.out.println("❌ Can't connect to database - check your config");
            return;
        }
        
        try {
            // Define test users with known passwords
            String[][] testUsers = {
                {"1", "password123"},
                {"2", "securepass456"}, 
                {"3", "testUser789"},
                {"4", "admin123"},
                {"5", "customer456"}
            };
            
            System.out.println("\n🔧 Setting up test user passwords...");
            
            for (String[] userData : testUsers) {
                String userID = userData[0];
                String password = userData[1];
                
                String result = SecUtils.setPassword(db, Integer.parseInt(userID), password);
                System.out.println("User " + userID + " (" + password + "): " + result);
            }
            
            System.out.println("\n✅ Test users setup complete!");
            System.out.println("\n🔑 You can now test with these credentials:");
            System.out.println("User 1: password123");
            System.out.println("User 2: securepass456");
            System.out.println("User 3: testUser789");
            System.out.println("User 4: admin123");
            System.out.println("User 5: customer456");
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.disconnectDb();
        }
    }
} 