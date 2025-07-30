package com.bookstore.db;

import java.sql.*;
import java.util.*;
import java.io.InputStream;
import java.util.Properties;
import com.bookstore.records.ShippingAddressRecords;

public class ShippingAddressDatabase {
    private Connection connection;
    private ArrayList<ShippingAddressRecords> results;
    private ResultSet rs;
    Boolean connected = false;

    public ShippingAddressDatabase() {
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

    public ArrayList<ShippingAddressRecords> getResults() {
        return results;
    }

    public String addAddress(ShippingAddressRecords addr) {
        String query = "INSERT INTO ShippingAddress (userID, street, city, state, zipCode) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, addr.getUserID());
            ps.setString(2, addr.getStreet());
            ps.setString(3, addr.getCity());
            ps.setString(4, addr.getState());
            ps.setString(5, addr.getZipCode());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Shipping Address Added.";
    }

    public String updateAddress(ShippingAddressRecords addr) {
        String query = "UPDATE ShippingAddress SET userID=?, street=?, city=?, state=?, zipCode=? WHERE addressID=?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, addr.getUserID());
            ps.setString(2, addr.getStreet());
            ps.setString(3, addr.getCity());
            ps.setString(4, addr.getState());
            ps.setString(5, addr.getZipCode());
            ps.setInt(6, addr.getAddressID());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Shipping Address Updated.";
    }

    public String loadResults() {
        results.clear();
        try {
            Statement stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * FROM ShippingAddress");
            while (rs.next()) {
                ShippingAddressRecords addr = new ShippingAddressRecords(
                        rs.getInt("addressID"),
                        rs.getInt("userID"),
                        rs.getString("street"),
                        rs.getString("city"),
                        rs.getString("state"),
                        rs.getString("zipCode")
                );
                results.add(addr);
            }
            return "Shipping Addresses Loaded.";
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public String deleteAddress(int addressID) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM ShippingAddress WHERE addressID=?");
            ps.setInt(1, addressID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Address Deleted.";
    }

    public ShippingAddressRecords findFirstByUserID(int userID) {
        try {
            String query = "SELECT * FROM ShippingAddress WHERE userID = ? LIMIT 1";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new ShippingAddressRecords(
                    rs.getInt("addressID"),
                    rs.getInt("userID"),
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

    public ArrayList<ShippingAddressRecords> getAddressesByUserID(int userID) {
        ArrayList<ShippingAddressRecords> userAddresses = new ArrayList<>();
        try {
            String query = "SELECT * FROM ShippingAddress WHERE userID = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ShippingAddressRecords addr = new ShippingAddressRecords(
                    rs.getInt("addressID"),
                    rs.getInt("userID"),
                    rs.getString("street"),
                    rs.getString("city"),
                    rs.getString("state"),
                    rs.getString("zipCode")
                );
                userAddresses.add(addr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userAddresses;
    }

    public int insertAddress(int userID, String street, String city, String state, String zipCode) {
        int newAddressID = -1;
        try {
            String sql = "INSERT INTO ShippingAddress (userID, street, city, state, zipCode) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, userID);
            stmt.setString(2, street);
            stmt.setString(3, city);
            stmt.setString(4, state);
            stmt.setString(5, zipCode);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    newAddressID = rs.getInt(1);
                }
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadResults();
        return newAddressID;
    }
}
