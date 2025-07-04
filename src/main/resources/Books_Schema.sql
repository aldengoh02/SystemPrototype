CREATE DATABASE IF NOT EXISTS BookStore;
USE BookStore;

DROP TABLE IF EXISTS books;

CREATE TABLE books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    category VARCHAR(50),
    author VARCHAR(100),
    title VARCHAR(150),
    coverImage VARCHAR(255),
    edition VARCHAR(20),
    publisher VARCHAR(100),
    publicationYear INT,
    quantityInStock INT,
    minThreshold INT,
    buyingPrice DOUBLE,
    sellingPrice DOUBLE,
    rating FLOAT,
    featured BOOLEAN DEFAULT FALSE,
    releaseDate DATE
);

INSERT INTO books (
    isbn, category, author, title, coverImage, edition, publisher, publicationYear,
    quantityInStock, minThreshold, buyingPrice, sellingPrice, rating, featured, releaseDate
) VALUES
    ('9780141036144', 'Fiction', 'George Orwell', '1984', 'img/1984.jpg', '1st', 'Secker & Warburg', 1949, 10, 3, 5.00, 9.99, 4.8, TRUE, '1949-06-08'),
    ('9780316017930', 'Non-fiction', 'Malcolm Gladwell', 'Outliers', 'img/outliers.jpg', '1st', 'Little, Brown and Company', 2008, 12, 4, 6.00, 12.99, 4.5, FALSE, '2008-11-18'),
    ('9780143105428', 'Classic', 'Jane Austen', 'Pride and Prejudice', 'img/pride_and_prejudice.jpg', '1st', 'Penguin Classics', 2014, 22, 5, 5.50, 9.99, 4.9, TRUE, '2014-01-01'),
    ('9780140449181', 'Classic', 'Homer', 'The Odyssey', 'img/odyssey.jpg', '1st', 'Penguin Classics', 2003, 17, 4, 6.50, 12.00, 4.4, FALSE, '2003-01-01')
ON DUPLICATE KEY UPDATE title = VALUES(title);
