package com.staybook.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Typed view of the {@code app.*} settings in application.yml.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String timezone,
        String currency,
        Cors cors,
        Booking booking
) {
    public record Cors(List<String> allowedOrigins) { }

    public record Booking(int maxNights, int maxDaysInAdvance) { }
}
