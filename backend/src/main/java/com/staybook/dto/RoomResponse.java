package com.staybook.dto;

import java.math.BigDecimal;

public record RoomResponse(
        Long id,
        String name,
        String description,
        String bedType,
        int maxGuests,
        BigDecimal pricePerNight,
        int totalRooms
) { }
