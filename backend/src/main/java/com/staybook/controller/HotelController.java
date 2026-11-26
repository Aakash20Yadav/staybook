package com.staybook.controller;

import com.staybook.dto.AlternativeStayResponse;
import com.staybook.dto.DayAvailabilityResponse;
import com.staybook.dto.HotelDetailResponse;
import com.staybook.dto.HotelSummaryResponse;
import com.staybook.service.HotelService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    /** Browse or search hotels. With dates, each hotel also reports rooms left for that stay. */
    @GetMapping
    public List<HotelSummaryResponse> search(
            @RequestParam(required = false) @Size(max = 80, message = "Location is too long.") String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return hotelService.search(location, checkIn, checkOut);
    }

    /** Distinct cities, for the location suggestions in the search box. */
    @GetMapping("/locations")
    public List<String> locations() {
        return hotelService.locations();
    }

    @GetMapping("/{id}")
    public HotelDetailResponse detail(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return hotelService.getDetail(id, checkIn, checkOut);
    }

    /** Surprise feature: per-night availability for a date strip. */
    @GetMapping("/{id}/calendar")
    public List<DayAvailabilityResponse> calendar(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(defaultValue = "30")
            @Min(value = 1, message = "days must be at least 1")
            @Max(value = 62, message = "days can be at most 62") int days) {
        return hotelService.calendar(id, from, days);
    }

    /** Surprise feature: nearest available stays of the same length. */
    @GetMapping("/{id}/alternatives")
    public List<AlternativeStayResponse> alternatives(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return hotelService.alternatives(id, checkIn, checkOut);
    }
}
