package com.bookstore.records;

import java.sql.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class BookRecords {
    private int id;
    private String isbn;
    private String category;
    private String author;
    private String title;
    private String coverImage;
    private String edition;
    private String publisher;
    private int publicationYear;
    private int quantityInStock;
    private int minThreshold;
    private double buyingPrice;
    private double sellingPrice;
    private float rating;
    private boolean featured;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date releaseDate;
    
    private String description;

    //Constructor
    public BookRecords(int id, String isbn, String category, String author, String title, String coverImage,
                       String edition, String publisher, int publicationYear, int quantityInStock, int minThreshold,
                       double buyingPrice, double sellingPrice, float rating, boolean featured, Date releaseDate, String description) {
        this.id = id;
        this.isbn = isbn;
        this.category = category;
        this.author = author;
        this.title = title;
        this.coverImage = coverImage;
        this.edition = edition;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.quantityInStock = quantityInStock;
        this.minThreshold = minThreshold;
        this.buyingPrice = buyingPrice;
        this.sellingPrice = sellingPrice;
        this.rating = rating;
        this.featured = featured;
        this.releaseDate = releaseDate;
        this.description = description;
    }

    /**
     * Checks if the book is considered "coming soon" by comparing its release date to the current date.
     * Currently not in use as method in @BookActions does so for the entire list of datab
     * but intended for use with a single book 
     * possibly implemented later or in book details page
     * @return true if the release date is in the future, false otherwise.
     **/
    public boolean isComingSoon() {
       
        if (this.releaseDate == null) { // release date not confirmed == not known, return false
            return false;
        }
       
        return this.releaseDate.after(new java.util.Date()); // book is coming soon if release after present
    }
    

    //Getters and setters for all fields
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverImage() {
        return coverImage;
    }
    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getEdition() {
        return edition;
    }
    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getPublisher() {
        return publisher;
    }
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getPublicationYear() {
        return publicationYear;
    }
    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }
    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public int getMinThreshold() {
        return minThreshold;
    }
    public void setMinThreshold(int minThreshold) {
        this.minThreshold = minThreshold;
    }

    public double getBuyingPrice() {
        return buyingPrice;
    }
    public void setBuyingPrice(double buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }
    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public float getRating() {
        return rating;
    }
    public void setRating(float rating) {
        this.rating = rating;
    }

    public boolean isFeatured() {
        return featured;
    }
    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}