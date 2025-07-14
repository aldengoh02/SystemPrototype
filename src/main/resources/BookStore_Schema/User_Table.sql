CREATE DATABASE IF NOT EXISTS BookStore;
USE BookStore;

CREATE TABLE IF NOT EXISTS UserTypes (
    userTypeID int AUTO_INCREMENT PRIMARY KEY,
    userTypeName VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO UserTypes (userTypeName) VALUES ('Admin'), ('Customer'), ('Employee');


CREATE TABLE IF NOT EXISTS Users (
    userID INT AUTO_INCREMENT PRIMARY KEY,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    status VARCHAR(20) DEFAULT 'inactive',
    enrollForPromotions BOOLEAN DEFAULT FALSE,
    userTypeID INT DEFAULT 2, -- Default will be customer
    FOREIGN KEY (userTypeID) REFERENCES UserTypes(userTypeID)
);

INSERT INTO Users (firstName, lastName, email, password, phone)
VALUES ('John', 'Doe', 'john@example.com', 'pass123', '555-555-5676');
