CREATE DATABASE IF NOT EXISTS BookStore;
USE BookStore;

DROP TABLE IF EXISTS books; -- Remove or comment this out after initial setup. Useful only for testing

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
    releaseDate DATE,
    description TEXT
);

INSERT INTO books (
    isbn, category, author, title, coverImage, edition, publisher, publicationYear,
    quantityInStock, minThreshold, buyingPrice, sellingPrice, rating, featured, releaseDate, description
) VALUES
-- Featured Books
('9781234567001', 'Fantasy', 'Rebecca Yarros', 'Fourth Wing', 'img/Fourth_Wing.png', '1st Edition', 'Entangled Publishing', 2023, 18, 3, 11.00, 19.99, 4.6, TRUE, '2023-04-05',
 'Welcome to the brutal and magical world of Basgiath War College, where young riders train with dragons or perish. Violet Sorrengail faces deadly trials, secrets, and a destiny that could change the kingdom. A thrilling blend of fantasy, romance, and adventure.'),

('9781234567002', 'Fantasy', 'Rebecca Yarros', 'Iron Flame', 'img/Iron_Flame.png', '1st Edition', 'Entangled Publishing', 2023, 20, 3, 13.00, 22.99, 4.8, TRUE, '2023-10-31',
 'In a land scorched by war and ambition, the Iron Flame ignites passion and power. A story of fierce loyalty, political intrigue, and magic that tests the limits of courage and love.'),

('9781234567003', 'Horror', 'Stephen Graham Jones', 'Mapping the Interior', 'img/Mapping_The_Interior.png', '2nd Edition', 'Tor Publishing Group', 2025, 15, 3, 7.50, 12.99, 4.4, TRUE, '2025-05-29',
 'A chilling tale that delves deep into the darkest corners of the human psyche. When the boundaries between reality and nightmare blur, one must confront their inner demons or be consumed by them.'),

('9781234567004', 'Fiction', 'V.E.Schwab', 'Bury Our Bones in the Midnight Soil', 'img/Midnight_Soil.png', '1st Edition', 'Tor Publishing Group', 2025, 12, 3, 10.00, 16.99, 4.5, TRUE, '2025-06-10',
 'A haunting narrative weaving themes of memory, loss, and survival. This evocative story explores the shadows that linger in the past and the hope that can be buried deep beneath the earth.'),

('9781234567005', 'Thriller', 'Stephen King', 'Never Flinch', 'img/Never_Flinch.png', '1st Edition', 'Scribner', 2025, 17, 3, 11.50, 18.99, 4.7, TRUE, '2025-05-27',
 'From the master of suspense comes a collection of gripping tales that will keep you on edge. Each story tests human endurance and the ability to face fear without ever flinching.'),

('9781234567006', 'Dystopian', 'Suzanne Collins', 'Sunrise on the Reaping', 'img/Sunrise_On_The_Reaping.png', '1st Edition', 'Scholastic Press', 2025, 25, 3, 14.00, 24.99, 5.0, TRUE, '2025-03-18',
 'In a dystopian future where survival is a daily battle, a new dawn brings hope and revolution. Follow the journey of those who rise against oppression and seek freedom at any cost.'),

('9781234567007', 'Thriller', 'Freida McFadden', 'The Housemaid', 'img/The_Housemaid.png', '1st Edition', 'Bookouture', 2022, 22, 3, 9.00, 16.99, 4.6, TRUE, '2022-04-21',
 'A psychological thriller unraveling secrets behind closed doors. When the new housemaid arrives, nothing is as it seems—trust is scarce, and danger lurks in every shadow.'),

('9781234567008', 'Fantasy', 'Sarah Beth Durst', 'The Spellshop', 'img/The_Spellshop.png', '1st Edition', 'Brambles', 2024, 20, 3, 13.50, 21.99, 4.5, TRUE, '2024-07-09',
 'Step inside The Spellshop, where magic is crafted with care and every customer has a story. This enchanting tale blends whimsy, wonder, and a touch of danger.'),

('9781234567009', 'Mystery', 'Freida McFadden', 'The Tenant', 'img/The_Tenant.png', 'Exclusive Edition', 'Sourcebooks', 2025, 14, 3, 8.00, 14.99, 4.3, TRUE, '2025-05-06',
 'When a mysterious tenant moves in, a quiet neighborhood is shaken. Secrets unravel, alliances shift, and the truth may be more terrifying than anyone imagined.'),

('9781234567010', 'Romance', 'Meghan Quinn', 'Till Summer Do Us Part', 'img/Till_Summer_Do_Us_Part.png', 'Deluxe Edition', 'Sourcebooks', 2025, 19, 3, 10.50, 17.99, 4.7, TRUE, '2025-06-03',
 'A heartwarming summer romance about second chances, unexpected love, and finding yourself when you least expect it. Perfect for fans of sweet and swoony love stories.'),

-- Coming Soon Books
('9781234567011', 'Fantasy', 'T. Kingfisher', 'Hemlock and Silver', 'img/Hemlock_&_Silver.png', '2nd Edition', 'Tor Publishing Group', 2025, 0, 0, 10.00, 18.99, 0.0, FALSE, '2025-08-19',
 'A dark fairy tale where magic and danger intertwine. Hemlock and Silver offers a fresh twist on classic fantasy with vivid characters and unexpected twists.'),

('9781234567012', 'Mystery', 'Richard Osman', 'The Impossible Fortune', 'img/The_Impossible_Fortune.png', '1st Edition', 'Penguin Publishing Group', 2025, 0, 0, 9.00, 17.99, 0.0, FALSE, '2025-09-30',
 'A clever mystery filled with wit and charm. When an impossible fortune surfaces, an unlikely group must solve puzzles and uncover hidden truths.'),

('9781234567013', 'Fantasy', 'Louis Sachar', 'The Magician of Tiger Castle', 'img/The_Magician_of_Tiger_Castle.png', '1st Edition', 'Penguin Publishing Group', 2025, 0, 0, 11.00, 19.99, 0.0, FALSE, '2025-08-05',
 'Journey to Tiger Castle where a young magician discovers secrets beyond his wildest dreams. A captivating tale of magic, friendship, and courage.'),

('9781234567014', 'Thriller', 'Hannah Nicole Maehrer', 'Accomplice to the Villain', 'img/Accomplice_To_The_Villain.png', '1st Edition', 'Entangled Publishing', 2025, 0, 0, 8.00, 15.99, 0.0, FALSE, '2025-08-05',
 'A suspenseful thriller about the fine line between justice and guilt. When caught in the web of a villainous plot, the true accomplice may surprise you.'),

('9781234567015', 'Science', 'Mary Roach', 'Replaceable You', 'img/Replaceable_You.png', '1st Edition', 'W.W. Norton & Company', 2025, 0, 0, 10.00, 18.00, 0.0, FALSE, '2025-09-16',
 'An engaging exploration of medical science and the ethics of organ transplantation. Mary Roach blends humor and curiosity in this thought-provoking read.'),

('9781234567016', 'Fiction', 'Ian McEwan', 'What We Can Know', 'img/What_We_Can_Know.png', '1st Edition', 'Doubleday', 2025, 0, 0, 9.00, 16.50, 0.0, FALSE, '2025-09-23',
 'A profound examination of knowledge, truth, and the human experience. Ian McEwan offers insight into what shapes our understanding of the world.'),

('9781234567017', 'Literature', 'Jason Mott', 'People Like Us', 'img/People_Like_Us.png', '1st Edition', 'Penguin Publishing Group', 2025, 0, 0, 8.50, 14.99, 0.0, FALSE, '2025-08-05',
 'A moving story about community, identity, and the ties that bind us. Jason Mott crafts a narrative that resonates deeply with readers.'),

('9781234567018', 'Historical', 'Marjan Kamali', 'The Lion Women of Tehran', 'img/The_Lion_Women_of_Tehran.png', '1st Edition', 'Gallery Books', 2025, 0, 0, 11.00, 19.99, 0.0, FALSE, '2025-08-05',
 'An evocative historical novel chronicling the strength and resilience of women in Tehran. A powerful tribute to courage and family.'),

('9781234567019', 'Fiction', 'Ryan Patrick', 'Buckeye', 'img/Buckeye.png', '1st Edition', 'Random House Publishing Group', 2025, 0, 0, 10.00, 17.99, 0.0, FALSE, '2025-09-02',
 'An intimate portrait of life in the American Midwest. Buckeye explores themes of hope, struggle, and the ties that hold communities together.'),

('9781234567020', 'Mystery', 'G.T. Karber', 'The Case of the Seven Skulls', 'img/The_Case_of_The_Seven_Skulls.png', '1st Edition', 'St. Martin Publishing Group', 2025, 0, 0, 9.00, 15.99, 0.0, FALSE, '2025-07-29',
 'A gripping mystery that pits a determined detective against an ancient curse. The Case of the Seven Skulls is full of suspense, secrets, and unexpected revelations.')
ON DUPLICATE KEY UPDATE title = VALUES(title);
