# Script for initializing all of the database tables
# PowerShell equivalent of the bash script
# Can be used in lieu of MySQL GUI
# Assumes existence of db.properties file
# Uses a relative path so you must run this from the root rather than from the scripts folder itself!
# Security issue acknowledged but acceptable for school project

# Get user and password from properties file
$propsFile = "src/main/resources/db.properties"

if (Test-Path $propsFile) {
    $content = Get-Content $propsFile
    $USR = ($content | Select-String "^db.username=").ToString().Split("=")[1]
    $DBPass = ($content | Select-String "^db.password=").ToString().Split("=")[1]
    
    Write-Host "Username: $USR"
    Write-Host "Password: $DBPass"
    
    # Reinitialize database and populate initial stuff from tables via mysql commands
    Write-Host "initializing database..."
    & mysql -u "$USR" -p"$DBPass" -e "DROP DATABASE IF EXISTS BookStore; CREATE DATABASE BookStore;"
    
    # Actual tables as of now
    Write-Host "init book table"
    & mysql -u "$USR" -p"$DBPass" BookStore -e "source src/main/resources/BookStore_Schema/Books_Table.sql"
    Write-Host "init User table"
    & mysql -u "$USR" -p"$DBPass" BookStore -e "source src/main/resources/BookStore_Schema/User_table.sql"
    Write-Host "init BillingAddress table"
    & mysql -u "$USR" -p"$DBPass" BookStore -e "source src/main/resources/BookStore_Schema/BillingAddress_Table.sql"
    Write-Host "init PaymentCard table"
    & mysql -u "$USR" -p"$DBPass" BookStore -e "source src/main/resources/BookStore_Schema/PaymentCard_Table.sql"
    Write-Host "init ShippingAddress"
    & mysql -u "$USR" -p"$DBPass" BookStore -e "source src/main/resources/BookStore_Schema/ShippingAddress_Table.sql"
    Write-Host "init CartTable"
    & mysql -u "$USR" -p"$DBPass" BookStore -e "source src/main/resources/BookStore_Schema/Cart_Table.sql"
    Write-Host "init PromotionTable"
    & mysql -u "$USR" -p"$DBPass" BookStore -e "source src/main/resources/BookStore_Schema/Promotion_Table.sql"
    Write-Host "init OrdersTable"
    & mysql -u "$USR" -p"$DBPass" BookStore -e "source src/main/resources/BookStore_Schema/Orders_Table.sql"
    Write-Host "init TransactionTable"
    & mysql -u "$USR" -p"$DBPass" BookStore -e "source src/main/resources/BookStore_Schema/Transaction_Table.sql"
    
    # Show all tables that have been initialized
    & mysql -u "$USR" -p"$DBPass" -e "USE BookStore; SHOW TABLES;"
} else {
    Write-Error "db.properties file not found at $propsFile"
}

Read-Host "Press Enter to continue..."