-- Run once as a MySQL admin user (e.g. root):
--   mysql -u root -p < database/01_create_database.sql
--
-- Replace 'change_me' with a password of your own and put the same value
-- in backend/.env (DB_PASSWORD). Do not commit your real password.

CREATE DATABASE IF NOT EXISTS hotel_booking
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER 'hotel_app'@'localhost' IDENTIFIED BY 'change_me';
GRANT ALL PRIVILEGES ON hotel_booking.* TO 'hotel_app'@'localhost';
FLUSH PRIVILEGES;
