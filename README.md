# SystemPrototype
SWE Bookstore System Prototype
# currently uses servlets in order to run backend
# after having started the sql database, run mvn jetty:run in the root in order to start the back end
# If you have issues with maven follow the instructions present in the scripts folder; Would reccomend running all scripts from the root however in order to avoid problems
# run npm start in root in a separate terminal to start frontend
# backend is currently connected and images are loaded via public/img

# For any functionalities involving passwords you need to 
# mvn jetty:run -Dencryptionkey=testKey123456789012345678901234567890 -DSalt=testSalt1234567890



#Database Setup
#Running the Schema Files 
- Open MySql, then click on file and open 'SQL Script' and locate to your folder to the src/main/resources/BookStore_Schema/the.sql files to open them on the workbench and click the lightning icon to run it.
- Important: If the tables already exist in your workbench comment out the DROP TABLE Statement using '--' when running the sql script again.
- Update the 'db.properties' file with your database credentials
- Once the Database tables are set up, use the testDB to test you connection to the tables.
