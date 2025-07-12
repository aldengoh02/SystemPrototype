@echo off

REM Set classpath
set CLASSPATH=lib\*;target\classes

REM Check if JettyServer.java exists
if exist src\main\java\com\bookstore\JettyServer.java (
    echo Using existing JettyServer.java
) else (
    echo Creating JettyServer.java from template...
    mkdir src\main\java\com\bookstore 2>nul
    copy scripts\JettyServer.template src\main\java\com\bookstore\JettyServer.java
)

REM Compile and run
javac -cp "%CLASSPATH%" src\main\java\com\bookstore\JettyServer.java -d target\classes
java -cp "%CLASSPATH%" com.bookstore.JettyServer