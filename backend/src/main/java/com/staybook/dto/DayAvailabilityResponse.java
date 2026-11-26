package com.staybook.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One cell of the availability calendar (surprise feature). */
public record DayAvailabilityResponse(
        LocalDate date,
        int availableRooms,
        int totalRooms,
        Level level,
        BigDecimal pricePerNight
) {
    public enum Level { AVAILABLE, LIMITED, SOLD_OUT }
}
