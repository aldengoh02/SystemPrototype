@echo off

REM Set classpath
set CLASSPATH=lib\*;target\classes

REM Compile and run
javac -cp "%CLASSPATH%" src\main\java\com\bookstore\JettyServer.java -d target\classes
java -cp "%CLASSPATH%" com.bookstore.JettyServer