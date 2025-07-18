#!/bin/bash

# Exit on any error
set -e

echo "=== Starting Bookstore System Setup and Launch ==="

# Create necessary directories
echo "Creating directories..."
mkdir -p lib
mkdir -p target/classes

# Download dependencies if lib directory is empty
if [ ! "$(ls -A lib 2>/dev/null)" ]; then
    echo "=== Downloading Dependencies ==="
    ./scripts/downloadDeps.sh
fi

# Detect OS and set classpath separator
case "$(uname -s)" in
    CYGWIN*|MINGW*|MSYS*|Windows*)
        SEPARATOR=";"
        ;;
    *)
        SEPARATOR=":"
        ;;
esac

# Set classpath with all dependencies
CLASSPATH="lib/*${SEPARATOR}target/classes"

# Compile Java files
echo "=== Compiling Java Source Files ==="
echo "Using classpath: $CLASSPATH"
find src/main/java -name "*.java" -print | xargs javac -cp "$CLASSPATH" -d target/classes
echo "Compilation complete!"

# Start the server (no encryption keys needed)
echo "=== Starting Jetty Server ==="
java -cp "$CLASSPATH" com.bookstore.JettyServer 