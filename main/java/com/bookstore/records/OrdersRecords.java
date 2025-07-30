package com.bookstore.records;

import java.sql.Timestamp;

public class OrdersRecords {
    private int orderID;
    private int userID;
    private int cardID;       // payment card id, assumed INT
    private Integer promoID;  // promoID can be null, so use Integer (nullable)
    private double grandTotal;
    private Timestamp orderDateTime;

    // Constructor
    public OrdersRecords(int orderID, int userID, int cardID, Integer promoID, double grandTotal, Timestamp orderDateTime) {
        this.orderID = orderID;
        this.userID = userID;
        this.cardID = cardID;
        this.promoID = promoID;
        this.grandTotal = grandTotal;
        this.orderDateTime = orderDateTime;
    }

    // Getters and Setters
    public int getOrderID() {
        return orderID;
    }
    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public int getUserID() {
        return userID;
    }
    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getCardID() {
        return cardID;
    }
    public void setCardID(int cardID) {
        this.cardID = cardID;
    }

    public Integer getPromoID() {
        return promoID;
    }
    public void setPromoID(Integer promoID) {
        this.promoID = promoID;
    }

    public double getGrandTotal() {
        return grandTotal;
    }
    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public Timestamp getOrderDateTime() {
        return orderDateTime;
    }
    public void setOrderDateTime(Timestamp orderDateTime) {
        this.orderDateTime = orderDateTime;
    }
}
