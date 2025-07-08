// Imports the express framework because we need it to build the server
const express = require('express');
// Imported the  MySQL2 to talk to our database cuz we're using MySQL
const mysql = require('mysql2');
// Import CORS so our React app can talk to the server without catching  errors
const cors = require('cors');
// Import  the path module to handle file paths correctly
  const path = require('path');
// Created our express  instance
const app = express();
// Sets the port number to 5000 that is the localhost port number of the database
 const PORT = 5000;

// This will tells  the express to use the  CORS middleware so  that the frontend can  be connected
app.use(cors());
// Here will  tell express to parse JSON bodies
 app.use(express.json());

// Here will serve  the images from the created public/img directory when  it is requested
app.use('/img', express.static(path.resolve(__dirname, 'public', 'img')));

// Sets up our database connection with the  information below
    const db = mysql.createConnection({
    host: 'localhost', // The database is on same machine
    user: 'root', // Using root as the user 
    password: '1234', //  password for mysql . Super secure password obv lol
    database: 'BookStore' // Thid is the  name of our database
});

// Here it will try to  connect to the database and  also check for  any errors
db.connect(err => {
    //But,  If there's an errors, it will  logg it and  then bail out
      if (err) {
        console.error(' Database connection failed:', err.stack);
        return;
    }
    // If we get here,  thats means the connection  has worked  which is good.
    console.log(' Connected to MySQL database.');
});

// This endpoint  here gets the  featured books from our sql database
app.get('/api/books/featured', (req, res) => {
    // The sql query implemented to to select featured books where  the featured = TRUE
    const q = `SELECT id, isbn, title, author, sellingPrice AS price, 
               coverImage, category, rating, featured 
               FROM books WHERE featured = TRUE`;
    // Runs the query against our database
    db.query(q, (err, results) => {
        // If something went wrong, it will  log it and then  send an error response
        if (err) {
            console.error(' Error fetching featured books:', err);
            return res.status(500).json({ error: err.message });
        }
        // But  if it is  ssuccessful  then send back the results as JSON
        res.json(results);
    });
});

// This endpoint gets coming soon books when the quantity is 0
app.get('/api/books/coming-soon', (req, res) => {
    // Thee SQL query for  the books  that have  zero stock
    const q = `SELECT id, isbn, title, author, sellingPrice AS price, 
               coverImage, category, rating 
               FROM books WHERE quantityInStock = 0`;
    // implemented  and used to run the query
    db.query(q, (err, results) => {
        // This here will handlee the  errors if they happen
        if (err) {
            console.error(' Error fetching coming soon books:', err);
            return res.status(500).json({ error: err.message });
        }
        // Sends an  successful message that  with  some book  data
        res.json(results);
    });
});

// Essentially   Checks the  endpoint to see if API is alive or not 
app.get('/', (req, res) => {
    // Just  will send back a message  if running.
    res.send('  the BookStore API is running.');
});

// Starts to  listen for  the requests on our  port
app.listen(PORT, () => {
    // Logs a message so  that we know that  the server  has started and running.
    console.log(`  the server running at http://localhost:${PORT}`);
});