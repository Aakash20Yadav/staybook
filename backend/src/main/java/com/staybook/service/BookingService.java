package com.staybook.service;

import com.staybook.config.AppProperties;
import com.staybook.dto.BookingResponse;
import com.staybook.dto.CreateBookingRequest;
import com.staybook.dto.QuoteRequest;
import com.staybook.dto.QuoteResponse;
import com.staybook.entity.Booking;
import com.staybook.entity.RoomType;
import com.staybook.exception.BookingStateException;
import com.staybook.exception.NotFoundException;
import com.staybook.repository.BookingRepository;
import com.staybook.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;

@Service
public class BookingService {

    private static final int MAX_REFERENCE_ATTEMPTS = 5;

    private final BookingRepository bookingRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final AvailabilityService availabilityService;
    private final BookingRules rules;
    private final ReferenceGenerator referenceGenerator;
    private final Clock clock;
    private final String currency;

    public BookingService(BookingRepository bookingRepository, RoomTypeRepository roomTypeRepository,
                          AvailabilityService availabilityService, BookingRules rules,
                          ReferenceGenerator referenceGenerator, Clock clock, AppProperties properties) {
        this.bookingRepository = bookingRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.availabilityService = availabilityService;
        this.rules = rules;
        this.referenceGenerator = referenceGenerator;
        this.clock = clock;
        this.currency = properties.currency();
    }

    /** Price and availability shown before the guest confirms. Changes nothing. */
    @Transactional(readOnly = true)
    public QuoteResponse quote(QuoteRequest request) {
        int nights = rules.validateStay(request.checkIn(), request.checkOut(), today());
        RoomType room = roomTypeRepository.findFirstByHotel_IdOrderByIdAsc(request.hotelId())
                .orElseThrow(BookingService::hotelNotFound);
        int left = availabilityService.availableRooms(room, request.checkIn(), request.checkOut());
        return new QuoteResponse(room.getHotelId(), room.getHotel().getName(), room.getName(),
                request.checkIn(), request.checkOut(), nights, room.getPricePerNight(),
                BookingRules.totalFor(room.getPricePerNight(), nights), currency, left, left > 0);
    }

    /**
     * Books one room. Steps, all in one transaction:
     * validate dates -> lock the room type row -> check and take every night
     * -> compute price on the server -> save with a unique reference.
     */
    @Transactional
    public BookingResponse create(CreateBookingRequest request) {
        int nights = rules.validateStay(request.checkIn(), request.checkOut(), today());

        RoomType room = roomTypeRepository.findByHotelIdForUpdate(request.hotelId()).stream()
                .findFirst()
                .orElseThrow(BookingService::hotelNotFound);

        availabilityService.reserve(room, request.checkIn(), request.checkOut());

        BigDecimal total = BookingRules.totalFor(room.getPricePerNight(), nights);
        Booking booking = new Booking(uniqueReference(), room,
                request.guestName().trim(),
                request.guestEmail().trim().toLowerCase(Locale.ROOT),
                request.checkIn(), request.checkOut(), nights,
                room.getPricePerNight(), total, Instant.now(clock));
        bookingRepository.save(booking);
        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse find(String reference, String email) {
        return toResponse(load(reference, email));
    }

    /**
     * Cancels a booking and gives its nights back to inventory.
     * Booking has a @Version column, so if two cancel requests race, the
     * second fails instead of releasing the same nights twice.
     */
    @Transactional
    public BookingResponse cancel(String reference, String email) {
        Booking booking = load(reference, email);
        if (booking.isCancelled()) {
            throw new BookingStateException("BOOKING_ALREADY_CANCELLED",
                    "This booking has already been cancelled.");
        }
        if (booking.getCheckIn().isBefore(today())) {
            throw new BookingStateException("BOOKING_NOT_CANCELLABLE",
                    "This stay has already started, so it can no longer be cancelled online.");
        }
        RoomType room = roomTypeRepository.findByIdForUpdate(booking.getRoomType().getId())
                .orElseThrow(BookingService::hotelNotFound);
        availabilityService.release(room, booking.getCheckIn(), booking.getCheckOut());
        booking.cancel(Instant.now(clock));
        bookingRepository.saveAndFlush(booking);
        return toResponse(booking);
    }

    /** Same "not found" answer whether the reference or the email is wrong, so references can't be probed. */
    private Booking load(String reference, String email) {
        String ref = reference == null ? "" : reference.trim().toUpperCase(Locale.ROOT);
        String mail = email == null ? "" : email.trim();
        return bookingRepository.findByReference(ref)
                .filter(b -> b.getGuestEmail().equalsIgnoreCase(mail))
                .orElseThrow(() -> new NotFoundException("BOOKING_NOT_FOUND",
                        "We couldn't find a booking with that reference and email address."));
    }

    private String uniqueReference() {
        for (int i = 0; i < MAX_REFERENCE_ATTEMPTS; i++) {
            String candidate = referenceGenerator.next();
            if (!bookingRepository.existsByReference(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not generate a unique booking reference");
    }

    private BookingResponse toResponse(Booking b) {
        boolean cancellable = !b.isCancelled() && !b.getCheckIn().isBefore(today());
        return new BookingResponse(b.getReference(), b.getStatus(), b.getHotel().getId(),
                b.getHotel().getName(), b.getHotel().getCity(), b.getHotel().getAddress(),
                b.getHotel().getCoverImageUrl(), b.getRoomType().getName(),
                b.getGuestName(), b.getGuestEmail(), b.getCheckIn(), b.getCheckOut(), b.getNights(),
                b.getPricePerNight(), b.getTotalAmount(), currency, b.getCreatedAt(), b.getCancelledAt(),
                cancellable);
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private static NotFoundException hotelNotFound() {
        return new NotFoundException("HOTEL_NOT_FOUND", "Hotel not found.");
    }
}
