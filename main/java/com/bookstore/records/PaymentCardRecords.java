package com.bookstore.records;

public class PaymentCardRecords {
    private int cardID;
    private String cardNo;
    private int userID;
    private String type;
    private String expirationDate;
    private int billingAddressID;

    public PaymentCardRecords(int cardID, String cardNo, int userID, String type, String expirationDate, int billingAddressID) {
        this.cardID = cardID;
        this.cardNo = cardNo;
        this.userID = userID;
        this.type = type;
        this.expirationDate = expirationDate;
        this.billingAddressID = billingAddressID;
    }

    public int getCardID() { return cardID; }
    public String getCardNo() { return cardNo; }
    public int getUserID() { return userID; }
    public String getType() { return type; }
    public String getExpirationDate() { return expirationDate; }
    public int getBillingAddressID() { return billingAddressID; }
}
