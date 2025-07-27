CREATE DATABASE IF NOT EXISTS BookStore;
USE BookStore;

-- 1.Add the column (run this only once! Remove it out after first run)
ALTER TABLE Users
ADD COLUMN loginUserID INT UNIQUE;

-- 2.Drop function if exists to avoid errors
DROP FUNCTION IF EXISTS generate_unique_user_id;

DELIMITER $$

-- 3.Create the function to generate unique 7 digit number
CREATE FUNCTION generate_unique_user_id()
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE randID INT;
    DECLARE idExists INT;

    REPEAT
        SET randID = FLOOR(1000000 + RAND() * 8999999);
        SELECT COUNT(*) INTO idExists FROM Users WHERE loginUserID = randID;
    UNTIL idExists = 0
    END REPEAT;

    RETURN randID;
END $$

DELIMITER ;

-- 4.Update existing rows without loginUserID
UPDATE Users
SET loginUserID = generate_unique_user_id()
WHERE loginUserID IS NULL;

-- 5.Drop any old trigger if it exists
DROP TRIGGER IF EXISTS before_insert_user_login_id;

DELIMITER $$

-- 6.Create trigger for auto-generating loginUserID for each new user
CREATE TRIGGER before_insert_user_login_id
BEFORE INSERT ON Users
FOR EACH ROW
BEGIN
    IF NEW.loginUserID IS NULL THEN
        SET NEW.loginUserID = generate_unique_user_id();
    END IF;
END $$

DELIMITER ;
