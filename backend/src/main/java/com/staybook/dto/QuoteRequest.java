package com.staybook.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record QuoteRequest(
        @NotNull(message = "Hotel is required.") @Positive Long hotelId,
        @NotNull(message = "Check-in date is required.") LocalDate checkIn,
        @NotNull(message = "Check-out date is required.") LocalDate checkOut
) { }
