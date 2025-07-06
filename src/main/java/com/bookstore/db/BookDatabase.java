package com.bookstore.db;

import java.sql.*;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;


public class BookDatabase {

    private static Connection connection;
    private ArrayList<BookRecords> results;
    private ResultSet rs;
    Boolean connected = false;

    //Constructor
    public BookDatabase() {
        super();
        this.results = new ArrayList<>();
    }

    //Connect to DB
    public boolean connectDb() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/BookStore?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", "root", "K55$durai");
            connected = true;
        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
            return false;
        }
        return true;
    }

    //Disconnect from DB
    public boolean disconnectDb() {
        try {
            connection.close();
            connected = false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Getter for connection
    public static Connection getConnection() {
        return connection;
    }

    //Getter and setter for results
    public ArrayList<BookRecords> getResults() {
        return results;
    }
    public void setResults(ArrayList<BookRecords> results) {
        this.results = results;
    }

    //Add New Book
    public String addBook(BookRecords book) {
        String query = "INSERT INTO books (isbn, category, author, title, coverImage, edition, publisher, publicationYear, quantityInStock, minThreshold, buyingPrice, sellingPrice, rating, featured, releaseDate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getCategory());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getTitle());
            ps.setString(5, book.getCoverImage());
            ps.setString(6, book.getEdition());
            ps.setString(7, book.getPublisher());
            ps.setInt(8, book.getPublicationYear());
            ps.setInt(9, book.getQuantityInStock());
            ps.setInt(10, book.getMinThreshold());
            ps.setDouble(11, book.getBuyingPrice());
            ps.setDouble(12, book.getSellingPrice());
            ps.setFloat(13, book.getRating());
            ps.setBoolean(14, book.isFeatured());
            ps.setDate(15, book.getReleaseDate());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Book Added.";
    }

    //READ or Load All Books
    public String loadResults() {
        results.clear();
        try {
            Statement stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * FROM books");
            while (rs.next()) {
                BookRecords book = new BookRecords(
                        rs.getInt("id"),
                        rs.getString("isbn"),
                        rs.getString("category"),
                        rs.getString("author"),
                        rs.getString("title"),
                        rs.getString("coverImage"),
                        rs.getString("edition"),
                        rs.getString("publisher"),
                        rs.getInt("publicationYear"),
                        rs.getInt("quantityInStock"),
                        rs.getInt("minThreshold"),
                        rs.getDouble("buyingPrice"),
                        rs.getDouble("sellingPrice"),
                        rs.getFloat("rating"),
                        rs.getBoolean("featured"),
                        rs.getDate("releaseDate")
                );
                results.add(book);
            }
            return "Records Loaded.";
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    //Update Book Information
    public String updateBook(BookRecords book) {
        String query = "UPDATE books SET isbn=?, category=?, author=?, title=?, coverImage=?, edition=?, publisher=?, publicationYear=?, quantityInStock=?, minThreshold=?, buyingPrice=?, sellingPrice=?, rating=?, featured=?, releaseDate=? WHERE id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getCategory());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getTitle());
            ps.setString(5, book.getCoverImage());
            ps.setString(6, book.getEdition());
            ps.setString(7, book.getPublisher());
            ps.setInt(8, book.getPublicationYear());
            ps.setInt(9, book.getQuantityInStock());
            ps.setInt(10, book.getMinThreshold());
            ps.setDouble(11, book.getBuyingPrice());
            ps.setDouble(12, book.getSellingPrice());
            ps.setFloat(13, book.getRating());
            ps.setBoolean(14, book.isFeatured());
            ps.setDate(15, book.getReleaseDate());
            ps.setInt(16, book.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Book Updated.";
    }

    //Delete Book by ID
    public String deleteBook(int bookId) {
        String query = "DELETE FROM books WHERE id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, bookId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Book Deleted.";
    }
}
