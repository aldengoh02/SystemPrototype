package com.bookstore.proxy;

import com.bookstore.records.UserRecords;

/**
 * Proxy interface for admin verification operations.
 * This interface defines the contract for verifying admin privileges
 * before allowing access to admin-only functionality.
 */
public interface AdminVerificationProxy {
    
    /**
     * Verifies if a user has admin privileges
     * @param user The user to verify
     * @return true if user is an admin, false otherwise
     */
    boolean isAdmin(UserRecords user);
    
    /**
     * Verifies if a user has admin privileges by user ID
     * @param userId The user ID to verify
     * @return true if user is an admin, false otherwise
     */
    boolean isAdmin(int userId);
    
    /**
     * Verifies if a user has admin privileges by email
     * @param email The user email to verify
     * @return true if user is an admin, false otherwise
     */
    boolean isAdmin(String email);
    
    /**
     * Gets the admin verification message for logging/auditing
     * @param user The user being verified
     * @return A message describing the verification result
     */
    String getVerificationMessage(UserRecords user);
} 