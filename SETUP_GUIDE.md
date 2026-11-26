# Staybook – Local Setup Guide

A step-by-step guide to running the hotel booking portal on your own machine, from a clean
computer to a working booking. Allow about 15 minutes the first time (most of it is downloads).

- [1. What you need](#1-what-you-need)
- [2. Get the code](#2-get-the-code)
- [3. Set up MySQL](#3-set-up-mysql)
- [4. Configure the backend](#4-configure-the-backend)
- [5. Start the backend](#5-start-the-backend)
- [6. Start the frontend](#6-start-the-frontend)
- [7. Check everything works](#7-check-everything-works)
- [8. Run the automated tests](#8-run-the-automated-tests)
- [9. Reset or reseed the data](#9-reset-or-reseed-the-data)
- [10. Troubleshooting](#10-troubleshooting)
- [11. Production build (optional)](#11-production-build-optional)

---

## 1. What you need

| Tool | Version | Download | Check |
|------|---------|----------|-------|
| Git | any recent | https://git-scm.com | `git --version` |
| JDK | **17 or newer** (21 LTS recommended) | https://adoptium.net | `java -version` |
| Maven | **3.9+** | https://maven.apache.org/download.cgi | `mvn -v` |
| MySQL Server | **8.0+** | https://dev.mysql.com/downloads/mysql/ | `mysql --version` |
| Node.js | **20.19+** or **22.12+** (LTS) | https://nodejs.org | `node -v` |
| npm | 10+ (comes with Node) | – | `npm -v` |

You also need an internet connection the first time, for Maven and npm downloads. Hotel photos
and web fonts are loaded from the internet when you use the app.

> **Windows users:** run commands in PowerShell or Git Bash. Where a command differs it is shown
> separately. Make sure `java`, `mvn`, `mysql` and `node` are on your `PATH` (open a new terminal
> after installing).

> **macOS (Homebrew) shortcut:** `brew install openjdk@21 maven mysql node` then
> `brew services start mysql`.

---

## 2. Get the code

```bash
git clone https://github.com/Aakash20Yadav/staybook.git
cd staybook
```

You should see `backend/`, `frontend/`, `database/` and the `.md` documents.

---

## 3. Set up MySQL

### 3.1 Make sure MySQL is running

| OS | Command |
|----|---------|
| macOS (Homebrew) | `brew services start mysql` |
| Linux (systemd) | `sudo systemctl start mysql` |
| Windows | Services app → **MySQL80** → Start (or `net start MySQL80` as admin) |

Check: `mysql -u root -p -e "SELECT VERSION();"` should print a version number.

### 3.2 Choose a password for the app user

Open `database/01_create_database.sql` and replace `change_me` with a password of your own.
Do **not** commit this change.

### 3.3 Create the database and user

```bash
mysql -u root -p < database/01_create_database.sql
```

(Windows PowerShell: `Get-Content database\01_create_database.sql | mysql -u root -p`)

This creates:

- database **`hotel_booking`** (utf8mb4)
- user **`hotel_app`**@`localhost` with full rights on that database only

Check:

```bash
mysql -u hotel_app -p -e "SHOW DATABASES;"
```

You should see `hotel_booking` in the list. **You do not create any tables by hand**: the backend
does that in step 5.

---

## 4. Configure the backend

Credentials are never stored in the repository. The backend reads them from environment variables,
or from a local `backend/.env` file that git ignores.

```bash
cd backend
cp .env.example .env          # Windows: copy .env.example .env
```

Open `backend/.env` and set your password:

```properties
DB_URL=jdbc:mysql://localhost:3306/hotel_booking?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=hotel_app
DB_PASSWORD=the_password_you_chose_in_3.2
APP_TIMEZONE=Asia/Kolkata
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

<details>
<summary>Prefer real environment variables instead of a .env file?</summary>

macOS / Linux:
```bash
export DB_USERNAME=hotel_app
export DB_PASSWORD='your_password'
```
Windows PowerShell:
```powershell
$env:DB_USERNAME = "hotel_app"
$env:DB_PASSWORD = "your_password"
```
Environment variables take priority over `.env`.
</details>

If your MySQL runs on another port or host, change `DB_URL` accordingly.

---

## 5. Start the backend

From the `backend` folder:

```bash
mvn spring-boot:run
```

The first run downloads dependencies (a few minutes). Then look for these lines:

```
Migrating schema `hotel_booking` to version "1 - create schema"
Migrating schema `hotel_booking` to version "2 - sample hotels"
Migrating schema `hotel_booking` to version "3 - demo bookings"
Successfully applied 3 migrations to schema `hotel_booking`
Tomcat started on port 8080 (http)
Started StaybookApplication in ... seconds
```

Flyway has now created all tables and loaded 7 hotels and 5 demo bookings. On later starts it
skips migrations that already ran.

Quick API check (new terminal or browser):

```bash
curl http://localhost:8080/api/hotels/locations
# ["Bengaluru","Goa","Jaipur","Manali","Mumbai","Munnar","Udaipur"]
```

Leave this terminal running.

<details>
<summary>Run as a JAR instead</summary>

```bash
mvn clean package
java -jar target/staybook-api-1.0.0.jar
```
Run the JAR from the `backend` folder so it finds `.env`.
</details>

---

## 6. Start the frontend

Open a **second terminal**:

```bash
cd frontend
npm install
npm start
```

When you see `Local: http://localhost:4200/`, open **http://localhost:4200** in your browser.

The Angular dev server forwards every `/api/...` call to `http://localhost:8080`
(`frontend/proxy.conf.json`), so the backend must be running.

---

## 7. Check everything works

Follow the main flow once:

| Step | Do | Expect |
|------|----|--------|
| 1 | Open http://localhost:4200 | Hero slider, search box, featured hotels carousel |
| 2 | Search with an empty location | All 7 hotels; with dates, a "rooms left" badge on each |
| 3 | Search **Munnar**, check-in today+5, check-out today+7 | Misty Hills shows **Sold out** |
| 4 | Open Misty Hills | Panel says sold out and lists **nearest dates with a room** |
| 5 | Tap a suggestion, or pick two days in the **availability calendar** | Total price appears |
| 6 | **Reserve this room** → enter name + email → **Confirm booking** | Confirmation page with a reference like `SB-K7QM4XPA` |
| 7 | **Manage booking** → enter reference + email → **Find booking** | Booking details |
| 8 | **Cancel booking** → **Yes, cancel it** | Status becomes Cancelled; the nights are free again in the calendar |

Seeded booking you can look up immediately: **`SB-DEMA2345`** / **`demo@staybook.test`**.

Things worth trying to see validation:

- Check-out before check-in, or a check-in in the past → clear message, nothing is sent.
- An invalid email in the booking form → message under the field (also enforced by the API).
- Book the last room of Misty Hills in two browser tabs at the same time → one succeeds, the
  other gets "no rooms left" and fresh suggestions.

---

## 8. Run the automated tests

```bash
cd backend
mvn test
```

The tests use an in-memory H2 database in MySQL mode, so MySQL does not need to be running.
Expected result: `Tests run: 30+, Failures: 0, Errors: 0`.

| Test class | What it proves |
|-----------|----------------|
| `BookingRulesTest` | Price = price × nights (with rounding, month/year/leap-year boundaries); every date rule and its error code |
| `AlternativeDateFinderTest` | Suggestions are nearest first, skip sold-out and past dates, report fewest rooms left |
| `BookingServiceIntegrationTest` | Capacity across overlapping stays, back-to-back stays allowed, failed booking takes nothing, cancel releases nights, double cancel rejected, email check, server-side total, **8 simultaneous requests for 1 room → exactly 1 succeeds** |
| `BookingControllerTest` | Consistent JSON error format: 400 with field errors, malformed date, 409 when sold out |

---

## 9. Reset or reseed the data

To start from a clean database (all bookings removed, demo data dated from today again):

```sql
-- mysql -u root -p
DROP DATABASE hotel_booking;
CREATE DATABASE hotel_booking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON hotel_booking.* TO 'hotel_app'@'localhost';
```

Restart the backend; Flyway recreates and reseeds everything.

---

## 10. Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `Access denied for user 'hotel_app'@'localhost'` | Wrong or missing password | Check `DB_PASSWORD` in `backend/.env` matches step 3.2. Make sure you run `mvn` from the `backend` folder so `.env` is found. |
| `Communications link failure` | MySQL not running or wrong port | Start MySQL (3.1). Check the port in `DB_URL`. |
| `Unknown database 'hotel_booking'` | Step 3.3 not run | Run `database/01_create_database.sql`. |
| `Public Key Retrieval is not allowed` | MySQL 8 auth with SSL off | Keep `allowPublicKeyRetrieval=true` in `DB_URL` (already in the default). |
| `Port 8080 was already in use` | Another app on 8080 | Set `SERVER_PORT=8081` in `.env` **and** change `target` in `frontend/proxy.conf.json` to `http://localhost:8081`. |
| `Validate failed: Migration checksum mismatch` | A migration file was edited after it ran | Reset the database (section 9). Never edit applied migrations; add a new `V4__...sql`. |
| Frontend says "We could not reach the server" | Backend not running, or not started with the proxy | Start the backend; start the frontend with `npm start` (which uses the proxy). |
| `The Angular CLI requires a minimum Node.js version` | Old Node | Install Node 20.19+ or 22.12+. |
| Hotel photos show a green placeholder | No internet or image blocked | Expected offline; everything else works. |
| Calendar shows no sold-out days | Demo bookings were seeded on an earlier date | Reset the data (section 9) to re-date them from today. |
| Dates look off by one | Browser time zone differs a lot from `APP_TIMEZONE` | Set `APP_TIMEZONE` to your zone in `.env`. |

---

## 11. Production build (optional)

```bash
# Frontend: static files in frontend/dist/frontend/browser
cd frontend && npm run build

# Backend: runnable JAR
cd backend && mvn clean package
```

Serve the frontend files from any static web server that forwards `/api` to the backend (for
example an Nginx `location /api { proxy_pass http://localhost:8080; }`), or set
`CORS_ALLOWED_ORIGINS` to the frontend's origin if they are served from different hosts.
