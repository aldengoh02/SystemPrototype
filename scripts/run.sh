#!/bin/bash

# Detect OS and set classpath separator
case "$(uname -s)" in
    CYGWIN*|MINGW*|MSYS*|Windows*)
        SEPARATOR=";"
        ;;
    *)
        SEPARATOR=":"
        ;;
esac

# Set classpath with all dependencies and compiled classes
CLASSPATH="lib/*:target/classes"

# Create a simple Java class to start Jetty
cat > src/main/java/com/bookstore/JettyServer.java << 'EOL'
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
EOL

# Compile the server class
javac -cp "$CLASSPATH" src/main/java/com/bookstore/JettyServer.java -d target/classes

# Run the server
java -cp "$CLASSPATH" com.bookstore.JettyServer 