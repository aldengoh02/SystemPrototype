@echo off

REM Compilation script for Windows systems

REM Create output directory
mkdir target\classes 2>nul

REM Set classpath with all dependencies
set CLASSPATH=lib\*;target\classes

REM Find and compile all Java files
echo Compiling Java files...
dir /s /b src\main\java\*.java > sources.txt
javac -cp "%CLASSPATH%" @sources.txt -d target\classes
del sources.txt

echo Compilation complete. Class files are in target\classes\ 