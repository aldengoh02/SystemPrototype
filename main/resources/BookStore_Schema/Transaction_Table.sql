CREATE DATABASE IF NOT EXISTS BookStore;
Use BookStore;

CREATE TABLE IF NOT EXISTS transaction (
    transactionID INT AUTO_INCREMENT PRIMARY KEY,
    orderID INT,
    bookID INT,
    quantity INT,
    FOREIGN KEY (orderID) REFERENCES orders(orderID),
    FOREIGN KEY (bookID) REFERENCES books(id)
);


INSERT INTO transaction (orderID, bookID, quantity) VALUES
(1, 1, 2);
