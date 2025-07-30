package com.bookstore.db;

import java.sql.*;
import java.util.*;
import java.io.InputStream;
import java.util.Properties;
import com.bookstore.records.UserRecords;

public class UserDatabase {

    private Connection connection;
    private ArrayList<UserRecords> results;
    private ResultSet rs;
    Boolean connected = false;

    // Constructor
    public UserDatabase() {
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
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connected = false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Getter and setter for results
    public ArrayList<UserRecords> getResults() {
        return results;
    }

    public void setResults(ArrayList<UserRecords> results) {
        this.results = results;
    }

    // Add New User
    public String addUser(UserRecords user) {
        String query = "INSERT INTO Users (firstName, lastName, email, password, phone, status, enrollForPromotions, userTypeID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword()); // Should be hashed before insert
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getStatus());
            ps.setBoolean(7, user.isEnrollForPromotions());
            ps.setInt(8, user.getUserTypeID());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "User Added.";
    }

    // Find user by email regardless of status (for registration purposes)
    public UserRecords findUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        try {
            String query = "SELECT * FROM Users WHERE email = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, email.toLowerCase());
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new UserRecords(
                    rs.getInt("userID"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("phone"),
                    rs.getString("status"),
                    rs.getBoolean("enrollForPromotions"),
                    rs.getInt("userTypeID"),
                    rs.getObject("loginUserID") != null ? rs.getInt("loginUserID") : null
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // READ or Load All Users
    public String loadResults() {
        results.clear();
        try {
            Statement stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * FROM Users");
            while (rs.next()) {
                UserRecords user = new UserRecords(
                        rs.getInt("userID"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("phone"),
                        rs.getString("status"),
                        rs.getBoolean("enrollForPromotions"),
                        rs.getInt("userTypeID"),
                        rs.getObject("loginUserID") != null ? rs.getInt("loginUserID") : null
                );
                results.add(user);
            }
            return "Records Loaded.";
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    // Update User
    public String updateUser(UserRecords user) {
        String query = "UPDATE Users SET firstName=?, lastName=?, email=?, password=?, phone=?, status=?, enrollForPromotions=?, userTypeID=? WHERE userID=?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword()); // Should be hashed if changed
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getStatus());
            ps.setBoolean(7, user.isEnrollForPromotions());
            ps.setInt(8, user.getUserTypeID());
            ps.setInt(9, user.getUserID());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "User Updated.";
    }

    // Delete User by ID
    public String deleteUser(int userId) {
        String query = "DELETE FROM Users WHERE userID=?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "User Deleted.";
    }

    // Get users enrolled for promotions
    public ArrayList<UserRecords> getUsersEnrolledForPromotions() {
        ArrayList<UserRecords> promotionUsers = new ArrayList<>();
        try {
            String query = "SELECT * FROM Users WHERE enrollForPromotions = true AND status = 'active'";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                UserRecords user = new UserRecords(
                    rs.getInt("userID"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("phone"),
                    rs.getString("status"),
                    rs.getBoolean("enrollForPromotions"),
                    rs.getInt("userTypeID"),
                    rs.getObject("loginUserID") != null ? rs.getInt("loginUserID") : null
                );
                promotionUsers.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return promotionUsers;
    }

    // Getter for connection
    public Connection getConnection() {
        return connection;
    }
}
