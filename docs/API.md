# API Reference

Base URL: `http://localhost:8080/api` · JSON · dates as `YYYY-MM-DD` · money as numbers with 2 decimals (INR).

Every error uses the format described in [DESIGN.md §3](../DESIGN.md#error-format).

---

## Hotels

### `GET /hotels`

Browse or search. All parameters optional; if one date is given, both are required.

| Param | Example | Notes |
|-------|---------|-------|
| `location` | `goa` | Case-insensitive match on city, address or name (max 80 chars) |
| `checkIn` | `2026-12-10` | |
| `checkOut` | `2026-12-12` | |

```bash
curl "http://localhost:8080/api/hotels?location=munnar&checkIn=2026-12-10&checkOut=2026-12-12"
```

```json
[
  {
    "id": 5,
    "name": "Misty Hills Tea Bungalow",
    "city": "Munnar",
    "address": "Pallivasal Estate Road, Chithirapuram, Munnar, Kerala 685565",
    "description": "A colonial planter's bungalow ...",
    "starRating": 3,
    "coverImageUrl": "https://images.unsplash.com/...",
    "amenities": ["Free Wi-Fi", "Breakfast included", "Tea estate walks", "Fireplace", "Free parking"],
    "roomName": "Planter's Cottage",
    "pricePerNight": 5200.00,
    "currency": "INR",
    "totalRooms": 2,
    "availableRooms": 2
  }
]
```

`availableRooms` is `null` when no dates are given.

### `GET /hotels/locations`

```json
["Bengaluru", "Goa", "Jaipur", "Manali", "Mumbai", "Munnar", "Udaipur"]
```

### `GET /hotels/{id}?checkIn=&checkOut=`

```json
{
  "id": 1,
  "name": "The Saffron Courtyard",
  "city": "Jaipur",
  "address": "...",
  "description": "...",
  "starRating": 4,
  "coverImageUrl": "...",
  "images": ["...", "...", "...", "..."],
  "amenities": ["Free Wi-Fi", "Breakfast included", "..."],
  "currency": "INR",
  "room": {
    "id": 1, "name": "Deluxe Courtyard Room", "description": "...",
    "bedType": "1 King bed", "maxGuests": 2, "pricePerNight": 6800.00, "totalRooms": 6
  },
  "stay": {
    "checkIn": "2026-12-10", "checkOut": "2026-12-13", "nights": 3,
    "availableRooms": 6, "available": true, "totalAmount": 20400.00
  }
}
```

`stay` is `null` without dates. Unknown id → `404 HOTEL_NOT_FOUND`.

### `GET /hotels/{id}/calendar?from=&days=` *(surprise feature)*

`from` defaults to today (earlier dates are moved to today). `days` 1–62, default 30.

```json
[
  { "date": "2026-11-29", "availableRooms": 0, "totalRooms": 2, "level": "SOLD_OUT",  "pricePerNight": 5200.00 },
  { "date": "2026-12-02", "availableRooms": 1, "totalRooms": 2, "level": "LIMITED",   "pricePerNight": 5200.00 },
  { "date": "2026-12-03", "availableRooms": 2, "totalRooms": 2, "level": "AVAILABLE", "pricePerNight": 5200.00 }
]
```

### `GET /hotels/{id}/alternatives?checkIn=&checkOut=` *(surprise feature)*

Nearest stays of the same length (±14 days, closest first, at most 4) with a room on every night.

```json
[
  { "checkIn": "2026-12-02", "checkOut": "2026-12-04", "nights": 2, "shiftDays": 3,  "availableRooms": 2, "totalAmount": 10400.00 },
  { "checkIn": "2026-11-26", "checkOut": "2026-11-28", "nights": 2, "shiftDays": -3, "availableRooms": 2, "totalAmount": 10400.00 }
]
```

---

## Bookings

### `POST /bookings/quote`

```bash
curl -X POST http://localhost:8080/api/bookings/quote \
  -H "Content-Type: application/json" \
  -d '{"hotelId":1,"checkIn":"2026-12-10","checkOut":"2026-12-13"}'
```

```json
{
  "hotelId": 1, "hotelName": "The Saffron Courtyard", "roomName": "Deluxe Courtyard Room",
  "checkIn": "2026-12-10", "checkOut": "2026-12-13", "nights": 3,
  "pricePerNight": 6800.00, "totalAmount": 20400.00, "currency": "INR",
  "availableRooms": 6, "available": true
}
```

### `POST /bookings` → `201 Created`

```bash
curl -i -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"hotelId":1,"checkIn":"2026-12-10","checkOut":"2026-12-13","guestName":"Asha Kumar","guestEmail":"asha@example.com"}'
```

```
HTTP/1.1 201
Location: http://localhost:8080/api/bookings/SB-K7QM4XPA
```

```json
{
  "reference": "SB-K7QM4XPA",
  "status": "CONFIRMED",
  "hotelId": 1, "hotelName": "The Saffron Courtyard", "hotelCity": "Jaipur",
  "hotelAddress": "...", "coverImageUrl": "...", "roomName": "Deluxe Courtyard Room",
  "guestName": "Asha Kumar", "guestEmail": "asha@example.com",
  "checkIn": "2026-12-10", "checkOut": "2026-12-13", "nights": 3,
  "pricePerNight": 6800.00, "totalAmount": 20400.00, "currency": "INR",
  "createdAt": "2026-11-24T06:12:03.511Z", "cancelledAt": null,
  "cancellable": true
}
```

Validation error example (`400`):

```json
{
  "status": 400, "error": "Bad Request", "code": "VALIDATION_FAILED",
  "message": "Some fields need your attention.", "path": "/api/bookings",
  "fieldErrors": [
    { "field": "guestEmail", "message": "Please enter a valid email address." },
    { "field": "guestName",  "message": "Please enter your full name." }
  ]
}
```

Sold out (`409`):

```json
{ "status": 409, "code": "ROOM_UNAVAILABLE",
  "message": "Sorry, no rooms are left for the night of 2026-11-29. Please try different dates." }
```

### `GET /bookings/{reference}?email=`

```bash
curl "http://localhost:8080/api/bookings/SB-DEMA2345?email=demo@staybook.test"
```

Returns the same body as create. Wrong reference or email → `404 BOOKING_NOT_FOUND`.

### `POST /bookings/{reference}/cancel`

```bash
curl -X POST http://localhost:8080/api/bookings/SB-K7QM4XPA/cancel \
  -H "Content-Type: application/json" -d '{"email":"asha@example.com"}'
```

Returns the booking with `"status": "CANCELLED"`, `cancelledAt` set and `"cancellable": false`.
Errors: `404 BOOKING_NOT_FOUND`, `409 BOOKING_ALREADY_CANCELLED`, `409 BOOKING_NOT_CANCELLABLE`.
