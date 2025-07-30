package com.bookstore.db;

import java.sql.*;
import java.util.*;
import java.io.InputStream;
import java.util.Properties;
import com.bookstore.records.OrdersRecords;

public class OrdersDatabase {

    private Connection connection;
    private ArrayList<OrdersRecords> results;
    private ResultSet rs;
    Boolean connected = false;

    // Constructor
    public OrdersDatabase() {
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
    public ArrayList<OrdersRecords> getResults() {
        return results;
    }
    public void setResults(ArrayList<OrdersRecords> results) {
        this.results = results;
    }

    // Add new Order
    public String addOrder(OrdersRecords order) {
        String query = "INSERT INTO orders (userID, cardID, promoID, grandTotal, orderDateTime) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, order.getUserID());
            ps.setInt(2, order.getCardID());

            if (order.getPromoID() != null) {
                ps.setInt(3, order.getPromoID());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setDouble(4, order.getGrandTotal());
            ps.setTimestamp(5, order.getOrderDateTime());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Order Added.";
    }

    // Load all Orders
    public String loadResults() {
        results.clear();
        try {
            Statement stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * FROM orders");
            while (rs.next()) {
                OrdersRecords order = new OrdersRecords(
                        rs.getInt("orderID"),
                        rs.getInt("userID"),
                        rs.getInt("cardID"),
                        rs.getInt("promoID"),
                        rs.getDouble("grandTotal"),
                        rs.getTimestamp("orderDateTime")
                );
                results.add(order);
            }
            return "Orders Loaded.";
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    // Update Order
    public String updateOrder(OrdersRecords order) {
        String query = "UPDATE orders SET userID=?, cardID=?, promoID=?, grandTotal=?, orderDateTime=? WHERE orderID=?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, order.getUserID());
            ps.setInt(2, order.getCardID());

            if (order.getPromoID() != null) {
                ps.setInt(3, order.getPromoID());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setDouble(4, order.getGrandTotal());
            ps.setTimestamp(5, order.getOrderDateTime());
            ps.setInt(6, order.getOrderID());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Order Updated.";
    }

    // Delete Order by ID
    public String deleteOrder(int orderID) {
        String query = "DELETE FROM orders WHERE orderID=?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, orderID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Order Deleted.";
    }
}
