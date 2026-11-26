# Staybook – Hotel Booking Portal

A small, complete hotel booking portal: browse hotels, search by location and dates, see live
availability, book one room with a name and email, get a booking reference, and look up or cancel
that booking later.

| Layer    | Technology |
|----------|-----------|
| Backend  | Java 17+, Spring Boot 3.5, Spring Data JPA, Bean Validation, Flyway |
| Frontend | Angular 20 (standalone components, signals), SCSS |
| Database | MySQL 8 |
| Tests    | JUnit 5, AssertJ, Spring Boot Test, MockMvc, H2 (MySQL mode) |

**Surprise feature: Availability calendar with "nearest available dates".**
Each hotel page shows a colour-coded, swipeable 30–90 day strip of rooms left per night. Guests can
pick their stay by tapping two days. If the chosen dates are sold out, the app suggests the closest
same-length stays that still have a room, with prices, and applies one in a single tap.
See [DESIGN.md §7](DESIGN.md#7-surprise-feature-availability-calendar--nearest-available-dates).

---

## Contents

| Document | What it covers |
|----------|----------------|
| [SETUP_GUIDE.md](SETUP_GUIDE.md) | Detailed, step-by-step local setup with troubleshooting |
| [DESIGN.md](DESIGN.md) | Architecture, database, API, booking logic, concurrency, trade-offs |
| [ASSUMPTIONS.md](ASSUMPTIONS.md) | Every assumption made, including overlapping stays |
| [docs/API.md](docs/API.md) | Endpoint reference with request/response examples |
| [database/README.md](database/README.md) | Database scripts and migrations |
| [docs/screenshots/](docs/screenshots/) | Screenshots of the main flow |

---

## Features

- **Browse** all hotels with name, location, description, amenities and price per night.
- **Search** by city, area or hotel name, with check-in and check-out dates. Results show rooms left
  for those exact nights, can be sorted, and filtered to available hotels only.
- **Hotel page** with a sliding photo gallery (arrows, thumbnails, touch swipe), amenities, room
  details and a sticky booking panel.
- **Server-side quote**: total price (nights x price per night) is calculated by the backend and shown
  before confirming.
- **Book** one room with full name and email in a slide-in panel. Clear field-level validation.
- **Confirmation** page with a unique, easy-to-read reference (e.g. `SB-K7QM4XPA`) and a copy button.
- **Manage booking**: look up with reference + email, see details, cancel with a confirmation step.
  Cancelling releases the nights for other guests.
- **Safe under concurrency**: two guests can never both take the last room (row locking, tested).
- **Consistent API errors**: one JSON error shape with a stable `code` for every failure.
- **Responsive UI** with solid colours, square edges, scroll reveal animations, an auto-playing hero
  slider and a sliding featured-hotels carousel.

---

## Prerequisites

| Tool | Version | Check with |
|------|---------|-----------|
| JDK | 17 or newer (21 recommended) | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| MySQL Server | 8.0+ | `mysql --version` |
| Node.js | 20.19+ or 22.12+ | `node -v` |
| npm | 10+ | `npm -v` |

An internet connection is needed the first time (Maven and npm downloads, hotel photos and web
fonts are loaded from the web).

---

## Quick start

> Full walkthrough with screenshots of each step and troubleshooting: **[SETUP_GUIDE.md](SETUP_GUIDE.md)**.

### 1. Create the database and user

```bash
mysql -u root -p < database/01_create_database.sql
```

This creates the `hotel_booking` database and a `hotel_app` user. Open the file first and replace
`change_me` with your own password.

### 2. Configure the backend (no secrets in git)

```bash
cd backend
cp .env.example .env        # Windows: copy .env.example .env
```

Edit `backend/.env` and set `DB_PASSWORD` to the password you chose. `.env` is git-ignored.
You can also set `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` as real environment variables instead.

### 3. Run the backend

```bash
cd backend
mvn spring-boot:run
```

On first start Flyway creates the tables and loads 7 sample hotels plus a few demo bookings.
The API listens on **http://localhost:8080**. Check it: http://localhost:8080/api/hotels

### 4. Run the frontend

In a second terminal:

```bash
cd frontend
npm install
npm start
```

Open **http://localhost:4200**. The dev server proxies `/api` to the backend (see
`frontend/proxy.conf.json`), so no CORS setup is needed.

### 5. Run the tests

```bash
cd backend
mvn test
```

Tests use an in-memory H2 database in MySQL mode, so MySQL does not need to be running.

---

## Try the main flow

1. On the home page, search **Munnar** with check-in **today + 5** and check-out **today + 7**.
   Misty Hills Tea Bungalow is sold out on those nights (seeded demo data).
2. Open it. The booking panel says "Sold out" and lists the **nearest available dates**. Tap one.
3. Or scroll to the **availability calendar**, tap a check-in day, then a check-out day.
4. Check the total, press **Reserve this room**, enter a name and email, **Confirm booking**.
5. Copy the reference from the confirmation page.
6. Go to **Manage booking**, enter the reference and email, and **Cancel booking**. The calendar
   shows the night free again.

Demo booking you can look up straight away: reference **`SB-DEMA2345`**, email **`demo@staybook.test`**.

---

## Project structure

```
hotel-booking-portal/
├── backend/                         Spring Boot API
│   ├── pom.xml
│   ├── .env.example                 template for local secrets (copy to .env)
│   └── src/
│       ├── main/java/com/staybook/
│       │   ├── config/              AppProperties, Clock, CORS
│       │   ├── controller/          HotelController, BookingController
│       │   ├── dto/                 request/response records, ApiError
│       │   ├── entity/              Hotel, RoomType, RoomInventory, Booking
│       │   ├── exception/           typed exceptions + GlobalExceptionHandler
│       │   ├── repository/          Spring Data JPA repositories (incl. locking queries)
│       │   └── service/             BookingRules, AvailabilityService, BookingService,
│       │                            HotelService, AlternativeDateFinder, ReferenceGenerator
│       ├── main/resources/
│       │   ├── application.yml
│       │   └── db/migration/        V1 schema, V2 sample hotels, V3 demo bookings
│       └── test/                    unit, integration, concurrency and web tests
├── frontend/                        Angular app
│   ├── proxy.conf.json              /api -> http://localhost:8080
│   └── src/app/
│       ├── core/                    API service, models, date + error helpers
│       ├── shared/                  header, footer, search form, hotel card, directives
│       └── pages/                   home, hotels, hotel-detail, confirmation, manage-booking
├── database/                        create-database script and notes
├── docs/                            API reference, screenshots
├── README.md  DESIGN.md  SETUP_GUIDE.md  ASSUMPTIONS.md
```

---

## Configuration

All settings live in `backend/src/main/resources/application.yml` and can be overridden with
environment variables or `backend/.env`.

| Variable | Default | Purpose |
|----------|---------|---------|
| `DB_URL` | `jdbc:mysql://localhost:3306/hotel_booking?...` | JDBC URL |
| `DB_USERNAME` | `hotel_app` | Database user |
| `DB_PASSWORD` | *(empty)* | Database password – set it in `.env`, never commit it |
| `SERVER_PORT` | `8080` | API port |
| `APP_TIMEZONE` | `Asia/Kolkata` | Zone used to decide what "today" is |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Allowed browser origins if not using the proxy |

---

## Screenshots

See [docs/screenshots/](docs/screenshots/README.md) for the main flow: home, search results,
hotel page with availability calendar, booking panel, confirmation and manage/cancel.

---

## Not included (by design)

Authentication, payments, an admin panel and cloud deployment were out of scope. See
[DESIGN.md §8](DESIGN.md#8-limitations-and-improvements) for limitations and next steps.
