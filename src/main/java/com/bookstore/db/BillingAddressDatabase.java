package com.bookstore.db;

import java.sql.*;
import java.util.*;
import java.io.InputStream;
import java.util.Properties;
import com.bookstore.records.BillingAddressRecords;

public class BillingAddressDatabase {
    private Connection connection;
    private ArrayList<BillingAddressRecords> results;
    private ResultSet rs;
    Boolean connected = false;

    public BillingAddressDatabase() {
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

    public ArrayList<BillingAddressRecords> getResults() {
        return results;
    }

    public String addAddress(BillingAddressRecords addr) {
        String query = "INSERT INTO BillingAddress (street, city, state, zipCode) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, addr.getStreet());
            ps.setString(2, addr.getCity());
            ps.setString(3, addr.getState());
            ps.setString(4, addr.getZipCode());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Billing Address Added.";
    }

    public String updateBillingAddress(BillingAddressRecords addr) {
        String query = "UPDATE BillingAddress SET street=?, city=?, state=?, zipCode=? WHERE addressID=?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, addr.getStreet());
            ps.setString(2, addr.getCity());
            ps.setString(3, addr.getState());
            ps.setString(4, addr.getZipCode());
            ps.setInt(5, addr.getAddressID());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Billing Address Updated.";
    }

    public String loadResults() {
        results.clear();
        try {
            Statement stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * FROM BillingAddress");
            while (rs.next()) {
                BillingAddressRecords addr = new BillingAddressRecords(
                        rs.getInt("addressID"),
                        rs.getString("street"),
                        rs.getString("city"),
                        rs.getString("state"),
                        rs.getString("zipCode")
                );
                results.add(addr);
            }
            return "Billing Addresses Loaded.";
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public String deleteAddress(int addressID) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM BillingAddress WHERE addressID=?");
            ps.setInt(1, addressID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Billing Address Deleted.";
    }

    public BillingAddressRecords findByAddressID(int addressID) {
        try {
            String query = "SELECT * FROM BillingAddress WHERE addressID = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, addressID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new BillingAddressRecords(
                    rs.getInt("addressID"),
                    rs.getString("street"),
                    rs.getString("city"),
                    rs.getString("state"),
                    rs.getString("zipCode")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
