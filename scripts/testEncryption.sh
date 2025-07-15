#!/bin/bash

#Tests functionality of currenty encryption capabilities
# simply checks if keys are present and work
# run from root directory since it uses relative paths 
# I have no idea how to write this for windows sorry

echo " Testing Encryption Keys..."
echo ""

# If no env file, generate one automatically by running key gen
if [ ! -f ".env" ]; then
    echo "  .env file not found. Generating encryption keys..."
    echo ""
    ./scripts/encryptionKeyGenerator.sh
    echo ""
    echo " Encryption keys generated. Continuing with test..."
    echo ""
fi

# Check if .env contains required keys, regenerate if missing
if ! grep -q "encryptionKey=" .env || ! grep -q "Salt=" .env; then
    echo "  .env file is missing required keys. Regenerating..."
    echo ""
    ./scripts/encryptionKeyGenerator.sh
    echo ""
    echo " Encryption keys regenerated. Continuing with test..."
    echo ""
fi

echo " Found .env file with encryption keys"
echo ""

# Load environment variables from .env
export $(cat .env | grep -v '#' | xargs)

# Create target directory if it doesn't exist
mkdir -p target/test-classes

# Compile the test class
echo " Compiling test class..."
javac -cp "target/classes:$(find lib -name "*.jar" | tr '\n' ':')" \
    -d target/test-classes \
    src/main/java/test/EncryptionTest.java

if [ $? -ne 0 ]; then
    echo " Compilation failed!"
    exit 1
fi

echo " Running encryption tests..."
echo ""

# Run the test
java -cp "target/classes:target/test-classes:$(find lib -name "*.jar" | tr '\n' ':')" \
    test.EncryptionTest

echo ""
echo " Test completed." 