CREATE DATABASE IF NOT EXISTS BookStore;
Use BookStore;

CREATE TABLE IF NOT EXISTS orders (
    orderID INT AUTO_INCREMENT PRIMARY KEY,
    userID INT,
    cardID INT,
    promoID INT,
    grandTotal DOUBLE,
    orderDateTime DATETIME,
    FOREIGN KEY (userID) REFERENCES users(userID),
    FOREIGN KEY (cardID) REFERENCES paymentcard(cardID),
    FOREIGN KEY (promoID) REFERENCES promotion(promoID)
);

INSERT INTO orders (userID, cardID, promoID, grandTotal, orderDateTime)
VALUES (1, 1, 1, 42.38, NOW());