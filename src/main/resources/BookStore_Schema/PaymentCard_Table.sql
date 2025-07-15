CREATE DATABASE IF NOT EXISTS BookStore;
USE BookStore;

-- Comment out the line below if table already exists
DROP TABLE IF EXISTS PaymentCard;

--Changed the fields a bit to include cardID; if you already have the table
--Please re-upload this file and redo the table, with the DROP statement it wil fix your table

CREATE TABLE PaymentCard (
    cardID INT AUTO_INCREMENT PRIMARY KEY,
    cardNo VARCHAR(16) UNIQUE,
    userID INT NOT NULL,
    type VARCHAR(20) NOT NULL,
    expirationDate VARCHAR(10) NOT NULL,
    billingAddressID INT NOT NULL,

    FOREIGN KEY (userID) REFERENCES Users(userID),
    FOREIGN KEY (billingAddressID) REFERENCES BillingAddress(addressID)
);


INSERT INTO PaymentCard (cardNo, userID, type, expirationDate, billingAddressID)
VALUES
('1234567890123456', 1, 'Visa', '12/26', 1);