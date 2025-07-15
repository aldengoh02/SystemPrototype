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
 *  Example response:
 * Book Object:
 * {
 *   "id": 1,
 *   "isbn": "9781234567001",
 *   "category": "Fantasy",
 *   "author": "Rebecca Yarros",
 *   "title": "Fourth Wing",
 *   "coverImage": "img/Fourth_Wing.png",
 *   "edition": "1st Edition",
 *   "publisher": "Entangled Publishing",
 *   "publicationYear": 2023,
 *   "quantityInStock": 18,
 *   "minThreshold": 3,
 *   "buyingPrice": 11.00,
 *   "sellingPrice": 19.99,
 *   "rating": 4.6,
 *   "featured": true,
 *   "releaseDate": "2023-04-05",
 *   "description": "Complete book description here..."
 * }

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
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BookServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private static final Logger LOGGER = Logger.getLogger(BookServlet.class.getName());

    // Method for checking availability of images but currently not used.
    // being kept for now but likely candidate for refactor
    private void logImageAvailability(BookRecords book, HttpServletRequest req) {
        if (book != null && book.getCoverImage() != null) {
            String imagePath = book.getCoverImage();
            LOGGER.info("Processing book: " + book.getTitle() + " (ID: " + book.getId() + ")");
            LOGGER.info("Image path from database: " + imagePath);

            /* Logger that was being used for checking file issues; Currently no longer 
            needed due to images being loaded via frontend

            // Try multiple possible paths
            String webappPath = getServletContext().getRealPath("/");
            LOGGER.info("Webapp root path: " + webappPath);
            
            // Try direct path from webapp root
            String path1 = webappPath + imagePath;
            File file1 = new File(path1);
            LOGGER.info("1. Full webapp path: " + path1 + " - Exists: " + file1.exists());
            
            // Try path relative to src/main/webapp
            String path2 = "src/main/" + imagePath;
            File file2 = new File(path2);
            LOGGER.info("2. Src path: " + path2 + " - Exists: " + file2.exists());
            
            // Try just the filename in webapp/img
            String path3 = webappPath + "img/" + new File(imagePath).getName();
            File file3 = new File(path3);
            LOGGER.info("3. Direct filename in webapp: " + path3 + " - Exists: " + file3.exists());
            */
        } else {
            LOGGER.warning("Book or cover image is null");
        }
    }

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
                        logImageAvailability(book, req);
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
                
                // Log image availability for all books
                for (BookRecords book : books) {
                    logImageAvailability(book, req);
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