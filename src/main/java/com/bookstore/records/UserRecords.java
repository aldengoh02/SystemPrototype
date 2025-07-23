package com.bookstore.records;

public class UserRecords {
    private int userID;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private String status;
    private boolean enrollForPromotions;
    private int userTypeID;

    //Constructor
    public UserRecords(int userID, String firstName, String lastName, String email, String password,
                       String phone, String status, boolean enrollForPromotions, int userTypeID) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.status = status;
        this.enrollForPromotions = enrollForPromotions;
        this.userTypeID = userTypeID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isEnrollForPromotions() {
        return enrollForPromotions;
    }

    public void setEnrollForPromotions(boolean enrollForPromotions) {
        this.enrollForPromotions = enrollForPromotions;
    }

    public int getUserTypeID() {
        return userTypeID;
    }

    public void setUserTypeID(int userTypeID) {
        this.userTypeID = userTypeID;
    }


}

