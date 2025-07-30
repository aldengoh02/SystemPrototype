package com.bookstore.web;

/**
 * Represents a single item in a shopping cart request.
 * Used for deserializing JSON from the frontend.
 * This class is now outdated and will be fully removed from both the frontend
 * and backend so delete it if you want to
 * will remove ability to see cart items currently until
 * backend solution is implemented
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