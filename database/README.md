# Database scripts

| File | Purpose |
|------|---------|
| `01_create_database.sql` | Creates the `hotel_booking` database and the `hotel_app` user. Run once by hand. |
| `../backend/src/main/resources/db/migration/V1__create_schema.sql` | Tables, keys and constraints. |
| `../backend/src/main/resources/db/migration/V2__sample_hotels.sql` | Seven sample hotels with amenities, images and one room type each. |
| `../backend/src/main/resources/db/migration/V3__demo_bookings.sql` | A few demo bookings so the sold-out and "limited" states are visible straight away. |

The `V*` scripts are **Flyway migrations**. They run automatically, in order, when the
backend starts, and Flyway records them in `flyway_schema_history` so each runs only once.
You do not need to run them by hand.

To reset everything:

```sql
DROP DATABASE hotel_booking;
CREATE DATABASE hotel_booking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Then start the backend again and the migrations re-create and re-seed the data.

Running the `V*` files by hand is not recommended: Flyway would then try to run them
again on startup. Let the backend apply them.
