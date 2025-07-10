#!/bin/bash

# compilation script in case of not having or having issues with maven; 
# Create output directory
mkdir -p target/classes

# Set classpath with all dependencies
CLASSPATH="lib/*:target/classes"

# Find and compile all Java files
echo "Compiling Java files..."
find src/main/java -name "*.java" -print | xargs javac -cp "$CLASSPATH" -d target/classes

echo "Compilation complete. Class files are in target/classes/" 