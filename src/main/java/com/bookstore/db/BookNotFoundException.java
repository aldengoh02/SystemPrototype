package com.bookstore.db;

/**
 * Custom exception to be thrown when a book with a specific ID cannot be found in the database.
 * Not applicable for current prototype however
 */
public class BookNotFoundException extends Exception {
    public BookNotFoundException(String message) {
        super(message);
    }
} 