#!/bin/bash
# script I wrote for initializing all of the database
# tables since I was changing them often enough
# can be used in lieu of mySQL gui 
# here for others since it might be useful too
# only works on unix
# assumes existence of db.properties file
# uses a relative path so you must run this from the root rather than from the scripts folder itself!
# I am aware it is a security issue but it does not matter for a school project like this
# gets user and password using grep
# echos are for debugging purposes
set -x

USR=$(grep "^db.username=" src/main/resources/db.properties | cut -d'=' -f2)
DBPass=$(grep "^db.password=" src/main/resources/db.properties | cut -d'=' -f2)
echo "Username: $USR"
echo "Password: $DBPass"

# reinitialize database and populate initial stuff from tables via mysql commands
echo "initializing databse..."
mysql -u "$USR" -p"$DBPass" -e "DROP DATABASE IF EXISTS BookStore; CREATE DATABASE BookStore;"

# actual tables as of now

echo "init book table"
mysql -u "$USR" -p"$DBPass" BookStore < src/main/resources/BookStore_Schema/Books_Table.sql

echo "init User table"
mysql -u "$USR" -p"$DBPass" BookStore < src/main/resources/BookStore_Schema/User_table.sql
echo "init BillingAddress table"
mysql -u "$USR" -p"$DBPass" BookStore < src/main/resources/BookStore_Schema/BillingAddress_Table.sql
echo "init PaymentCard table"
mysql -u "$USR" -p"$DBPass" BookStore < src/main/resources/BookStore_Schema/PaymentCard_Table.sql
echo "init ShippingAdress"
mysql -u "$USR" -p"$DBPass" BookStore < src/main/resources/BookStore_Schema/ShippingAddress_Table.sql
echo "init CartTable"
mysql -u "$USR" -p"$DBPass" BookStore < src/main/resources/BookStore_Schema/Cart_Table.sql
echo "init PromotionTable"
mysql -u "$USR" -p"$DBPass" BookStore < src/main/resources/BookStore_Schema/Promotion_Table.sql
echo "init OrdersTable"
mysql -u "$USR" -p"$DBPass" BookStore < src/main/resources/BookStore_Schema/Orders_Table.sql
echo "init TransactionTable"
mysql -u "$USR" -p"$DBPass" BookStore < src/main/resources/BookStore_Schema/Transaction_Table.sql
echo "init VerificationTable"
mysql -u "$USR" -p"$DBPass" BookStore < src/main/resources/BookStore_Schema/VerificationToken.sql
echo "init AlterUserTable"
mysql -u "$USR" -p"$DBPass" BookStore < src/main/resources/BookStore_Schema/AlterUserTable.sql

# show all tables that have been initialized.
mysql -u "$USR" -p"$DBPass" -e "USE BookStore; SHOW TABLES;"

