-- =====================================================================
-- Demo bookings, relative to the date the migration runs, so reviewers can
-- see the availability calendar and the "nearest available dates" feature
-- straight away.
--
--  * Misty Hills (2 rooms) is SOLD OUT for the nights today+5 .. today+7.
--  * Lakeview Palace (4 rooms) has only 1 room left for today+10 .. today+12.
--
-- Try retrieving:  reference SB-DEMA2345   email demo@staybook.test
-- =====================================================================

INSERT INTO booking (reference, hotel_id, room_type_id, guest_name, guest_email, check_in, check_out,
                     nights, price_per_night, total_amount, status, created_at) VALUES
('SB-DEMA2345', 5, 5, 'Ananya Rao',   'demo@staybook.test',   CURDATE() + INTERVAL 5 DAY,  CURDATE() + INTERVAL 8 DAY,  3, 5200.00, 15600.00, 'CONFIRMED', UTC_TIMESTAMP(6)),
('SB-DEMB2345', 5, 5, 'Vikram Shah',  'vikram@staybook.test', CURDATE() + INTERVAL 5 DAY,  CURDATE() + INTERVAL 8 DAY,  3, 5200.00, 15600.00, 'CONFIRMED', UTC_TIMESTAMP(6)),
('SB-DEMC2345', 4, 4, 'Meera Iyer',   'meera@staybook.test',  CURDATE() + INTERVAL 10 DAY, CURDATE() + INTERVAL 13 DAY, 3, 14200.00, 42600.00, 'CONFIRMED', UTC_TIMESTAMP(6)),
('SB-DEMD2345', 4, 4, 'Rahul Menon',  'rahul@staybook.test',  CURDATE() + INTERVAL 10 DAY, CURDATE() + INTERVAL 13 DAY, 3, 14200.00, 42600.00, 'CONFIRMED', UTC_TIMESTAMP(6)),
('SB-DEME2345', 4, 4, 'Sara Thomas',  'sara@staybook.test',   CURDATE() + INTERVAL 10 DAY, CURDATE() + INTERVAL 13 DAY, 3, 14200.00, 42600.00, 'CONFIRMED', UTC_TIMESTAMP(6));

-- Inventory rows that match the bookings above.
INSERT INTO room_inventory (room_type_id, stay_date, booked_count) VALUES
(5, CURDATE() + INTERVAL 5 DAY, 2),
(5, CURDATE() + INTERVAL 6 DAY, 2),
(5, CURDATE() + INTERVAL 7 DAY, 2),
(4, CURDATE() + INTERVAL 10 DAY, 3),
(4, CURDATE() + INTERVAL 11 DAY, 3),
(4, CURDATE() + INTERVAL 12 DAY, 3);
