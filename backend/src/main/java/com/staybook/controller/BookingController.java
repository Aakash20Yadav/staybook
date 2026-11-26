package com.staybook.controller;

import com.staybook.dto.BookingResponse;
import com.staybook.dto.CancelBookingRequest;
import com.staybook.dto.CreateBookingRequest;
import com.staybook.dto.QuoteRequest;
import com.staybook.dto.QuoteResponse;
import com.staybook.service.BookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /** Server-side price and availability for a stay, shown before confirming. */
    @PostMapping("/quote")
    public QuoteResponse quote(@Valid @RequestBody QuoteRequest request) {
        return bookingService.quote(request);
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse booking = bookingService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{reference}").buildAndExpand(booking.reference()).toUri();
        return ResponseEntity.created(location).body(booking);
    }

    /** Look up a booking. The email acts as a lightweight ownership check. */
    @GetMapping("/{reference}")
    public BookingResponse find(
            @PathVariable String reference,
            @RequestParam @NotBlank(message = "Email is required.")
            @Email(message = "Please enter a valid email address.") String email) {
        return bookingService.find(reference, email);
    }

    /** POST rather than DELETE: the booking is kept with status CANCELLED, not removed. */
    @PostMapping("/{reference}/cancel")
    public BookingResponse cancel(@PathVariable String reference,
                                  @Valid @RequestBody CancelBookingRequest request) {
        return bookingService.cancel(reference, request.email());
    }
}
