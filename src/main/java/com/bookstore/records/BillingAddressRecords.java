package com.bookstore.records;

public class BillingAddressRecords {
    private int addressID;
    private String street;
    private String city;
    private String state;
    private String zipCode;

    public BillingAddressRecords(int addressID, String street, String city, String state, String zipCode) {
        this.addressID = addressID;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
    }

    public int getAddressID() { return addressID; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZipCode() { return zipCode; }
}
