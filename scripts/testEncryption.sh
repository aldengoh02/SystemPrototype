#!/bin/bash

# Tests functionality of current encryption capabilities
# Simply checks if the simple encryption works without external keys
# Run from root directory since it uses relative paths 

echo " Testing Simple Encryption..."
echo ""

echo " No external keys needed - using built-in simple encryption"
echo ""

# Create target directory if it doesn't exist
mkdir -p target/test-classes

# Compile the test class
echo " Compiling test class..."
javac -cp "target/classes:$(find lib -name "*.jar" | tr '\n' ':')" \
    -d target/test-classes \
    src/main/java/com/bookstore/records/UserRecords.java \
    src/main/java/com/bookstore/SecUtils.java \
    src/main/java/test/EncryptionTest.java

if [ $? -ne 0 ]; then
    echo " Compilation failed!"
    exit 1
fi

echo " Running encryption tests..."
echo ""

# Run the test (no encryption keys needed)
java -cp "target/classes:target/test-classes:$(find lib -name "*.jar" | tr '\n' ':')" \
    test.EncryptionTest

echo ""
echo " Test completed." 