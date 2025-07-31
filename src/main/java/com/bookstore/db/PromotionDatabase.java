package com.bookstore.db;

import java.sql.*;
import java.util.*;
import java.io.InputStream;
import java.util.Properties;
import com.bookstore.records.PromotionRecords;

public class PromotionDatabase implements DatabaseInterface {

    private Connection connection;
    private ArrayList<PromotionRecords> results;
    private ResultSet rs;
    Boolean connected = false;

    public PromotionDatabase() {
        super();
        this.results = new ArrayList<>();
    }

    @Override
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

    @Override
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

    @Override
    public Connection getConnection() {
        return connection;
    }
    
    @Override
    public boolean isConnected() {
        return connected;
    }
        public ArrayList<PromotionRecords> getResults() {
        return results;
    }
    public void setResults(ArrayList<PromotionRecords> results) {
        this.results = results;
    }



    public String addPromotion(PromotionRecords promo) {
        String query = "INSERT INTO promotion (promoCode, discount, startDate, endDate, pushed) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, promo.getPromoCode());
            ps.setFloat(2, promo.getDiscount());
            ps.setDate(3, promo.getStartDate());
            ps.setDate(4, promo.getEndDate());
            ps.setBoolean(5, promo.isPushed());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Promotion Added.";
    }

    public String loadResults() {
        results.clear();
        try {
            Statement stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * FROM promotion");
            while (rs.next()) {
                PromotionRecords promo = new PromotionRecords(
                        rs.getInt("promoID"),
                        rs.getString("promoCode"),
                        rs.getFloat("discount"),
                        rs.getDate("startDate"),
                        rs.getDate("endDate"),
                        rs.getBoolean("pushed")
                );
                results.add(promo);
            }
            return "Promotions Loaded.";
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public String updatePromotion(PromotionRecords promo) {
        String query = "UPDATE promotion SET promoCode=?, discount=?, startDate=?, endDate=?, pushed=? WHERE promoID=?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, promo.getPromoCode());
            ps.setFloat(2, promo.getDiscount());
            ps.setDate(3, promo.getStartDate());
            ps.setDate(4, promo.getEndDate());
            ps.setBoolean(5, promo.isPushed());
            ps.setInt(6, promo.getPromoID());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Promotion Updated.";
    }

    public String deletePromotion(int promoID) {
        String query = "DELETE FROM promotion WHERE promoID=?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, promoID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Promotion Deleted.";
    }

    public String updatePromotionPushedStatus(int promoID, boolean pushed) {
        String query = "UPDATE promotion SET pushed=? WHERE promoID=?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setBoolean(1, pushed);
            ps.setInt(2, promoID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Promotion push status updated.";
    }
}
