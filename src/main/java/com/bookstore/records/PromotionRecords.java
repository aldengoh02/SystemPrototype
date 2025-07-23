package com.bookstore.records;

import java.sql.Date;

public class PromotionRecords {
    private int promoID;
    private String promoCode;
    private float discount;  // changed to float to match SQL
    private Date startDate;
    private Date endDate;

    public PromotionRecords(int promoID, String promoCode, float discount, Date startDate, Date endDate) {
        this.promoID = promoID;
        this.promoCode = promoCode;
        this.discount = discount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and setters
    public int getPromoID() {
        return promoID;
    }
    public void setPromoID(int promoID) {
        this.promoID = promoID;
    }

    public String getPromoCode() {
        return promoCode;
    }
    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public float getDiscount() {
        return discount;
    }
    public void setDiscount(float discount) {
        this.discount = discount;
    }

    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
