#!/bin/bash

# DEPRECATED: This script is no longer needed!
# The new simple encryption in SecUtils.java doesn't require external keys.
# You can safely delete this script and any .env files.
#
# If you still need this for legacy purposes, the original functionality is below:

# Creates a .env file with encryption and salt keys for use in 
# decryption and encryption of data.
# generated key will be stored in a .env file in the root.
# I have not run this from any directory other than the root
# so would reccomend you do the same
# ie bash scripts/encryptionKeyGenerator.sh
# do not under any circumstances commit the actual .env to the repo.
# I am not exactly sure how to do this for windows for the time being
# so testing might have to be limited to unix systems only for the time being

echo " DEPRECATED: This script is no longer needed!"
echo " The bookstore now uses simple encryption that doesn't require external keys."
echo " You can use 'mvn jetty:run' directly without any parameters."
echo ""
echo " If you really need to generate keys for legacy purposes, continuing..."
echo ""

# Random secret key that is 32 bytes and base64 encoded
SECRETKEY=$(openssl rand -base64 32)

# 16 bytes base64 encoded
SALT=$(openssl rand -base64 16)

# Create or update .env file
ENV_FILE=".env"

echo "Generated keys:"
echo "encryptionKey=$SECRETKEY"
echo "Salt=$SALT"
echo ""

# Create or overwrite .env file as needed
echo " Creating .env file..."
cat > "$ENV_FILE" << EOF
# Encryption keys for SecUtils.java
encryptionKey=$SECRETKEY
Salt=$SALT

EOF
echo " Created .env file"

echo ""
echo " Encryption keys generated successfully!"
echo " (But remember, these are no longer needed for the current system)"
echo ""
