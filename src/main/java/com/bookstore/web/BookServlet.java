/*
 * Rest API endpoint 
 * Currently connects to the database and supports search functionality
 * Supports current format of queries:
 * ?display=featured 
 * ?display=comingsoon
 * ?search=. . .
 * /bookId num ex api/books/10
 */

package com.bookstore.web;

import com.bookstore.db.BookActions;
import com.bookstore.db.BookDatabase;
import com.bookstore.db.BookRecords;
import com.bookstore.db.BookNotFoundException;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class BookServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /**
         * Handles the checkout calculation. This endpoint expects a POST request to `/api/books/calculate`.
         * The request body must be a JSON array of objects, where each object represents a cart item
         * and has an "id" (integer) and a "quantity" (integer).
         * 
         * Example JSON body:
         * [
         *   { "id": 1, "quantity": 2 },
         *   { "id": 5, "quantity": 1 }
         * ]
         * 
         * On success, it returns a JSON object with "subtotal", "salesTax", and "total".
         * On failure, it returns a JSON object with an "error" message.
         */
        BookDatabase bookDb = new BookDatabase();
        
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || !pathInfo.equals("/calculate")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid endpoint for POST request. Use /calculate.\"}");
            out.flush();
            return;
        }

        if (bookDb.connectDb()) {
            try {
                String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                CartItem[] cartItems = gson.fromJson(requestBody, CartItem[].class);

                Map<String, Double> result = BookActions.calculateCheckout(BookDatabase.getConnection(), cartItems);
                
                out.print(gson.toJson(result));

            } catch (BookNotFoundException e) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"" + e.getMessage() + "\"}");
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Error processing the request: " + e.getMessage() + "\"}");
                e.printStackTrace();
            } finally {
                bookDb.disconnectDb();
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to connect to the database\"}");
        }
        out.flush();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BookDatabase bookDb = new BookDatabase();

        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        PrintWriter out = resp.getWriter();

        if (bookDb.connectDb()) {
            String pathInfo = req.getPathInfo();

            if (pathInfo != null && !pathInfo.equals("/")) {
                try {
                    int bookId = Integer.parseInt(pathInfo.substring(1));
                    BookRecords book = BookActions.getBookById(BookDatabase.getConnection(), bookId);

                    if (book != null) {
                        out.print(gson.toJson(book));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\": \"Book not found\"}");
                    }
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid book ID\"}");
                }
            } else {
                String searchTerm = req.getParameter("search");
                String display = req.getParameter("display");
                ArrayList<BookRecords> books;

                if (searchTerm != null && !searchTerm.isEmpty()) {
                    books = BookActions.searchBooks(BookDatabase.getConnection(), searchTerm);
                } else if (display != null) {
                    switch (display) {
                        case "featured":
                            books = BookActions.getFeaturedBooks(BookDatabase.getConnection());
                            break;
                        case "comingsoon":
                            books = BookActions.getComingSoonBooks(BookDatabase.getConnection());
                            break;
                        default:
                            bookDb.loadResults();
                            books = bookDb.getResults();
                            break;
                    }
                } else {
                    bookDb.loadResults();
                    books = bookDb.getResults();
                }
                String booksJson = gson.toJson(books);
                out.print(booksJson);
            }
            bookDb.disconnectDb();
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to connect to the database\"}");
        }
        out.flush();
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Handle pre-flight CORS requests from the browser
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
} 