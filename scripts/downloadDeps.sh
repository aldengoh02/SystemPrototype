#!/bin/bash

#downloads various dependencies needed for project if maven does not work

# Create lib directory if it doesn't exist
mkdir -p lib

# Download required JARs
echo "Downloading dependencies..."

# Download Jakarta Servlet API
curl -L "https://repo1.maven.org/maven2/jakarta/servlet/jakarta.servlet-api/5.0.0/jakarta.servlet-api-5.0.0.jar" -o lib/jakarta.servlet-api-5.0.0.jar

# Download MySQL Connector
curl -L "https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.28/mysql-connector-java-8.0.28.jar" -o lib/mysql-connector-java-8.0.28.jar

# Download Gson
curl -L "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.9.0/gson-2.9.0.jar" -o lib/gson-2.9.0.jar

# Download Spring Security BCrypt
curl -L "https://repo1.maven.org/maven2/org/springframework/security/spring-security-crypto/6.2.1/spring-security-crypto-6.2.1.jar" -o lib/spring-security-crypto.jar

# Download Java Dotenv
curl -L "https://repo1.maven.org/maven2/io/github/cdimascio/java-dotenv/5.2.2/java-dotenv-5.2.2.jar" -o lib/java-dotenv.jar

# Download Jetty 11 (Jakarta EE 9+ compatible)
curl -L "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-server/11.0.9/jetty-server-11.0.9.jar" -o lib/jetty-server.jar
curl -L "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-servlet/11.0.9/jetty-servlet-11.0.9.jar" -o lib/jetty-servlet.jar
curl -L "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-util/11.0.9/jetty-util-11.0.9.jar" -o lib/jetty-util.jar
curl -L "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-http/11.0.9/jetty-http-11.0.9.jar" -o lib/jetty-http.jar
curl -L "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-io/11.0.9/jetty-io-11.0.9.jar" -o lib/jetty-io.jar

# Additional Jetty dependencies needed for Jakarta EE 9+
curl -L "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-security/11.0.9/jetty-security-11.0.9.jar" -o lib/jetty-security.jar
curl -L "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar" -o lib/slf4j-api.jar
curl -L "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar" -o lib/slf4j-simple.jar

echo "Dependencies downloaded to lib/ directory" 