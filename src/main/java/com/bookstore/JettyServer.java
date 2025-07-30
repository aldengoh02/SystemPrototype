package com.bookstore;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import jakarta.servlet.MultipartConfigElement;
import com.bookstore.web.BookServlet;
import com.bookstore.web.EditUserServlet;
import com.bookstore.web.LoginServlet;
import com.bookstore.web.SecurityServlet;
import com.bookstore.web.RegistrationServlet;

/*
 * Main class for the Jetty server
 */
public class JettyServer {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        
        // Create handlers
        HandlerList handlers = new HandlerList();
        
        // Static file handler for main webapp directory
        ResourceHandler webappHandler = new ResourceHandler();
        webappHandler.setDirectoriesListed(true);
        webappHandler.setResourceBase("src/main/webapp");
        webappHandler.setWelcomeFiles(new String[]{"index.html"});
        
        // Static file handler for test directory (serves /test/* URLs)
        ResourceHandler testHandler = new ResourceHandler();
        testHandler.setDirectoriesListed(true);
        testHandler.setResourceBase("src/main/webapp/test");
        testHandler.setWelcomeFiles(new String[]{"index.html"});
        
        // Servlet context handler
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        
        // Configure the servlet
        ServletHolder servletHolder = new ServletHolder(new BookServlet());
        servletHolder.getRegistration().setMultipartConfig(
            new MultipartConfigElement(System.getProperty("java.io.tmpdir"))
        );
        
        // Add the BookServlet
        context.addServlet(servletHolder, "/api/books/*");
        
        // Add other servlets
        context.addServlet(new ServletHolder(new EditUserServlet()), "/api/user/*");
        context.addServlet(new ServletHolder(new LoginServlet()), "/api/auth/*");
        context.addServlet(new ServletHolder(new SecurityServlet()), "/api/security/*");
        context.addServlet(new ServletHolder(new RegistrationServlet()), "/api/register/*");
        
        // Add handlers to the list
        handlers.addHandler(webappHandler);
        handlers.addHandler(testHandler);
        handlers.addHandler(context);
        
        server.setHandler(handlers);
        
        // Start the server and show available endpoints
        try {
            server.start();
            System.out.println("Server started on port 8080");
            System.out.println("Available endpoints:");
            // Directory I added for testing 
            System.out.println("  - Static files: / (from src/main/webapp/ directory)");

            System.out.println("  - Books: /api/books/*");
            System.out.println("  - User: /api/user/*");
            System.out.println("  - Auth: /api/auth/*");
            System.out.println("  - Security: /api/security/*");
            System.out.println("  - Registration: /api/register/*");
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
