-- =====================================================================
-- Sample data: 7 hotels across 7 Indian cities, one room category each.
-- Images are served by Unsplash (free licence); the frontend falls back to
-- a solid colour panel if an image cannot load.
-- =====================================================================

INSERT INTO hotel (id, name, city, address, description, star_rating, cover_image_url) VALUES
(1, 'The Saffron Courtyard', 'Jaipur', 'Hawa Mahal Road, Badi Choupad, Jaipur, Rajasthan 302002',
 'A restored 19th-century haveli in the heart of the Pink City. Rooms open onto a sandstone courtyard with a stepwell fountain, and the rooftop looks straight across to the Hawa Mahal. Breakfast is served on the terrace each morning, and the staff can arrange early-entry tours of Amber Fort before the crowds arrive.',
 4, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1400&q=80'),
(2, 'Blue Lagoon Beach Resort', 'Goa', 'Candolim Beach Road, Candolim, North Goa 403515',
 'Low-rise cottages set among coconut palms, a two-minute walk from Candolim beach. The lagoon pool stays open until midnight, the beach shack serves fresh catch every evening, and bicycles are free for exploring Fort Aguada and the quieter villages inland.',
 4, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1400&q=80'),
(3, 'Marine Drive Grand', 'Mumbai', '135 Netaji Subhash Chandra Bose Road, Marine Drive, Mumbai 400020',
 'An art-deco landmark on the Queen''s Necklace with sweeping views of the Arabian Sea. Walk to Nariman Point and Churchgate station in minutes. The rooftop bar is one of the best places in the city to watch the sunset, and the business centre is open around the clock.',
 5, 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1400&q=80'),
(4, 'Lakeview Palace Retreat', 'Udaipur', 'Lal Ghat, near Jagdish Temple, Udaipur, Rajasthan 313001',
 'A small palace-style retreat on the banks of Lake Pichola, with only a handful of suites so it never feels busy. Every suite faces the water and the City Palace. Evening boat rides leave from the private jetty, and the in-house chef cooks traditional Mewari thalis on request.',
 5, 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1400&q=80'),
(5, 'Misty Hills Tea Bungalow', 'Munnar', 'Pallivasal Estate Road, Chithirapuram, Munnar, Kerala 685565',
 'A colonial planter''s bungalow surrounded by working tea gardens at 1,600 metres. With just two cottages it is quiet, cosy and very popular, so book early. Guided estate walks, a tea-tasting session and a log fire in the lounge every evening are included.',
 3, 'https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=1400&q=80'),
(6, 'Cedar Ridge Lodge', 'Manali', 'Old Manali Road, near Manu Temple, Manali, Himachal Pradesh 175131',
 'A timber-and-stone lodge above the Manalsu river with views of snow peaks from every balcony. Ideal as a base for Solang Valley and the Hampta Pass trek. Hot water bottles, heated blankets and a bonfire on the lawn keep winter nights warm.',
 3, 'https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=1400&q=80'),
(7, 'Indiranagar Urban Suites', 'Bengaluru', '100 Feet Road, HAL 2nd Stage, Indiranagar, Bengaluru, Karnataka 560038',
 'Spacious studio suites on Bengaluru''s liveliest street, close to the metro and the city''s best cafes and microbreweries. Each suite has a kitchenette and a proper work desk, and the coworking lounge has fast fibre internet, making it a favourite for longer work trips.',
 4, 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1400&q=80');

INSERT INTO hotel_amenity (hotel_id, amenity) VALUES
(1, 'Free Wi-Fi'), (1, 'Breakfast included'), (1, 'Rooftop restaurant'), (1, 'Air conditioning'), (1, 'Airport transfer'),
(2, 'Free Wi-Fi'), (2, 'Swimming pool'), (2, 'Beach access'), (2, 'Bicycle hire'), (2, 'Restaurant'), (2, 'Air conditioning'),
(3, 'Free Wi-Fi'), (3, 'Sea view'), (3, 'Spa'), (3, 'Fitness centre'), (3, 'Rooftop bar'), (3, 'Business centre'), (3, 'Valet parking'),
(4, 'Free Wi-Fi'), (4, 'Lake view'), (4, 'Boat rides'), (4, 'Spa'), (4, 'Breakfast included'), (4, 'Swimming pool'),
(5, 'Free Wi-Fi'), (5, 'Breakfast included'), (5, 'Tea estate walks'), (5, 'Fireplace'), (5, 'Free parking'),
(6, 'Free Wi-Fi'), (6, 'Mountain view'), (6, 'Room heating'), (6, 'Bonfire'), (6, 'Free parking'), (6, 'Trek desk'),
(7, 'Free Wi-Fi'), (7, 'Kitchenette'), (7, 'Coworking lounge'), (7, 'Fitness centre'), (7, 'Air conditioning'), (7, 'Laundry');

INSERT INTO hotel_image (hotel_id, sort_order, image_url) VALUES
(1, 0, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1400&q=80'), (1, 1, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1400&q=80'), (1, 2, 'https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1400&q=80'), (1, 3, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1400&q=80'),
(2, 0, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1400&q=80'), (2, 1, 'https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=1400&q=80'), (2, 2, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=1400&q=80'), (2, 3, 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1400&q=80'),
(3, 0, 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1400&q=80'), (3, 1, 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1400&q=80'), (3, 2, 'https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1400&q=80'), (3, 3, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1400&q=80'),
(4, 0, 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1400&q=80'), (4, 1, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1400&q=80'), (4, 2, 'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?auto=format&fit=crop&w=1400&q=80'), (4, 3, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&w=1400&q=80'),
(5, 0, 'https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=1400&q=80'), (5, 1, 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1400&q=80'), (5, 2, 'https://images.unsplash.com/photo-1470770841072-f978cf4d019e?auto=format&fit=crop&w=1400&q=80'), (5, 3, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1400&q=80'),
(6, 0, 'https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=1400&q=80'), (6, 1, 'https://images.unsplash.com/photo-1449158743715-0a90ebb6d2d8?auto=format&fit=crop&w=1400&q=80'), (6, 2, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=1400&q=80'), (6, 3, 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1400&q=80'),
(7, 0, 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1400&q=80'), (7, 1, 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1400&q=80'), (7, 2, 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=1400&q=80'), (7, 3, 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1400&q=80');

INSERT INTO room_type (id, hotel_id, name, description, bed_type, max_guests, price_per_night, total_rooms) VALUES
(1, 1, 'Deluxe Courtyard Room', 'Hand-painted walls, carved wooden furniture and a window seat over the courtyard.', '1 King bed', 2, 6800.00, 6),
(2, 2, 'Garden Cottage', 'Private sit-out facing the pool garden, rain shower and a hammock on the porch.', '1 Queen bed', 2, 8500.00, 8),
(3, 3, 'Premier Sea View King', 'Floor-to-ceiling windows over Marine Drive, marble bathroom with a soaking tub.', '1 King bed', 2, 12500.00, 10),
(4, 4, 'Lake Facing Suite', 'Jharokha balcony over Lake Pichola, separate lounge and a four-poster bed.', '1 King bed', 3, 14200.00, 4),
(5, 5, 'Planter''s Cottage', 'Wood-panelled cottage with its own fireplace and a veranda over the tea slopes.', '1 Queen bed', 2, 5200.00, 2),
(6, 6, 'Mountain View Room', 'Pine-clad room with a balcony facing the Pir Panjal range and a heated floor.', '2 Single beds', 2, 4600.00, 5),
(7, 7, 'Studio Suite', 'Open-plan studio with kitchenette, work desk, ergonomic chair and a city view.', '1 Queen bed', 2, 7400.00, 12);
