#!/bin/bash

# Detect OS and set classpath separator
case "$(uname -s)" in
    CYGWIN*|MINGW*|MSYS*|Windows*)
        SEPARATOR=";"
        ;;
    *)
        SEPARATOR=":"
        ;;
esac

# Set classpath with all dependencies and compiled classes
CLASSPATH="lib/*:target/classes"

# Run the server (no encryption keys needed)
echo "Starting Jetty Server..."
java -cp "$CLASSPATH" com.bookstore.JettyServer 