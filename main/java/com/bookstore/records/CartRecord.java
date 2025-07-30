package com.bookstore.records;

public class CartRecord {
    private int userID;
    private int bookID;
    private int quantity;

    // Constructor
    public CartRecord(int userID, int bookID, int quantity) {
        this.userID = userID;
        this.bookID = bookID;
        this.quantity = quantity;
    }

    // Getters and setters
    public int getUserID() {
        return userID;
    }
    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getBookID() {
        return bookID;
    }
    public void setBookID(int bookID) {
        this.bookID = bookID;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
