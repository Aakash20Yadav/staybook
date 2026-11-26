package com.staybook.dto;

import java.util.List;

/** Full hotel page. {@code stay} is filled only when dates were supplied. */
public record HotelDetailResponse(
        Long id,
        String name,
        String city,
        String address,
        String description,
        int starRating,
        String coverImageUrl,
        List<String> images,
        List<String> amenities,
        String currency,
        RoomResponse room,
        StayAvailabilityResponse stay
) { }
