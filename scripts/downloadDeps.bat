@echo off

REM ============================
REM Dependency Download Script
REM ============================

REM Configure versions
set SERVLET_VER=5.0.0
set MYSQL_VER=8.0.28
set GSON_VER=2.9.0
set JETTY_VER=11.0.9
set SLF4J_VER=1.7.36
set SPRING_SEC_VER=6.2.1

REM Create lib directory
if not exist lib mkdir lib

echo Downloading dependencies...

REM Core dependencies
curl -L "https://repo1.maven.org/maven2/jakarta/servlet/jakarta.servlet-api/%SERVLET_VER%/jakarta.servlet-api-%SERVLET_VER%.jar" -o lib\jakarta.servlet-api-%SERVLET_VER%.jar
curl -L "https://repo1.maven.org/maven2/mysql/mysql-connector-java/%MYSQL_VER%/mysql-connector-java-%MYSQL_VER%.jar" -o lib\mysql-connector-java-%MYSQL_VER%.jar
curl -L "https://repo1.maven.org/maven2/com/google/code/gson/gson/%GSON_VER%/gson-%GSON_VER%.jar" -o lib\gson-%GSON_VER%.jar

REM Spring Security BCrypt
curl -L "https://repo1.maven.org/maven2/org/springframework/security/spring-security-crypto/%SPRING_SEC_VER%/spring-security-crypto-%SPRING_SEC_VER%.jar" -o lib\spring-security-crypto.jar

REM Jetty dependencies
curl -L "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-server/%JETTY_VER%/jetty-server-%JETTY_VER%.jar" -o lib\jetty-server.jar
curl -L "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-servlet/%JETTY_VER%/jetty-servlet-%JETTY_VER%.jar" -o lib\jetty-servlet.jar
curl -L "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-util/%JETTY_VER%/jetty-util-%JETTY_VER%.jar" -o lib\jetty-util.jar
curl -L "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-http/%JETTY_VER%/jetty-http-%JETTY_VER%.jar" -o lib\jetty-http.jar
curl -L "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-io/%JETTY_VER%/jetty-io-%JETTY_VER%.jar" -o lib\jetty-io.jar
curl -L "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-security/%JETTY_VER%/jetty-security-%JETTY_VER%.jar" -o lib\jetty-security.jar

REM Logging dependencies
curl -L "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/%SLF4J_VER%/slf4j-api-%SLF4J_VER%.jar" -o lib\slf4j-api.jar
curl -L "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/%SLF4J_VER%/slf4j-simple-%SLF4J_VER%.jar" -o lib\slf4j-simple.jar

echo Dependencies downloaded successfully.