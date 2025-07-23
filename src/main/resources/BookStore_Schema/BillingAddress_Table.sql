CREATE DATABASE IF NOT EXISTS BookStore;
USE BookStore;

-- Comment out the line below if table already exists
DROP TABLE IF EXISTS BillingAddress;


CREATE TABLE BillingAddress (
    addressID INT AUTO_INCREMENT PRIMARY KEY,
    street VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zipCode VARCHAR(10) NOT NULL
);

INSERT INTO BillingAddress (street, city, state, zipCode)
VALUES
('123 Peach St', 'Atlanta', 'GA', '30303');