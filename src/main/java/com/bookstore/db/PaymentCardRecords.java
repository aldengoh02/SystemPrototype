package com.bookstore.db;

public class PaymentCardRecords {
    private String cardNo;
    private int userID;
    private String type;
    private String expirationDate;
    private int billingAddressID;

    public PaymentCardRecords(String cardNo, int userID, String type, String expirationDate, int billingAddressID) {
        this.cardNo = cardNo;
        this.userID = userID;
        this.type = type;
        this.expirationDate = expirationDate;
        this.billingAddressID = billingAddressID;
    }

    public String getCardNo() { return cardNo; }
    public int getUserID() { return userID; }
    public String getType() { return type; }
    public String getExpirationDate() { return expirationDate; }
    public int getBillingAddressID() { return billingAddressID; }
}
