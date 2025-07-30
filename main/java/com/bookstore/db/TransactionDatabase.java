package com.bookstore.db;

import java.sql.*;
import java.util.*;
import java.io.InputStream;
import java.util.Properties;
import com.bookstore.records.TransactionRecords;

public class TransactionDatabase {

    private Connection connection;
    private ArrayList<TransactionRecords> results;
    private ResultSet rs;
    Boolean connected = false;

    // Constructor
    public TransactionDatabase() {
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
    public ArrayList<TransactionRecords> getResults() {
        return results;
    }
    public void setResults(ArrayList<TransactionRecords> results) {
        this.results = results;
    }

    // Add new Transaction
    public String addTransaction(TransactionRecords transaction) {
        String query = "INSERT INTO transaction (orderID, bookID, quantity) VALUES (?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, transaction.getOrderID());
            ps.setInt(2, transaction.getBookID());
            ps.setInt(3, transaction.getQuantity());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Transaction Added.";
    }

    // Load all Transactions
    public String loadResults() {
        results.clear();
        try {
            Statement stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * FROM transaction");
            while (rs.next()) {
                TransactionRecords transaction = new TransactionRecords(
                        rs.getInt("transactionID"),
                        rs.getInt("orderID"),
                        rs.getInt("bookID"),
                        rs.getInt("quantity")
                );
                results.add(transaction);
            }
            return "Transactions Loaded.";
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    // Update Transaction
    public String updateTransaction(TransactionRecords transaction) {
        String query = "UPDATE transaction SET orderID=?, bookID=?, quantity=? WHERE transactionID=?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, transaction.getOrderID());
            ps.setInt(2, transaction.getBookID());
            ps.setInt(3, transaction.getQuantity());
            ps.setInt(4, transaction.getTransactionID());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Transaction Updated.";
    }

    // Delete Transaction by ID
    public String deleteTransaction(int transactionID) {
        String query = "DELETE FROM transaction WHERE transactionID=?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, transactionID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Transaction Deleted.";
    }
}
