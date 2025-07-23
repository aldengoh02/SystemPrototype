package com.bookstore.db;

import java.sql.*;
import java.util.*;
import java.io.InputStream;
import java.util.Properties;
import com.bookstore.records.CartRecord;

public class CartDatabase {

    private Connection connection;
    private ArrayList<CartRecord> results;
    private ResultSet rs;
    Boolean connected = false;

    // Constructor
    public CartDatabase() {
        super();
        this.results = new ArrayList<>();
    }

    // Connect to DB
    public boolean connectDb() {
        try {
            Properties props = new Properties();
            InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties");
            if (input == null) {
                System.out.println("Sorry, unable to find db.properties");
                return false;
            }
            props.load(input);

            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            connected = true;
        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
            return false;
        }
        return true;
    }

    // Disconnect from DB
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

    // Getter and setter for results
    public ArrayList<CartRecord> getResults() {
        return results;
    }
    public void setResults(ArrayList<CartRecord> results) {
        this.results = results;
    }

    // Add new cart record
    public String addCartRecord(CartRecord cart) {
        String query = "INSERT INTO cart (userID, bookID, quantity) VALUES (?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, cart.getUserID());
            ps.setInt(2, cart.getBookID());
            ps.setInt(3, cart.getQuantity());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Cart record added.";
    }

    // Load all cart records
    public String loadResults() {
        results.clear();
        try {
            Statement stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * FROM cart");
            while (rs.next()) {
                CartRecord cart = new CartRecord(
                        rs.getInt("userID"),
                        rs.getInt("bookID"),
                        rs.getInt("quantity")
                );
                results.add(cart);
            }
            return "Cart records loaded.";
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    // Update cart record quantity
    public String updateCartRecord(CartRecord cart) {
        String query = "UPDATE cart SET quantity=? WHERE userID=? AND bookID=?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, cart.getQuantity());
            ps.setInt(2, cart.getUserID());
            ps.setInt(3, cart.getBookID());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Cart record updated.";
    }

    // Delete cart record
    public String deleteCartRecord(int userID, int bookID) {
        String query = "DELETE FROM cart WHERE userID=? AND bookID=?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userID);
            ps.setInt(2, bookID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Cart record deleted.";
    }
}
