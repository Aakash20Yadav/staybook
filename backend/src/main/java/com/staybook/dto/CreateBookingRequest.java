package com.staybook.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Note there is no price field: the client cannot influence the amount charged.
 */
public record CreateBookingRequest(
        @NotNull(message = "Hotel is required.") @Positive Long hotelId,
        @NotNull(message = "Check-in date is required.") LocalDate checkIn,
        @NotNull(message = "Check-out date is required.") LocalDate checkOut,

        @NotBlank(message = "Please enter your full name.")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters.")
        @Pattern(regexp = "^[\\p{L} .'-]+$",
                message = "Name can contain letters, spaces, apostrophes, dots and hyphens only.")
        String guestName,

        @NotBlank(message = "Please enter your email address.")
        @Email(message = "Please enter a valid email address.")
        @Size(max = 150, message = "Email must be at most 150 characters.")
        String guestEmail
) { }
