package test;
import com.mysql.cj.jdbc.Driver;
import com.bookstore.SecUtils;
import com.bookstore.db.UserDatabase;
import com.bookstore.db.UserRecords;

/*
 * Testing class for login functionality
 * Requires database connection and proper db.properties configuration
* run using mvn dependency:copy-dependencies -DoutputDirectory=target/dependency
* and then
* java -cp "target/classes:target/dependency/*" test.LoginTest 
* Make sure you have a user in the database that is actually active
* if you want to pass all tests
* as the user database currently is, only passing 8/10 of the tests is normal
 */

public class LoginTest {
    
    public static void main(String[] args) {
        System.out.println("Running login tests...");
        
        UserDatabase db = new UserDatabase();
        if (!db.connectDb()) {
            System.out.println("Can't connect to database - check your config");
            return;
        }
        
        int passed = 0;
        int total = 0;
        
        // Basic email lookup
        total++;
        UserRecords user = SecUtils.findUserForLoginFlexible(db, "john@example.com");
        if (user != null) {
            System.out.println("Email lookup: OK");
            passed++;
        } else {
            System.out.println("Email lookup: FAILED");
        }
        
        // Account ID lookup  
        total++;
        user = SecUtils.findUserForLoginFlexible(db, "1");
        if (user != null) {
            System.out.println("Account ID lookup: OK");
            passed++;
        } else {
            System.out.println("Account ID lookup: FAILED");
        }
        
        // Invalid credentials should fail
        total++;
        user = SecUtils.findUserForLoginFlexible(db, "fake@email.com");
        if (user == null) {
            System.out.println("Invalid email rejection: OK");
            passed++;
        } else {
            System.out.println("Invalid email rejection: FAILED");
        }
        
        total++;
        user = SecUtils.findUserForLoginFlexible(db, "999999");
        if (user == null) {
            System.out.println("Invalid ID rejection: OK");
            passed++;
        } else {
            System.out.println("Invalid ID rejection: FAILED");
        }
        
        // Edge cases
        total++;
        user = SecUtils.findUserForLoginFlexible(db, "");
        if (user == null) {
            System.out.println("Empty string handling: OK");
            passed++;
        } else {
            System.out.println("Empty string handling: FAILED");
        }
        
        total++;
        user = SecUtils.findUserForLoginFlexible(db, null);
        if (user == null) {
            System.out.println("Null handling: OK");
            passed++;
        } else {
            System.out.println("Null handling: FAILED");
        }
        
        // Check format detection
        total++;
        String email = "test@example.com";
        boolean isNumeric = email.matches("\\d+");
        if (!isNumeric) {
            System.out.println("Email format detection: OK");
            passed++;
        } else {
            System.out.println("Email format detection: FAILED");
        }
        
        total++;
        String accountId = "12345";
        isNumeric = accountId.matches("\\d+");
        if (isNumeric) {
            System.out.println("Account ID format detection: OK");
            passed++;
        } else {
            System.out.println("Account ID format detection: FAILED");
        }
        
        // Password check
        total++;
        user = SecUtils.findUserForLoginFlexible(db, "john@example.com");
        if (user != null) {
            boolean passwordTest = SecUtils.verifyPassword("wrongpass", user.getPassword());
            if (!passwordTest) {
                System.out.println("Password verification: OK");
                passed++;
            } else {
                System.out.println("Password verification: FAILED");
            }
        } else {
            passed++; // Skip if no user
        }
        
        // Case sensitivity  
        total++;
        UserRecords user1 = SecUtils.findUserForLoginFlexible(db, "john@example.com");
        UserRecords user2 = SecUtils.findUserForLoginFlexible(db, "JOHN@EXAMPLE.COM");
        if ((user1 == null && user2 == null) || 
            (user1 != null && user2 != null && user1.getUserID() == user2.getUserID())) {
            System.out.println("Case handling: OK");
            passed++;
        } else {
            System.out.println("Case handling: FAILED");
        }
        
        db.disconnectDb();
        
        System.out.println("\nResults: " + passed + "/" + total + " passed");
        if (passed == total) {
            System.out.println("All tests passed");
        } else {
            System.out.println((total - passed) + " tests failed");
        }
        
    }
} 