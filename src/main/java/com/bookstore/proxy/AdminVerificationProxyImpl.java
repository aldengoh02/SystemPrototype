package com.bookstore.proxy;

import com.bookstore.SecUtils;
import com.bookstore.db.UserDatabase;
import com.bookstore.records.UserRecords;

/**
 * Concrete implementation of AdminVerificationProxy.
 * This class provides the actual admin verification logic
 * and acts as a proxy to control access to admin functionality.
 */
public class AdminVerificationProxyImpl implements AdminVerificationProxy {
    
    private final UserDatabase userDatabase;
    
    public AdminVerificationProxyImpl() {
        this.userDatabase = new UserDatabase();
    }
    
    public AdminVerificationProxyImpl(UserDatabase userDatabase) {
        this.userDatabase = userDatabase;
    }
    
    @Override
    public boolean isAdmin(UserRecords user) {
        if (user == null) {
            System.out.println("Admin verification failed: User is null");
            return false;
        }
        
        boolean isAdmin = user.getUserTypeID() == 1; // Admin userTypeID is 1
        
        String message = getVerificationMessage(user);
        System.out.println(message);
        
        return isAdmin;
    }
    
    @Override
    public boolean isAdmin(int userId) {
        if (!userDatabase.connectDb()) {
            System.out.println("Admin verification failed: Cannot connect to database");
            return false;
        }
        
        try {
            UserRecords user = SecUtils.findUserByID(userDatabase, userId);
            return isAdmin(user);
        } catch (Exception e) {
            System.out.println("Admin verification failed for user ID " + userId + ": " + e.getMessage());
            return false;
        } finally {
            userDatabase.disconnectDb();
        }
    }
    
    @Override
    public boolean isAdmin(String email) {
        if (email == null || email.trim().isEmpty()) {
            System.out.println("Admin verification failed: Email is null or empty");
            return false;
        }
        
        if (!userDatabase.connectDb()) {
            System.out.println("Admin verification failed: Cannot connect to database");
            return false;
        }
        
        try {
            UserRecords user = userDatabase.findUserByEmail(email);
            return isAdmin(user);
        } catch (Exception e) {
            System.out.println("Admin verification failed for email " + email + ": " + e.getMessage());
            return false;
        } finally {
            userDatabase.disconnectDb();
        }
    }
    
    @Override
    public String getVerificationMessage(UserRecords user) {
        if (user == null) {
            return "Admin verification failed: User is null";
        }
        
        String role = SecUtils.getUserRoleName(user.getUserTypeID());
        boolean isAdmin = user.getUserTypeID() == 1;
        
        return String.format("Admin verification for user %s (%s): %s - %s", 
                           user.getEmail(), 
                           role, 
                           isAdmin ? "GRANTED" : "DENIED",
                           isAdmin ? "User has admin privileges" : "User does not have admin privileges");
    }
} 