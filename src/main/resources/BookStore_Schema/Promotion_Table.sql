CREATE DATABASE IF NOT EXISTS BookStore;
USE BookStore;

CREATE TABLE promotion (
    promoID INT AUTO_INCREMENT PRIMARY KEY,
    promoCode VARCHAR(50) UNIQUE NOT NULL,
    startDate DATE,
    endDate DATE,
    discount FLOAT
);

INSERT INTO promotion (promoCode, startDate, endDate, discount) VALUES
('SUMMER20', '2025-06-01', '2025-08-31', 20.0),
('BACK2SCHOOL', '2025-08-01', '2025-09-15', 15.0);