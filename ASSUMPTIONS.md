# Assumptions

Decisions taken where the brief left room for interpretation. Each is easy to change.

## Stays and dates

| # | Assumption |
|---|------------|
| A1 | A stay is the half-open range **[check-in, check-out)**. The guest uses the nights starting on check-in up to the night before check-out. Nights = days between the two dates. |
| A2 | **Overlapping stays:** two bookings overlap only if they share at least one *night*. A guest checking out on the 13th and another checking in on the 13th do **not** overlap and can use the same room. |
| A3 | A room type with *N* rooms can accept up to *N* bookings that overlap on any single night. A new booking is accepted only if **every** night of its stay has at least one room free. Rooms are interchangeable; the system tracks counts, not room numbers. |
| A4 | Check-in **today** is allowed. Check-in yesterday or earlier is rejected. |
| A5 | "Today" is decided in the hotels' time zone (`Asia/Kolkata` by default, configurable with `APP_TIMEZONE`), not the server's or the browser's. |
| A6 | Minimum stay is 1 night. Maximum stay is **30 nights** per booking. |
| A7 | Bookings open **365 days** in advance. Later check-ins are rejected. |
| A8 | Check-in/check-out times (e.g. 2 pm / 11 am) are not modelled; only dates matter. |

## Pricing

| # | Assumption |
|---|------------|
| P1 | Total = price per night × number of nights. Same price every night (no weekend or seasonal rates). |
| P2 | Prices are in **Indian Rupees (INR)** and **include taxes**. No fees, discounts or deposits. |
| P3 | Price does not depend on the number of guests. Each room shows a max occupancy for information. |
| P4 | The price per night and total are fixed at booking time. A later price change does not affect existing bookings. |
| P5 | Payment happens at the hotel (no payment integration, as stated in the brief). |

## Bookings

| # | Assumption |
|---|------------|
| B1 | One booking = one room of the hotel's single room category. |
| B2 | Guest name: 2–100 characters, letters (any language), spaces, apostrophes, dots and hyphens. |
| B3 | Email is stored lower-cased and compared case-insensitively. The same email may make many bookings. |
| B4 | The booking reference is case-insensitive when looking up (`sb-k7qm4xpa` works). |
| B5 | A booking is confirmed immediately on creation; there is no pending/hold state. |
| B6 | Looking up or cancelling a booking requires **both** the reference and the email used to book. A wrong reference and a wrong email give the same "not found" answer. |

## Cancellation

| # | Assumption |
|---|------------|
| C1 | Cancellation is **free and allowed until the check-in date** (including on the check-in day itself). Once check-in is in the past, the stay has started and cannot be cancelled online. |
| C2 | Cancelling releases every night of the stay immediately, so other guests can book them. |
| C3 | A cancelled booking is kept with status `CANCELLED` and a timestamp; it is never deleted. It cannot be cancelled again or re-activated. |
| C4 | Partial cancellation (shortening a stay) is not supported. |

## Data and environment

| # | Assumption |
|---|------------|
| D1 | Seven sample hotels in seven Indian cities, one room category each, with 2–12 rooms. Small room counts (e.g. Munnar has 2) make sold-out states easy to demonstrate. |
| D2 | Demo bookings are seeded relative to the date the migration runs (today+5 … today+12), so the calendar shows sold-out and "few left" days straight away. |
| D3 | Hotel photos are hot-linked from Unsplash. Without internet, a solid-colour placeholder is shown. |
| D4 | One running backend instance is assumed for local use, but correctness under concurrency relies on database row locks, so it also holds with several instances sharing one MySQL. |
| D5 | Location search is a case-insensitive "contains" match on city, address or hotel name. |
