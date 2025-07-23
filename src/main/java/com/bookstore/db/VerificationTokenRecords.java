package com.bookstore.db;

import java.sql.Timestamp;

public class VerificationTokenRecords {
    private int tokenId;
    private int userId;
    private String token;
    private Timestamp expiryDate;
    private String tokenType;

    public VerificationTokenRecords(int tokenId, int userId, String token, Timestamp expiryDate, String tokenType) {
        this.tokenId = tokenId;
        this.userId = userId;
        this.token = token;
        this.expiryDate = expiryDate;
        this.tokenType = tokenType;
    }

    //Getters and setters
    public int getTokenId() { return tokenId; }
    public void setTokenId(int tokenId) { this.tokenId = tokenId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Timestamp getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Timestamp expiryDate) { this.expiryDate = expiryDate; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
}
