package com.staybook.service;

import com.staybook.dto.BookingResponse;
import com.staybook.dto.CreateBookingRequest;
import com.staybook.dto.QuoteRequest;
import com.staybook.entity.BookingStatus;
import com.staybook.entity.Hotel;
import com.staybook.entity.RoomType;
import com.staybook.exception.BookingStateException;
import com.staybook.exception.BookingValidationException;
import com.staybook.exception.NotFoundException;
import com.staybook.exception.RoomUnavailableException;
import com.staybook.repository.BookingRepository;
import com.staybook.repository.HotelRepository;
import com.staybook.repository.RoomInventoryRepository;
import com.staybook.repository.RoomTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end booking rules against a real (in-memory H2, MySQL mode) database.
 * "Today" is fixed at 2030-01-10 so the tests never depend on the real date.
 */
@SpringBootTest
@ActiveProfiles("test")
class BookingServiceIntegrationTest {

    static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    static final LocalDate TODAY = LocalDate.of(2030, 1, 10);

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(TODAY.atTime(10, 0).atZone(ZONE).toInstant(), ZONE);
        }
    }

    @Autowired BookingService bookingService;
    @Autowired HotelService hotelService;
    @Autowired HotelRepository hotelRepository;
    @Autowired RoomTypeRepository roomTypeRepository;
    @Autowired RoomInventoryRepository inventoryRepository;
    @Autowired BookingRepository bookingRepository;

    private Long hotelId;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        inventoryRepository.deleteAll();
        roomTypeRepository.deleteAll();
        hotelRepository.deleteAll();
        hotelId = createHotel(2, "5200.00");
    }

    private Long createHotel(int rooms, String price) {
        Hotel hotel = hotelRepository.save(new Hotel("Test Hotel", "Munnar", "1 Tea Road",
                "A test hotel", 3, "https://example.com/cover.jpg"));
        roomTypeRepository.save(new RoomType(hotel, "Cottage", "Cosy", "1 Queen bed", 2,
                new BigDecimal(price), rooms));
        return hotel.getId();
    }

    private CreateBookingRequest request(int fromDay, int toDay, String email) {
        return new CreateBookingRequest(hotelId, TODAY.plusDays(fromDay), TODAY.plusDays(toDay),
                "Asha Kumar", email);
    }

    private int roomsLeft(int fromDay, int toDay) {
        return hotelService.getDetail(hotelId, TODAY.plusDays(fromDay), TODAY.plusDays(toDay))
                .stay().availableRooms();
    }

    @Test
    void totalIsCalculatedOnTheServerFromNightsAndPrice() {
        var quote = bookingService.quote(new QuoteRequest(hotelId, TODAY.plusDays(3), TODAY.plusDays(6)));
        assertThat(quote.nights()).isEqualTo(3);
        assertThat(quote.totalAmount()).isEqualByComparingTo("15600.00");

        BookingResponse booking = bookingService.create(request(3, 6, "asha@example.com"));
        assertThat(booking.totalAmount()).isEqualByComparingTo("15600.00");
        assertThat(booking.reference()).matches("SB-[A-Z2-9]{8}");
        assertThat(booking.status()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void cannotBookBeyondCapacityForOverlappingNights() {
        bookingService.create(request(5, 8, "a@example.com"));
        bookingService.create(request(6, 7, "b@example.com"));   // overlaps night 6

        assertThat(roomsLeft(6, 7)).isZero();
        assertThat(roomsLeft(5, 6)).isEqualTo(1);

        assertThatThrownBy(() -> bookingService.create(request(4, 7, "c@example.com")))
                .isInstanceOf(RoomUnavailableException.class)
                .extracting("soldOutNight").isEqualTo(TODAY.plusDays(6));
    }

    @Test
    void failedBookingDoesNotTakeAnyNight() {
        bookingService.create(request(6, 7, "a@example.com"));
        bookingService.create(request(6, 7, "b@example.com"));   // night 6 now full

        assertThatThrownBy(() -> bookingService.create(request(4, 8, "c@example.com")))
                .isInstanceOf(RoomUnavailableException.class);

        // Nights 4, 5 and 7 must be untouched (transaction rolled back).
        assertThat(roomsLeft(4, 6)).isEqualTo(2);
        assertThat(roomsLeft(7, 8)).isEqualTo(2);
    }

    @Test
    void checkOutDayCanBeSomeoneElsesCheckInDay() {
        bookingService.create(request(5, 7, "a@example.com"));
        bookingService.create(request(5, 7, "b@example.com"));   // nights 5,6 full

        BookingResponse backToBack = bookingService.create(request(7, 9, "c@example.com"));
        assertThat(backToBack.status()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void cancellingReleasesTheNights() {
        BookingResponse first = bookingService.create(request(5, 8, "a@example.com"));
        bookingService.create(request(5, 8, "b@example.com"));
        assertThat(roomsLeft(5, 8)).isZero();

        BookingResponse cancelled = bookingService.cancel(first.reference(), "A@Example.com");
        assertThat(cancelled.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(roomsLeft(5, 8)).isEqualTo(1);

        assertThat(bookingService.create(request(5, 8, "c@example.com")).status())
                .isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void cancellingTwiceIsRejected() {
        BookingResponse booking = bookingService.create(request(5, 6, "a@example.com"));
        bookingService.cancel(booking.reference(), "a@example.com");

        assertThatThrownBy(() -> bookingService.cancel(booking.reference(), "a@example.com"))
                .isInstanceOf(BookingStateException.class)
                .extracting("code").isEqualTo("BOOKING_ALREADY_CANCELLED");
        assertThat(roomsLeft(5, 6)).isEqualTo(2);  // released once, not twice
    }

    @Test
    void lookupNeedsMatchingEmail() {
        BookingResponse booking = bookingService.create(request(5, 6, "a@example.com"));

        assertThat(bookingService.find(booking.reference().toLowerCase(), " A@EXAMPLE.COM ").reference())
                .isEqualTo(booking.reference());
        assertThatThrownBy(() -> bookingService.find(booking.reference(), "someone@else.com"))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> bookingService.cancel(booking.reference(), "someone@else.com"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void pastCheckInIsRejectedBeforeAnythingIsReserved() {
        assertThatThrownBy(() -> bookingService.create(request(-1, 2, "a@example.com")))
                .isInstanceOf(BookingValidationException.class);
        assertThat(inventoryRepository.count()).isZero();
    }

    @Test
    void alternativesAreOfferedAroundASoldOutNight() {
        bookingService.create(request(5, 6, "a@example.com"));
        bookingService.create(request(5, 6, "b@example.com"));

        var alternatives = hotelService.alternatives(hotelId, TODAY.plusDays(5), TODAY.plusDays(7));
        assertThat(alternatives).isNotEmpty();
        assertThat(alternatives.get(0).checkIn()).isEqualTo(TODAY.plusDays(6));
        assertThat(alternatives).allSatisfy(a -> assertThat(a.totalAmount()).isEqualByComparingTo("10400.00"));
    }

    @Test
    void onlyOneOfManySimultaneousRequestsGetsTheLastRoom() throws Exception {
        hotelId = createHotel(1, "4000.00");
        int guests = 8;
        ExecutorService pool = Executors.newFixedThreadPool(guests);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> results = new ArrayList<>();

        for (int i = 0; i < guests; i++) {
            String email = "guest" + i + "@example.com";
            Callable<String> task = () -> {
                start.await();
                try {
                    bookingService.create(request(20, 23, email));
                    return "BOOKED";
                } catch (RoomUnavailableException e) {
                    return "SOLD_OUT";
                }
            };
            results.add(pool.submit(task));
        }
        start.countDown();

        int booked = 0;
        int soldOut = 0;
        for (Future<String> f : results) {
            if (f.get().equals("BOOKED")) booked++; else soldOut++;
        }
        pool.shutdown();

        assertThat(booked).isEqualTo(1);
        assertThat(soldOut).isEqualTo(guests - 1);
        assertThat(roomsLeft(20, 23)).isZero();
    }
}
