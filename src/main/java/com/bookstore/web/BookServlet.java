/**
 * REST endpoint for most anything related to books
 * Following javadoc shows format and examples of endpoints
 * currently in use
 * 
 * Base URL: http://localhost:8080/api/books
 * 
 * Current endpoints:
 *  
 *  GET /api/books
 *    Returns: Array of all books with complete details including descriptions
 *    Example: GET http://localhost:8080/api/books
 * 
 *  GET /api/books?display=featured
 *    Returns: Array of featured books only
 *    Example: GET http://localhost:8080/api/books?display=featured
 * 
 *  GET /api/books?display=comingsoon
 *    Returns: Array of upcoming releases (books with future release dates)
 *    Example: GET http://localhost:8080/api/books?display=comingsoon
 * 
 *  GET /api/books?search={term}
 *    Returns: Array of books matching search term (searches title, author, category, ISBN)
 *    Example: GET http://localhost:8080/api/books?search=fantasy
 * 
 *  GET /api/books/{id}
 *    Returns: Single book object with complete details including description
 *    Example: GET http://localhost:8080/api/books/1
 * 
 *  POST /api/books/calculate
 *    Body: Array of {id: number, quantity: number}
 *    Returns: Checkout calculation with subtotal, tax, and total
 *    Example: POST http://localhost:8080/api/books/calculate
 *             Body: [{"id": 1, "quantity": 2}, {"id": 3, "quantity": 1}]
 * 
 *
 */

package com.bookstore.web;

import com.bookstore.db.BookActions;
import com.bookstore.db.BookDatabase;
import com.bookstore.records.BookRecords;
import com.bookstore.db.BookNotFoundException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class BookServlet extends AdminSecuredServlet {
    // Fix: Set Gson to use yyyy-MM-dd for SQL Date
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       
        setCorsHeaders(resp);
        
        // Verify admin access for book creation operations
        if (!verifyAdminAccess(req, resp)) {
            return;
        }
        
        BookDatabase bookDb = new BookDatabase();
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();
        
        if (bookDb.connectDb()) {
            try {
                String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                
                if (pathInfo != null && pathInfo.equals("/calculate")) {
                    // Handle checkout calculation
                    CartItem[] cartItems = gson.fromJson(requestBody, CartItem[].class);
                    Map<String, Double> result = BookActions.calculateCheckout(bookDb.getConnection(), cartItems);
                    out.print(gson.toJson(result));
                } else if (pathInfo == null || pathInfo.equals("/")) {
                    // Handle adding a new book
                    BookRecords newBook = gson.fromJson(requestBody, BookRecords.class);
                    String result = bookDb.addBook(newBook);
                    
                    if (result.equals("Book Added.")) {
                        resp.setStatus(HttpServletResponse.SC_CREATED);
                        out.print("{\"message\": \"" + result + "\"}");
                    } else {
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        out.print("{\"error\": \"" + result + "\"}");
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid endpoint for POST request. Use /calculate for checkout or root for adding books.\"}");
                }

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
        setCorsHeaders(resp);
        
        BookDatabase bookDb = new BookDatabase();
        PrintWriter out = resp.getWriter();

        if (bookDb.connectDb()) {
            String pathInfo = req.getPathInfo();

            if (pathInfo != null && !pathInfo.equals("/")) {
                try {
                    int bookId = Integer.parseInt(pathInfo.substring(1));
                    BookRecords book = BookActions.getBookById(bookDb.getConnection(), bookId);

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
                    books = BookActions.searchBooks(bookDb.getConnection(), searchTerm);
                } else if (display != null) {
                    switch (display) {
                        case "featured":
                            books = BookActions.getFeaturedBooks(bookDb.getConnection());
                            break;
                        case "comingsoon":
                            books = BookActions.getComingSoonBooks(bookDb.getConnection());
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
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
} 