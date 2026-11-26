package com.staybook.service;

import com.staybook.exception.BookingValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingRulesTest {

    private static final LocalDate TODAY = LocalDate.of(2030, 1, 10);
    private final BookingRules rules = new BookingRules(30, 365);

    @Nested
    @DisplayName("Price calculation")
    class Pricing {

        @ParameterizedTest(name = "{0} x {1} nights = {2}")
        @CsvSource({
                "6800.00, 1, 6800.00",
                "6800.00, 3, 20400.00",
                "4999.99, 7, 34999.93",
                "12500.50, 30, 375015.00"
        })
        void totalIsPricePerNightTimesNights(String price, int nights, String expected) {
            assertThat(BookingRules.totalFor(new BigDecimal(price), nights))
                    .isEqualByComparingTo(expected);
        }

        @Test
        void totalAlwaysHasTwoDecimals() {
            assertThat(BookingRules.totalFor(new BigDecimal("5200"), 2).scale()).isEqualTo(2);
        }

        @Test
        void zeroNightsIsRejected() {
            assertThatThrownBy(() -> BookingRules.totalFor(new BigDecimal("100"), 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void nightsCountCrossesMonthAndYearBoundaries() {
            assertThat(BookingRules.nightsBetween(LocalDate.of(2030, 12, 30), LocalDate.of(2031, 1, 2)))
                    .isEqualTo(3);
            assertThat(BookingRules.nightsBetween(LocalDate.of(2032, 2, 28), LocalDate.of(2032, 3, 1)))
                    .as("leap year").isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Date validation")
    class Dates {

        @Test
        void checkInTodayIsAllowed() {
            assertThat(rules.validateStay(TODAY, TODAY.plusDays(2), TODAY)).isEqualTo(2);
        }

        @Test
        void checkInInThePastIsRejected() {
            assertCode(() -> rules.validateStay(TODAY.minusDays(1), TODAY.plusDays(1), TODAY),
                    "CHECK_IN_IN_PAST");
        }

        @Test
        void checkOutOnSameDayIsRejected() {
            assertCode(() -> rules.validateStay(TODAY.plusDays(3), TODAY.plusDays(3), TODAY),
                    "CHECK_OUT_NOT_AFTER_CHECK_IN");
        }

        @Test
        void checkOutBeforeCheckInIsRejected() {
            assertCode(() -> rules.validateStay(TODAY.plusDays(5), TODAY.plusDays(2), TODAY),
                    "CHECK_OUT_NOT_AFTER_CHECK_IN");
        }

        @Test
        void missingDatesAreRejected() {
            assertCode(() -> rules.validateStay(null, TODAY.plusDays(2), TODAY), "DATES_REQUIRED");
        }

        @Test
        void staysLongerThanMaximumAreRejected() {
            assertThat(rules.validateStay(TODAY, TODAY.plusDays(30), TODAY)).isEqualTo(30);
            assertCode(() -> rules.validateStay(TODAY, TODAY.plusDays(31), TODAY), "STAY_TOO_LONG");
        }

        @Test
        void checkInBeyondBookingWindowIsRejected() {
            assertThat(rules.validateStay(TODAY.plusDays(365), TODAY.plusDays(366), TODAY)).isEqualTo(1);
            assertCode(() -> rules.validateStay(TODAY.plusDays(366), TODAY.plusDays(367), TODAY),
                    "CHECK_IN_TOO_FAR");
        }

        private void assertCode(Runnable call, String code) {
            assertThatThrownBy(call::run)
                    .isInstanceOf(BookingValidationException.class)
                    .extracting("code").isEqualTo(code);
        }
    }
}
