package com.staybook.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CancelBookingRequest(
        @NotBlank(message = "Email is required to cancel a booking.")
        @Email(message = "Please enter a valid email address.")
        String email
) { }
