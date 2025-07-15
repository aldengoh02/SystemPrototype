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
USR=$(grep "^db.username=" src/main/resources/db.properties | cut -d'=' -f2)
DBPass=$(grep "^db.password=" src/main/resources/db.properties | cut -d'=' -f2)
echo "Username: $USR"
echo "Password: $DBPass"

# reinitialize database and populate initial stuff from tables via mysql commands
mysql -u "$user" -p"$DBPass" -e "DROP DATABASE IF EXISTS BookStore; CREATE DATABASE BookStore;"

# actual tables as of now
mysql -u "$user" -p"$DBPass" BookStore < src/main/resources/BookStore_Schema/Books_Table.sql
mysql -u "$user" -p"$DBPass" BookStore < src/main/resources/BookStore_Schema/User_table.sql
mysql -u "$user" -p"$DBPass" BookStore < src/main/resources/BookStore_Schema/BillingAddress_Table.sql
mysql -u "$user" -p"$DBPass" BookStore < src/main/resources/BookStore_Schema/PaymentCard_Table.sql
mysql -u "$user" -p"$DBPass" BookStore < src/main/resources/BookStore_Schema/ShippingAddress_Table.sql


# show all tables that have been initialized.
mysql -u "$user" -p"$DBPass" -e "USE BookStore; SHOW TABLES;"

