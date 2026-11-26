-- =====================================================================
-- Staybook schema (MySQL 8.0+)
-- A stay is the half-open range [check_in, check_out): the nights booked
-- are check_in .. check_out - 1. room_inventory tracks one row per night.
-- =====================================================================

CREATE TABLE hotel (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    name             VARCHAR(120) NOT NULL,
    city             VARCHAR(80)  NOT NULL,
    address          VARCHAR(255) NOT NULL,
    description      TEXT         NOT NULL,
    star_rating      INT          NOT NULL,
    cover_image_url  VARCHAR(500) NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT chk_hotel_star_rating CHECK (star_rating BETWEEN 1 AND 5),
    INDEX idx_hotel_city (city)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE hotel_amenity (
    hotel_id  BIGINT      NOT NULL,
    amenity   VARCHAR(60) NOT NULL,
    PRIMARY KEY (hotel_id, amenity),
    CONSTRAINT fk_amenity_hotel FOREIGN KEY (hotel_id) REFERENCES hotel (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE hotel_image (
    hotel_id    BIGINT       NOT NULL,
    sort_order  INT          NOT NULL,
    image_url   VARCHAR(500) NOT NULL,
    PRIMARY KEY (hotel_id, sort_order),
    CONSTRAINT fk_image_hotel FOREIGN KEY (hotel_id) REFERENCES hotel (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- One bookable room category per hotel (uk_room_type_hotel). Drop that key
-- to allow several categories; the rest of the schema already supports it.
CREATE TABLE room_type (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    hotel_id         BIGINT        NOT NULL,
    name             VARCHAR(80)   NOT NULL,
    description      VARCHAR(500)  NULL,
    bed_type         VARCHAR(60)   NULL,
    max_guests       INT           NOT NULL DEFAULT 2,
    price_per_night  DECIMAL(10,2) NOT NULL,
    total_rooms      INT           NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_room_type_hotel UNIQUE (hotel_id),
    CONSTRAINT fk_room_type_hotel FOREIGN KEY (hotel_id) REFERENCES hotel (id) ON DELETE CASCADE,
    CONSTRAINT chk_room_price_positive CHECK (price_per_night > 0),
    CONSTRAINT chk_room_total_positive CHECK (total_rooms > 0),
    CONSTRAINT chk_room_guests_positive CHECK (max_guests > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Rooms taken per room type per night. A missing row means nothing is booked.
-- booked_count <= total_rooms is enforced by the application under a row lock
-- (MySQL CHECK constraints cannot reference another table).
CREATE TABLE room_inventory (
    id            BIGINT NOT NULL AUTO_INCREMENT,
    room_type_id  BIGINT NOT NULL,
    stay_date     DATE   NOT NULL,
    booked_count  INT    NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_inventory_room_date UNIQUE (room_type_id, stay_date),
    CONSTRAINT fk_inventory_room_type FOREIGN KEY (room_type_id) REFERENCES room_type (id) ON DELETE CASCADE,
    CONSTRAINT chk_inventory_non_negative CHECK (booked_count >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE booking (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    reference        VARCHAR(12)   NOT NULL,
    hotel_id         BIGINT        NOT NULL,
    room_type_id     BIGINT        NOT NULL,
    guest_name       VARCHAR(100)  NOT NULL,
    guest_email      VARCHAR(150)  NOT NULL,
    check_in         DATE          NOT NULL,
    check_out        DATE          NOT NULL,
    nights           INT           NOT NULL,
    price_per_night  DECIMAL(10,2) NOT NULL,
    total_amount     DECIMAL(12,2) NOT NULL,
    status           VARCHAR(20)   NOT NULL,
    created_at       DATETIME(6)   NOT NULL,
    cancelled_at     DATETIME(6)   NULL,
    version          BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_booking_reference UNIQUE (reference),
    CONSTRAINT fk_booking_hotel FOREIGN KEY (hotel_id) REFERENCES hotel (id),
    CONSTRAINT fk_booking_room_type FOREIGN KEY (room_type_id) REFERENCES room_type (id),
    CONSTRAINT chk_booking_dates CHECK (check_out > check_in),
    CONSTRAINT chk_booking_nights CHECK (nights > 0),
    CONSTRAINT chk_booking_amount CHECK (total_amount > 0),
    CONSTRAINT chk_booking_status CHECK (status IN ('CONFIRMED', 'CANCELLED')),
    INDEX idx_booking_email (guest_email),
    INDEX idx_booking_room_dates (room_type_id, check_in, check_out)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
