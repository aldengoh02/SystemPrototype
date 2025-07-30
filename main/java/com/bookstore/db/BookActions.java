/*
 * Data access layer for database
 * Currently only supports search functionality
 * performs an sql query to database and returns result
 * Final deliverable will need for this section to be modified
 */

package com.bookstore.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.bookstore.web.CartItem;
import com.bookstore.records.BookRecords;

public class BookActions {

    public static ArrayList<BookRecords> searchBooks(Connection connection, String searchTerm) {
        ArrayList<BookRecords> results = new ArrayList<>();
        String query = "SELECT * FROM books WHERE isbn LIKE ? OR category LIKE ? OR author LIKE ? OR title LIKE ? OR edition LIKE ? OR publisher LIKE ? OR CAST(publicationYear AS CHAR) LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            String searchPattern = "%" + searchTerm + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            ps.setString(4, searchPattern);
            ps.setString(5, searchPattern);
            ps.setString(6, searchPattern);
            ps.setString(7, searchPattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToBookRecord(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static Map<String, Double> calculateCheckout(Connection connection, CartItem[] cartItems) throws SQLException, BookNotFoundException {
        double subtotal = 0.0;

        for (CartItem item : cartItems) {
            BookRecords book = getBookById(connection, item.getId());
            if (book != null) {
                subtotal += book.getSellingPrice() * item.getQuantity();
            } else {
                throw new BookNotFoundException("Book with ID " + item.getId() + " not found.");
            }
        }

        double total = subtotal; // No sales tax

        Map<String, Double> result = new HashMap<>();
        result.put("subtotal", subtotal);
        result.put("total", total);

        return result;
    }

    public static ArrayList<BookRecords> getFeaturedBooks(Connection connection) {
        ArrayList<BookRecords> results = new ArrayList<>();
        String query = "SELECT * FROM books WHERE featured = TRUE";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(mapRowToBookRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static ArrayList<BookRecords> getComingSoonBooks(Connection connection) {
        ArrayList<BookRecords> results = new ArrayList<>();
        String query = "SELECT * FROM books WHERE releaseDate > CURDATE()";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(mapRowToBookRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
    
    public static BookRecords getBookById(Connection connection, int id) {
        String query = "SELECT * FROM books WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBookRecord(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static BookRecords mapRowToBookRecord(ResultSet rs) throws SQLException {
        return new BookRecords(
                rs.getInt("id"),
                rs.getString("isbn"),
                rs.getString("category"),
                rs.getString("author"),
                rs.getString("title"),
                rs.getString("coverImage"),
                rs.getString("edition"),
                rs.getString("publisher"),
                rs.getInt("publicationYear"),
                rs.getInt("quantityInStock"),
                rs.getInt("minThreshold"),
                rs.getDouble("buyingPrice"),
                rs.getDouble("sellingPrice"),
                rs.getFloat("rating"),
                rs.getBoolean("featured"),
                rs.getDate("releaseDate"),
                rs.getString("description")
        );
    }
} 