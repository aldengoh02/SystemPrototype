CREATE DATABASE IF NOT EXISTS BookStore;
USE BookStore;

-- Comment out the line below if table already exists
DROP TABLE IF EXISTS PaymentCard;

CREATE TABLE PaymentCard (
    cardNo VARCHAR(16) PRIMARY KEY,
    userID INT NOT NULL,
    type VARCHAR(20) NOT NULL,
    expirationDate VARCHAR(10) NOT NULL,
    billingAddressID INT NOT NULL,

    FOREIGN KEY (userID) REFERENCES Users(userID),
    FOREIGN KEY (billingAddressID) REFERENCES BillingAddress(addressID)
);


INSERT INTO PaymentCard (cardNo, userID, type, expirationDate, billingAddressID)
VALUES
('1234567890123456', 1, 'Visa', '12/26', 1)