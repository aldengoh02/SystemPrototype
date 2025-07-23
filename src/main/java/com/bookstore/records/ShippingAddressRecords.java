package com.bookstore.records;

public class ShippingAddressRecords {
    private int addressID;
    private int userID;
    private String street;
    private String city;
    private String state;
    private String zipCode;

    public ShippingAddressRecords(int addressID, int userID, String street, String city, String state, String zipCode) {
        this.addressID = addressID;
        this.userID = userID;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
    }

    public int getAddressID() { return addressID; }
    public int getUserID() { return userID; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZipCode() { return zipCode; }
}
