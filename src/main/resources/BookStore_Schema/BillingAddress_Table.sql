CREATE DATABASE IF NOT EXISTS BookStore;
USE BookStore;

-- Comment out the line below if table already exists
DROP TABLE IF EXISTS BillingAddress;


CREATE TABLE BillingAddress (
    addressID INT AUTO_INCREMENT PRIMARY KEY,
    userID INT NOT NULL UNIQUE,
    street VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zipCode VARCHAR(10) NOT NULL,
    FOREIGN KEY (userID) REFERENCES Users(userID)
);

INSERT INTO BillingAddress (userID, street, city, state, zipCode)
VALUES
(1, '123 Peach St', 'Atlanta', 'GA', '30303');