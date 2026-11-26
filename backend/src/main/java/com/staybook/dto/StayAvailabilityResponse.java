package com.staybook.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Availability and price of the room for one specific stay. */
public record StayAvailabilityResponse(
        LocalDate checkIn,
        LocalDate checkOut,
        int nights,
        int availableRooms,
        boolean available,
        BigDecimal totalAmount
) { }
