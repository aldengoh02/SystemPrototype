CREATE DATABASE IF NOT EXISTS BookStore;
Use BookStore;

CREATE TABLE IF NOT EXISTS cart (
    userID INT,
    bookID INT,
    quantity INT,
    PRIMARY KEY (userID, bookID),
    FOREIGN KEY (userID) REFERENCES users(userID),
    FOREIGN KEY (bookID) REFERENCES books(id)
);

INSERT INTO cart (userID, bookID, quantity) VALUES
(1, 1, 1);
