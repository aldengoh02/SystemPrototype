@echo off

REM ============================
REM Java Compilation Script
REM ============================

REM Setup directories
if not exist target\classes mkdir target\classes

REM Setup classpath
set CLASSPATH=lib\*;target\classes

REM Compile all Java files
echo Building project...
dir /s /b src\main\java\*.java > sources.txt
javac -cp "%CLASSPATH%" @sources.txt -d target\classes
del sources.txt

echo Build complete.