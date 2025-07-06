package com.bookstore.web;

/**
 * Represents a single item in a shopping cart request.
 * Used for deserializing JSON from the frontend.
 */
public class CartItem {
    int id;
    int quantity;

    /*
     * Generic getters 
     */
    public int getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }
} 