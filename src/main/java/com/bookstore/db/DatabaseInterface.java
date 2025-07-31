package com.bookstore.db;

import java.sql.Connection;

/**
 * Common interface for all database classes in the bookstore system.
 * This interface defines the standard methods that all database classes must implement.
 */
public interface DatabaseInterface {
    
    /**
     * Connect to the database using properties from db.properties file
     * @return true if connection successful, false otherwise
     */
    boolean connectDb();
    
    /**
     * Disconnect from the database
     * @return true if disconnection successful, false otherwise
     */
    boolean disconnectDb();
    
    /**
     * Get the current database connection
     * @return the database connection object
     */
    Connection getConnection();
    
    /**
     * Check if currently connected to database
     * @return true if connected, false otherwise
     */
    default boolean isConnected() {
        return false; // Default implementation, should be overridden
    }
} 