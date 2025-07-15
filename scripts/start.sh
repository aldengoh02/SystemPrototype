#!/bin/bash

# Exit on any error
set -e

echo "=== Starting Bookstore System Setup and Launch ==="

# Create necessary directories
echo "Creating directories..."
mkdir -p lib
mkdir -p target/classes

# Download dependencies if lib directory is empty
if [ -z "$(ls -A lib 2>/dev/null)" ]; then
    echo "=== Downloading Dependencies ==="
    
    curl -L "https://repo1.maven.org/maven2/jakarta/servlet/jakarta.servlet-api/5.0.0/jakarta.servlet-api-5.0.0.jar" -o lib/jakarta.servlet-api-5.0.0.jar

    curl -L "https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.28/mysql-connector-java-8.0.28.jar" -o lib/mysql-connector-java-8.0.28.jar

    curl -L "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.9.0/gson-2.9.0.jar" -o lib/gson-2.9.0.jar

    curl -L "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-server/11.0.9/jetty-server-11.0.9.jar" -o lib/jetty-server.jar
    curl -L "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-servlet/11.0.9/jetty-servlet-11.0.9.jar" -o lib/jetty-servlet.jar
    curl -L "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-util/11.0.9/jetty-util-11.0.9.jar" -o lib/jetty-util.jar
    curl -L "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-http/11.0.9/jetty-http-11.0.9.jar" -o lib/jetty-http.jar
    curl -L "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-io/11.0.9/jetty-io-11.0.9.jar" -o lib/jetty-io.jar

    # Additional Jetty dependencies
    curl -L "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-security/11.0.9/jetty-security-11.0.9.jar" -o lib/jetty-security.jar
    curl -L "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar" -o lib/slf4j-api.jar
    curl -L "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar" -o lib/slf4j-simple.jar

    # Spring Security BCrypt
    curl -L "https://repo1.maven.org/maven2/org/springframework/security/spring-security-crypto/6.2.1/spring-security-crypto-6.2.1.jar" -o lib/spring-security-crypto.jar

    echo "Dependencies downloaded successfully!"
else
    echo "Dependencies already present in lib/ directory"
fi

# Set classpath
CLASSPATH="lib/*:target/classes"

# Compile Java files
echo "=== Compiling Java Files ==="
find src/main/java -name "*.java" -print | xargs javac -cp "$CLASSPATH" -d target/classes
echo "Compilation complete!"

# Start the server
echo "=== Starting Jetty Server ==="
java -cp "$CLASSPATH" com.bookstore.JettyServer 