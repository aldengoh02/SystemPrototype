/*
 * Rest API endpoint 
 * Currently connects to the database and supports search functionality
 */

package com.bookstore.web;

import com.bookstore.db.BookActions;
import com.bookstore.db.BookDatabase;
import com.bookstore.db.BookRecords;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class BookServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BookDatabase bookDb = new BookDatabase();

        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        PrintWriter out = resp.getWriter();

        if (bookDb.connectDb()) {
            String searchTerm = req.getParameter("search");
            ArrayList<BookRecords> books;
            if (searchTerm != null && !searchTerm.isEmpty()) {
                books = BookActions.searchBooks(BookDatabase.getConnection(), searchTerm);
            } else {
                bookDb.loadResults();
                books = bookDb.getResults();
            }
            String booksJson = gson.toJson(books);
            out.print(booksJson);
            bookDb.disconnectDb();
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to connect to the database\"}");
        }
        out.flush();
    }
} 