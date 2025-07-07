const express = require('express');
const mysql = require('mysql2');
const cors = require('cors');
const path = require('path');
const app = express();
const PORT = 5000;

app.use(cors());
app.use(express.json());

// Serve images from public/img directory
app.use('/img', express.static(path.resolve(__dirname, 'public', 'img')));

// MySQL connection
const db = mysql.createConnection({
    host: 'localhost',
    user: 'root',
    password: '1234',
    database: 'BookStore'
});

db.connect(err => {
    if (err) {
        console.error('❌ Database connection failed:', err.stack);
        return;
    }
    console.log('✅ Connected to MySQL database.');
});

// Get featured books
app.get('/api/books/featured', (req, res) => {
    const q = `SELECT id, isbn, title, author, sellingPrice AS price, 
               coverImage, category, rating, featured 
               FROM books WHERE featured = TRUE`;
    db.query(q, (err, results) => {
        if (err) {
            console.error('❌ Error fetching featured books:', err);
            return res.status(500).json({ error: err.message });
        }
        res.json(results);
    });
});

// Get coming soon books
app.get('/api/books/coming-soon', (req, res) => {
    const q = `SELECT id, isbn, title, author, sellingPrice AS price, 
               coverImage, category, rating 
               FROM books WHERE quantityInStock = 0`;
    db.query(q, (err, results) => {
        if (err) {
            console.error('❌ Error fetching coming soon books:', err);
            return res.status(500).json({ error: err.message });
        }
        res.json(results);
    });
});

// Health check endpoint
app.get('/', (req, res) => {
    res.send('✅ BookStore API is running.');
});

// Start server
app.listen(PORT, () => {
    console.log(`🚀 Server running at http://localhost:${PORT}`);
});