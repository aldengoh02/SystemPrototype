/*
 * Data access layer for database
 * Currently only supports search functionality
 * performs an sql query to database and returns result
 */

package com.bookstore.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class BookActions {

    public static ArrayList<BookRecords> searchBooks(Connection connection, String searchTerm) {
        ArrayList<BookRecords> results = new ArrayList<>();
        String query = "SELECT * FROM books WHERE isbn LIKE ? OR category LIKE ? OR author LIKE ? OR title LIKE ? OR edition LIKE ? OR publisher LIKE ? OR CAST(publicationYear AS CHAR) LIKE ?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            String searchPattern = "%" + searchTerm + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            ps.setString(4, searchPattern);
            ps.setString(5, searchPattern);
            ps.setString(6, searchPattern);
            ps.setString(7, searchPattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BookRecords book = new BookRecords(
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
                        rs.getDate("releaseDate")
                );
                results.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
} 