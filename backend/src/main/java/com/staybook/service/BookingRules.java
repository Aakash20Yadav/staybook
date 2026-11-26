package com.staybook.service;

import com.staybook.config.AppProperties;
import com.staybook.exception.BookingValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Pure booking rules: date validation and price calculation. No database access,
 * so the rules are easy to unit test.
 *
 * A stay is the half-open range [checkIn, checkOut): the guest occupies the
 * nights starting on checkIn up to, but not including, checkOut.
 */
@Component
public class BookingRules {

    private final int maxNights;
    private final int maxDaysInAdvance;

    @Autowired
    public BookingRules(AppProperties properties) {
        this(properties.booking().maxNights(), properties.booking().maxDaysInAdvance());
    }

    public BookingRules(int maxNights, int maxDaysInAdvance) {
        this.maxNights = maxNights;
        this.maxDaysInAdvance = maxDaysInAdvance;
    }

    /**
     * Validates a stay against today's date and returns its number of nights.
     *
     * @throws BookingValidationException with a specific code when a rule is broken
     */
    public int validateStay(LocalDate checkIn, LocalDate checkOut, LocalDate today) {
        if (checkIn == null || checkOut == null) {
            throw new BookingValidationException("DATES_REQUIRED",
                    "Please choose both check-in and check-out dates.");
        }
        if (checkIn.isBefore(today)) {
            throw new BookingValidationException("CHECK_IN_IN_PAST",
                    "Check-in date cannot be in the past.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new BookingValidationException("CHECK_OUT_NOT_AFTER_CHECK_IN",
                    "Check-out date must be after the check-in date.");
        }
        if (checkIn.isAfter(today.plusDays(maxDaysInAdvance))) {
            throw new BookingValidationException("CHECK_IN_TOO_FAR",
                    "Bookings open " + maxDaysInAdvance + " days in advance. Please choose an earlier check-in date.");
        }
        int nights = nightsBetween(checkIn, checkOut);
        if (nights > maxNights) {
            throw new BookingValidationException("STAY_TOO_LONG",
                    "A single booking can be at most " + maxNights + " nights.");
        }
        return nights;
    }

    public boolean isWithinBookingWindow(LocalDate checkIn, LocalDate today) {
        return !checkIn.isBefore(today) && !checkIn.isAfter(today.plusDays(maxDaysInAdvance));
    }

    public static int nightsBetween(LocalDate checkIn, LocalDate checkOut) {
        return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    /** Total = price per night x nights, rounded to 2 decimal places. */
    public static BigDecimal totalFor(BigDecimal pricePerNight, int nights) {
        if (nights <= 0) {
            throw new IllegalArgumentException("nights must be positive");
        }
        return pricePerNight.multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);
    }

    public int getMaxNights() { return maxNights; }
    public int getMaxDaysInAdvance() { return maxDaysInAdvance; }
}
