@echo off
REM Script for initializing all of the database tables
REM Windows batch equivalent of the bash script
REM Can be used in lieu of MySQL GUI
REM Assumes existence of db.properties file
REM Uses a relative path so you must run this from the root rather than from the scripts folder itself!
REM Security issue acknowledged but acceptable for school project

REM Get user and password using findstr (Windows equivalent of grep)
for /f "tokens=2 delims==" %%a in ('findstr "^db.username=" src\main\resources\db.properties') do set USR=%%a
for /f "tokens=2 delims==" %%a in ('findstr "^db.password=" src\main\resources\db.properties') do set DBPass=%%a

echo Username: %USR%
echo Password: %DBPass%

REM Reinitialize database and populate initial stuff from tables via mysql commands
echo initializing database...
mysql -u "%USR%" -p"%DBPass%" -e "DROP DATABASE IF EXISTS BookStore; CREATE DATABASE BookStore;"

REM Actual tables as of now
echo init book table
mysql -u "%USR%" -p"%DBPass%" BookStore < src\main\resources\BookStore_Schema\Books_Table.sql
echo init User table
mysql -u "%USR%" -p"%DBPass%" BookStore < src\main\resources\BookStore_Schema\User_table.sql
echo init BillingAddress table
mysql -u "%USR%" -p"%DBPass%" BookStore < src\main\resources\BookStore_Schema\BillingAddress_Table.sql
echo init PaymentCard table
mysql -u "%USR%" -p"%DBPass%" BookStore < src\main\resources\BookStore_Schema\PaymentCard_Table.sql
echo init ShippingAddress
mysql -u "%USR%" -p"%DBPass%" BookStore < src\main\resources\BookStore_Schema\ShippingAddress_Table.sql
echo init CartTable
mysql -u "%USR%" -p"%DBPass%" BookStore < src\main\resources\BookStore_Schema\Cart_Table.sql
echo init PromotionTable
mysql -u "%USR%" -p"%DBPass%" BookStore < src\main\resources\BookStore_Schema\Promotion_Table.sql
echo init OrdersTable
mysql -u "%USR%" -p"%DBPass%" BookStore < src\main\resources\BookStore_Schema\Orders_Table.sql
echo init TransactionTable
mysql -u "%USR%" -p"%DBPass%" BookStore < src\main\resources\BookStore_Schema\Transaction_Table.sql

REM Show all tables that have been initialized
mysql -u "%USR%" -p"%DBPass%" -e "USE BookStore; SHOW TABLES;"

pause