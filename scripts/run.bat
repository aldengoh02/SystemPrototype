@echo off

REM Set classpath with all dependencies and compiled classes
set CLASSPATH=lib\*;target\classes

REM Create a simple Java class to start Jetty
echo package com.bookstore; > src\main\java\com\bookstore\JettyServer.java
echo. >> src\main\java\com\bookstore\JettyServer.java
echo import org.eclipse.jetty.server.Server; >> src\main\java\com\bookstore\JettyServer.java
echo import org.eclipse.jetty.servlet.ServletContextHandler; >> src\main\java\com\bookstore\JettyServer.java
echo import org.eclipse.jetty.servlet.ServletHolder; >> src\main\java\com\bookstore\JettyServer.java
echo import jakarta.servlet.MultipartConfigElement; >> src\main\java\com\bookstore\JettyServer.java
echo import com.bookstore.web.BookServlet; >> src\main\java\com\bookstore\JettyServer.java
echo. >> src\main\java\com\bookstore\JettyServer.java
echo public class JettyServer { >> src\main\java\com\bookstore\JettyServer.java
echo     public static void main(String[] args) throws Exception { >> src\main\java\com\bookstore\JettyServer.java
echo         Server server = new Server(8080); >> src\main\java\com\bookstore\JettyServer.java
echo. >> src\main\java\com\bookstore\JettyServer.java
echo         ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS); >> src\main\java\com\bookstore\JettyServer.java
echo         context.setContextPath("/"); >> src\main\java\com\bookstore\JettyServer.java
echo         server.setHandler(context); >> src\main\java\com\bookstore\JettyServer.java
echo. >> src\main\java\com\bookstore\JettyServer.java
echo         // Configure the servlet >> src\main\java\com\bookstore\JettyServer.java
echo         ServletHolder servletHolder = new ServletHolder(new BookServlet()); >> src\main\java\com\bookstore\JettyServer.java
echo         servletHolder.getRegistration().setMultipartConfig( >> src\main\java\com\bookstore\JettyServer.java
echo             new MultipartConfigElement(System.getProperty("java.io.tmpdir")) >> src\main\java\com\bookstore\JettyServer.java
echo         ); >> src\main\java\com\bookstore\JettyServer.java
echo. >> src\main\java\com\bookstore\JettyServer.java
echo         // Add the BookServlet >> src\main\java\com\bookstore\JettyServer.java
echo         context.addServlet(servletHolder, "/api/books/*"); >> src\main\java\com\bookstore\JettyServer.java
echo. >> src\main\java\com\bookstore\JettyServer.java
echo         // Start the server >> src\main\java\com\bookstore\JettyServer.java
echo         try { >> src\main\java\com\bookstore\JettyServer.java
echo             server.start(); >> src\main\java\com\bookstore\JettyServer.java
echo             System.out.println("Server started on port 8080"); >> src\main\java\com\bookstore\JettyServer.java
echo             server.join(); >> src\main\java\com\bookstore\JettyServer.java
echo         } catch (Exception e) { >> src\main\java\com\bookstore\JettyServer.java
echo             e.printStackTrace(); >> src\main\java\com\bookstore\JettyServer.java
echo             System.exit(1); >> src\main\java\com\bookstore\JettyServer.java
echo         } >> src\main\java\com\bookstore\JettyServer.java
echo     } >> src\main\java\com\bookstore\JettyServer.java
echo } >> src\main\java\com\bookstore\JettyServer.java

REM Compile the server class
javac -cp "%CLASSPATH%" src\main\java\com\bookstore\JettyServer.java -d target\classes

REM Run the server
java -cp "%CLASSPATH%" com.bookstore.JettyServer 