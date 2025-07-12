package com.bookstore;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import jakarta.servlet.MultipartConfigElement;
import com.bookstore.web.BookServlet;

public class JettyServer {
    public static void main(String[] args) throws Exception {
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
