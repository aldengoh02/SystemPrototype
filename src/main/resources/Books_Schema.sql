CREATE DATABASE IF NOT EXISTS BookStore;
USE BookStore;

DROP TABLE IF EXISTS books;

CREATE TABLE books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    category VARCHAR(50),
    author VARCHAR(100),
    title VARCHAR(150),
    coverImage VARCHAR(255),
    edition VARCHAR(20),
    publisher VARCHAR(100),
    publicationYear INT,
    quantityInStock INT,
    minThreshold INT,
    buyingPrice DOUBLE,
    sellingPrice DOUBLE,
    rating FLOAT,
    featured BOOLEAN DEFAULT FALSE,
    releaseDate DATE
);

INSERT INTO books (
    isbn, category, author, title, coverImage, edition, publisher, publicationYear,
    quantityInStock, minThreshold, buyingPrice, sellingPrice, rating, featured, releaseDate
) VALUES
-- Featured Books
('9781234567001', 'Fantasy', 'Rebecca Yarros', 'Fourth Wing', 'webapp/img/Fourth_Wing.png', '1st Edition', 'Entangled Publishing', 2023, 18, 3, 11.00, 19.99, 4.6, TRUE, '2023-04-05'),
('9781234567002', 'Fantasy', 'Rebecca Yarros', 'Iron Flame', 'webapp/img/Iron_Flame.png', '1st Edition', 'Entangled Publishing', 2023, 20, 3, 13.00, 22.99, 4.8, TRUE, '2023-10-31'),
('9781234567003', 'Horror', 'Stephen Graham Jones', 'Mapping the Interior', 'webapp/img/Mapping_The_Interior.png', '2nd Edition', 'Tor Publishing Group', 2025, 15, 3, 7.50, 12.99, 4.4, TRUE, '2025-05-29'),
('9781234567004', 'Fiction', 'V.E.Schwab', 'Bury Our Bones in the Midnight Soil', 'webapp/img/Midnight_Soil.png', '1st Edition', 'Tor Publishing Group', 2025, 12, 3, 10.00, 16.99, 4.5, TRUE, '2025-06-10'),
('9781234567005', 'Thriller', 'Stephen King', 'Never Flinch', 'webapp/img/Never_Flinch.png', '1st Edition', 'Scribner', 2025, 17, 3, 11.50, 18.99, 4.7, TRUE, '2025-05-27'),
('9781234567006', 'Dystopian', 'Suzanne Collins', 'Sunrise on the Reaping', 'webapp/img/Sunrise_On_The_Reaping.png', '1st Edition', 'Scholastic Press', 2025, 25, 3, 14.00, 24.99, 5.0, TRUE, '2025-03-18'),
('9781234567007', 'Thriller', 'Freida McFadden', 'The Housemaid', 'webapp/img/The_Housemaid.png', '1st Edition', 'Bookouture', 2022, 22, 3, 9.00, 16.99, 4.6, TRUE, '2022-04-21'),
('9781234567008', 'Fantasy', 'Sarah Beth Durst', 'The Spellshop', 'webapp/img/The_Spellshop.png', '1st Edition', 'Brambles', 2024, 20, 3, 13.50, 21.99, 4.5, TRUE, '2024-07-09'),
('9781234567009', 'Mystery', 'Freida McFadden', 'The Tenant', 'webapp/img/The_Tenant.png', 'Exclusive Edition', 'Sourcebooks', 2025, 14, 3, 8.00, 14.99, 4.3, TRUE, '2025-05-06'),
('9781234567010', 'Romance', 'Meghan Quinn', 'Till Summer Do Us Part', 'webapp/img/Till_Summer_Do_Us_Part.png', 'Deluxe Edition', 'Sourcebooks', 2025, 19, 3, 10.50, 17.99, 4.7, TRUE, '2025-06-03'),
-- Coming Soon Books
('9781234567011', 'Fantasy', 'T. Kingfisher', 'Hemlock and Silver', 'webapp/img/Hemlock_&_Silver.png', '2nd Edition', 'Tor Publishing Group', 2025, 0, 0, 10.00, 18.99, 0.0, FALSE, '2025-08-19'),
('9781234567012', 'Mystery', 'Richard Osman', 'The Impossible Fortune', 'webapp/img/The_Impossible_Fortune.png', '1st Edition', 'Penguin Publishing Group', 2025, 0, 0, 9.00, 17.99, 0.0, FALSE, '2025-09-30'),
('9781234567013', 'Fantasy', 'Louis Sachar', 'The Magician of Tiger Castle', 'webapp/img/The_Magician_Of_Tiger_Castle.png', '1st Edition', 'Penguin Publishing Group', 2025, 0, 0, 11.00, 19.99, 0.0, FALSE, '2025-08-05'),
('9781234567014', 'Thriller', 'Hannah Nicole Maehrer', 'Accomplice to the Villain', 'webapp/img/Accomplice_To_The_Villain.png', '1st Edition', 'Entangled Publishing', 2025, 0, 0, 8.00, 15.99, 0.0, FALSE, '2025-08-05'),
('9781234567015', 'Science', 'Mary Roach', 'Replaceable You', 'webapp/img/Replaceable_You.png', '1st Edition', 'W.W. Norton & Company', 2025, 0, 0, 10.00, 18.00, 0.0, FALSE, '2025-09-16'),
('9781234567016', 'Fiction', 'Ian McEwan', 'What We Can Know', 'webapp/img/What_We_Can_Know.png', '1st Edition', 'Doubleday', 2025, 0, 0, 9.00, 16.50, 0.0, FALSE, '2025-09-23'),
('9781234567017', 'Literature', 'Jason Mott', 'People Like Us', 'webapp/img/People_Like_Us.png', '1st Edition', 'Penguin Publishing Group', 2025, 0, 0, 8.50, 14.99, 0.0, FALSE, '2025-08-05'),
('9781234567018', 'Historical', 'Marjan Kamali', 'The Lion Women of Tehran', 'webapp/img/The_Lion_Women_Of_Tehran.png', '1st Edition', 'Gallery Books', 2025, 0, 0, 11.00, 19.99, 0.0, FALSE, '2025-08-05'),
('9781234567019', 'Fiction', 'Ryan', 'Buckeye', 'webapp/img/Buckeye.png', '1st Edition', 'Random House Publishing Group', 2025, 0, 0, 10.00, 17.99, 0.0, FALSE, '2025-09-02'),
('9781234567020', 'Mystery', 'G.T. Karber', 'The Case of the Seven Skulls', 'webapp/img/The_Case_Of_The_Seven_Skulls.png', '1st Edition', 'St. Martin Publishing Group', 2025, 0, 0, 9.00, 15.99, 0.0, FALSE, '2025-07-29')
ON DUPLICATE KEY UPDATE title = VALUES(title);