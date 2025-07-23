CREATE DATABASE IF NOT EXISTS BookStore;
USE BookStore;

-- Comment out the line below if table already exists
DROP TABLE IF EXISTS ShippingAddress;

CREATE TABLE ShippingAddress (
    addressID INT AUTO_INCREMENT PRIMARY KEY,
    userID INT NOT NULL,
    street VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zipCode VARCHAR(10) NOT NULL,

    FOREIGN KEY (userID) REFERENCES Users(userID)
);


INSERT INTO ShippingAddress (userID, street, city, state, zipCode)
VALUES
(1, '789 Maple Dr', 'Savannah', 'GA', '31401');