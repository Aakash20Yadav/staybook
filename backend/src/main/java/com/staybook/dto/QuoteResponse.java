package com.staybook.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Price shown to the guest before they confirm. Always computed on the server. */
public record QuoteResponse(
        Long hotelId,
        String hotelName,
        String roomName,
        LocalDate checkIn,
        LocalDate checkOut,
        int nights,
        BigDecimal pricePerNight,
        BigDecimal totalAmount,
        String currency,
        int availableRooms,
        boolean available
) { }
