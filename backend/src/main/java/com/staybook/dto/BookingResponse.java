package com.staybook.dto;

import com.staybook.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record BookingResponse(
        String reference,
        BookingStatus status,
        Long hotelId,
        String hotelName,
        String hotelCity,
        String hotelAddress,
        String coverImageUrl,
        String roomName,
        String guestName,
        String guestEmail,
        LocalDate checkIn,
        LocalDate checkOut,
        int nights,
        BigDecimal pricePerNight,
        BigDecimal totalAmount,
        String currency,
        Instant createdAt,
        Instant cancelledAt,
        boolean cancellable
) { }
