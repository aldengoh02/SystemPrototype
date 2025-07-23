package com.bookstore.records;

public class TransactionRecords {
    private int transactionID;
    private int orderID;
    private int bookID;
    private int quantity;

    // Constructor
    public TransactionRecords(int transactionID, int orderID, int bookID, int quantity) {
        this.transactionID = transactionID;
        this.orderID = orderID;
        this.bookID = bookID;
        this.quantity = quantity;
    }

    // Getters and Setters
    public int getTransactionID() {
        return transactionID;
    }
    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    public int getOrderID() {
        return orderID;
    }
    public void setOrderID(int orderID) {
        this.orderID = orderID;
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
