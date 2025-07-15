package com.bookstore.db;

import java.sql.*;
import java.util.*;
import java.io.InputStream;
import java.util.Properties;

public class PaymentCardDatabase {
    private static Connection connection;
    private ArrayList<PaymentCardRecords> results;
    private ResultSet rs;
    Boolean connected = false;

    public PaymentCardDatabase() {
        this.results = new ArrayList<>();
    }

    public boolean connectDb() {
        try {
            Properties props = new Properties();
            InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties");
            if (input == null) return false;
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

    public ArrayList<PaymentCardRecords> getResults() {
        return results;
    }

    public String addCard(PaymentCardRecords card) {
        String query = "INSERT INTO PaymentCard (cardNo, userID, type, expirationDate, billingAddressID) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, card.getCardNo());
            ps.setInt(2, card.getUserID());
            ps.setString(3, card.getType());
            ps.setString(4, card.getExpirationDate());
            ps.setInt(5, card.getBillingAddressID());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Payment Card Added.";
    }

    public String loadResults() {
        results.clear();
        try {
            Statement stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * FROM PaymentCard");
            while (rs.next()) {
                PaymentCardRecords card = new PaymentCardRecords(
                        rs.getString("cardNo"),
                        rs.getInt("userID"),
                        rs.getString("type"),
                        rs.getString("expirationDate"),
                        rs.getInt("billingAddressID")
                );
                results.add(card);
            }
            return "Payment Cards Loaded.";
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public String deleteCard(String cardNo) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM PaymentCard WHERE cardNo=?");
            ps.setString(1, cardNo);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Payment Card Deleted.";
    }
}
