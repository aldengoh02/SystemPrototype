package com.bookstore.db;

import com.bookstore.SecUtils;
import java.sql.*;
import java.util.*;
import java.io.InputStream;
import java.util.Properties;
import com.bookstore.records.PaymentCardRecords;

public class PaymentCardDatabase {
    private Connection connection;
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

        /*
         * Adds card to database
         * >= 4 used just since it has higher robustness than just
         * checking if = 4
         */
    public String addCard(PaymentCardRecords card) {
        String countQuery = "SELECT COUNT(*) FROM PaymentCard WHERE userID = ?";
        try {
            PreparedStatement countPs = connection.prepareStatement(countQuery);
            countPs.setInt(1, card.getUserID());
            ResultSet rs = countPs.executeQuery();
            if (rs.next() && rs.getInt(1) >= 4) {
                return "User cannot have more than 4 payment cards.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }

        String query = "INSERT INTO PaymentCard (cardNo, userID, type, expirationDate, billingAddressID) VALUES (?, ?, ?, ?, ?)";
        try {
            String encryptedCardNo = SecUtils.encryptCreditCardSimple(card.getCardNo());
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, encryptedCardNo);
            ps.setInt(2, card.getUserID());
            ps.setString(3, card.getType());
            ps.setString(4, card.getExpirationDate());
            ps.setInt(5, card.getBillingAddressID());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Payment Card Added.";
    }

    public String updateCard(PaymentCardRecords card) {
        String query = "UPDATE PaymentCard SET cardNo=?, type=?, expirationDate=?, billingAddressID=? WHERE cardID=? AND userID=?";
        try {
            String encryptedCardNo = SecUtils.encryptCreditCardSimple(card.getCardNo());
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, encryptedCardNo);
            ps.setString(2, card.getType());
            ps.setString(3, card.getExpirationDate());
            ps.setInt(4, card.getBillingAddressID());
            ps.setInt(5, card.getCardID());
            ps.setInt(6, card.getUserID());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Payment Card Updated.";
    }

    public String loadResults() {
        results.clear();
        try {
            Statement stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * FROM PaymentCard");
            while (rs.next()) {
                PaymentCardRecords card = new PaymentCardRecords(
                        rs.getInt("cardID"),
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

    public String deleteCard(int cardID) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM PaymentCard WHERE cardID=?");
            ps.setInt(1, cardID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Payment Card Deleted.";
    }

    public ArrayList<PaymentCardRecords> getCardsByUserID(int userID) {
        ArrayList<PaymentCardRecords> userCards = new ArrayList<>();
        try {
            String query = "SELECT * FROM PaymentCard WHERE userID = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                PaymentCardRecords card = new PaymentCardRecords(
                    rs.getInt("cardID"),
                    rs.getString("cardNo"),
                    rs.getInt("userID"),
                    rs.getString("type"),
                    rs.getString("expirationDate"),
                    rs.getInt("billingAddressID")
                );
                userCards.add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userCards;
    }
}
