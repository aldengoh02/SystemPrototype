package com.bookstore;

import io.github.cdimascio.dotenv.Dotenv;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import jakarta.servlet.MultipartConfigElement;
import com.bookstore.web.BookServlet;
import com.bookstore.web.EditUserServlet;
import com.bookstore.web.LoginServlet;
import com.bookstore.web.SecurityServlet;

public class JettyServer {
    public static void main(String[] args) throws Exception {
        // Load environment variables from .env file
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        
        Server server = new Server(8080);
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

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
        
        // Start the server
        try {
            server.start();
            System.out.println("Server started on port 8080");
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
