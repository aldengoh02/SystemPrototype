CREATE DATABASE IF NOT EXISTS BookStore;
USE BookStore;

CREATE TABLE IF NOT EXISTS VerificationToken (
    token_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR(255) NOT NULL,
    expiry_date DATETIME NOT NULL,
    token_type VARCHAR(50) NOT NULL,  -- 'email_verification' or 'password_reset'
    CONSTRAINT fk_users FOREIGN KEY (user_id) REFERENCES Users(userID)
);

INSERT INTO VerificationToken (user_id, token, expiry_date, token_type)
VALUES (1, 'abc123emailtoken456', '2025-07-31 23:59:59', 'email_verification');
