package com.bookstore.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.io.InputStream;

public class VerificationTokenDatabase {

    private Connection connection;
    private ArrayList<VerificationTokenRecords> results;
    private ResultSet rs;
    Boolean connected = false;

    // Constructor
    public VerificationTokenDatabase() {
        super();
        this.results = new ArrayList<>();
    }

    //Connect to DB
    public boolean connectDb() {
        try {
            if (connection != null && !connection.isClosed()) {
                connected = true;
                return true;
            }
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

    //Disconnect from DB
    public boolean disconnectDb() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connected = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //Getter for results
    public ArrayList<VerificationTokenRecords> getResults() {
        return results;
    }

    public void setResults(ArrayList<VerificationTokenRecords> results) {
        this.results = results;
    }

    //Add new token
    public String addToken(VerificationTokenRecords token) {
        String query = "INSERT INTO VerificationToken (user_id, token, expiry_date, token_type) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, token.getUserId());
            ps.setString(2, token.getToken());
            ps.setTimestamp(3, token.getExpiryDate());
            ps.setString(4, token.getTokenType());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Token Added.";
    }

    //Load all tokens or Read
    public String loadResults() {
        results.clear();
        try {
            Statement stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * FROM VerificationToken");
            while (rs.next()) {
                VerificationTokenRecords token = new VerificationTokenRecords(
                        rs.getInt("token_id"),
                        rs.getInt("user_id"),
                        rs.getString("token"),
                        rs.getTimestamp("expiry_date"),
                        rs.getString("token_type")
                );
                results.add(token);
            }
            return "Tokens Loaded.";
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    //Update token
    public String updateToken(VerificationTokenRecords token) {
        String query = "UPDATE VerificationToken SET user_id=?, token=?, expiry_date=?, token_type=? WHERE token_id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, token.getUserId());
            ps.setString(2, token.getToken());
            ps.setTimestamp(3, token.getExpiryDate());
            ps.setString(4, token.getTokenType());
            ps.setInt(5, token.getTokenId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Token Updated.";
    }

    //Delete token by ID
    public String deleteToken(int tokenId) {
        String query = "DELETE FROM VerificationToken WHERE token_id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, tokenId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Token Deleted.";
    }

    //Find token by token string
    public VerificationTokenRecords findTokenByToken(String token) {
        String query = "SELECT * FROM VerificationToken WHERE token = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, token);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return new VerificationTokenRecords(
                    rs.getInt("token_id"),
                    rs.getInt("user_id"),
                    rs.getString("token"),
                    rs.getTimestamp("expiry_date"),
                    rs.getString("token_type")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Validate token (check if exists and not expired)
    public boolean isTokenValid(String token) {
        VerificationTokenRecords tokenRecord = findTokenByToken(token);
        if (tokenRecord == null) {
            return false;
        }
        
        // Check if token is expired
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return tokenRecord.getExpiryDate().after(now);
    }

    //Delete expired tokens
    public String deleteExpiredTokens() {
        String query = "DELETE FROM VerificationToken WHERE expiry_date < NOW()";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            int deletedCount = ps.executeUpdate();
            loadResults();
            return "Deleted " + deletedCount + " expired tokens.";
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    //Delete token by token string
    public String deleteTokenByToken(String token) {
        String query = "DELETE FROM VerificationToken WHERE token = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, token);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        loadResults();
        return "Token Deleted.";
    }
}
