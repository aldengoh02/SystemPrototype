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

-- filling users table with sample users with hashes for these passwords and
-- their relevant actual pass being shown, users set to active for testing

-- Password: password123 (for John Doe)
INSERT INTO Users (firstName, lastName, email, password, phone, status)
VALUES ('John', 'Doe', 'john@example.com', '$2a$12$abcdefghijklmnopqrstuuCzUtq1M4rqLjJzOfb.FbCAmg1Iz.cIy', '555-555-5676', 'active');

-- Password: securepass456 (for Jane Smith)  
INSERT INTO Users (firstName, lastName, email, password, phone, status)
VALUES ('Jane', 'Smith', 'jane@example.com', '$2a$12$zyxwvutsrqponmlkjihgfey9oSa6NSBqPPTJDkk.WH7tkTbGSY/xa', '555-555-1234', 'active');

-- Password: testUser789 (for Test User)
INSERT INTO Users (firstName, lastName, email, password, phone, status)
VALUES ('Test', 'User', 'test@example.com', '$2a$12$1234567890abcdefghijkeW9xxo0jTBbAtdBUgB5yPfprdeEI4d2.', '555-555-9999', 'active');

-- Password: admin123 (for Admin User)
INSERT INTO Users (firstName, lastName, email, password, phone, status, userTypeID)  
VALUES ('Admin', 'User', 'admin@example.com', '$2a$12$qwertyuiopasdfghjklzxOsM3DLFZN7Bj0m4nFnCC8BbuHSOaajUO', '555-555-0001', 'active', 1);

-- Password: customer456 (for Customer User)
INSERT INTO Users (firstName, lastName, email, password, phone, status)
VALUES ('Customer', 'Demo', 'customer@example.com', '$2a$12$mnbvcxzasdfghjklpoiuyemxBrR1BAshmQ86CsGNK/szLuyBIcaYS', '555-555-0002', 'active');
