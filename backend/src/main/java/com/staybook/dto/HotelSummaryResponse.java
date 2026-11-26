package com.staybook.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * A hotel in a list. {@code availableRooms} is filled only when the caller
 * searched with dates; otherwise it is null.
 */
public record HotelSummaryResponse(
        Long id,
        String name,
        String city,
        String address,
        String description,
        int starRating,
        String coverImageUrl,
        List<String> amenities,
        String roomName,
        BigDecimal pricePerNight,
        String currency,
        int totalRooms,
        Integer availableRooms
) { }
