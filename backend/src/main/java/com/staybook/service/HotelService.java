package com.staybook.service;

import com.staybook.config.AppProperties;
import com.staybook.dto.AlternativeStayResponse;
import com.staybook.dto.DayAvailabilityResponse;
import com.staybook.dto.HotelDetailResponse;
import com.staybook.dto.HotelSummaryResponse;
import com.staybook.dto.RoomResponse;
import com.staybook.dto.StayAvailabilityResponse;
import com.staybook.entity.Hotel;
import com.staybook.entity.RoomType;
import com.staybook.exception.BookingValidationException;
import com.staybook.exception.NotFoundException;
import com.staybook.repository.HotelRepository;
import com.staybook.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class HotelService {

    /** How far either side of the requested dates we look for alternatives. */
    static final int ALTERNATIVE_SEARCH_DAYS = 14;
    static final int ALTERNATIVE_LIMIT = 4;

    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final AvailabilityService availabilityService;
    private final BookingRules rules;
    private final Clock clock;
    private final String currency;

    public HotelService(HotelRepository hotelRepository, RoomTypeRepository roomTypeRepository,
                        AvailabilityService availabilityService, BookingRules rules,
                        Clock clock, AppProperties properties) {
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.availabilityService = availabilityService;
        this.rules = rules;
        this.clock = clock;
        this.currency = properties.currency();
    }

    public List<HotelSummaryResponse> search(String location, LocalDate checkIn, LocalDate checkOut) {
        boolean hasDates = validateOptionalDates(checkIn, checkOut);

        String cleaned = location == null ? "" : location.replaceAll("[%_]", "").trim();
        List<Hotel> hotels = cleaned.isEmpty()
                ? hotelRepository.findAllByOrderByNameAsc()
                : hotelRepository.searchByLocation("%" + cleaned.toLowerCase(Locale.ROOT) + "%");
        if (hotels.isEmpty()) {
            return List.of();
        }

        Map<Long, RoomType> roomByHotel = roomTypeRepository
                .findByHotel_IdIn(hotels.stream().map(Hotel::getId).toList()).stream()
                .collect(Collectors.toMap(RoomType::getHotelId, Function.identity(), (a, b) -> a));
        Map<Long, Integer> available = hasDates
                ? availabilityService.availableRooms(roomByHotel.values(), checkIn, checkOut)
                : Map.of();

        return hotels.stream()
                .filter(h -> roomByHotel.containsKey(h.getId()))
                .map(h -> {
                    RoomType room = roomByHotel.get(h.getId());
                    return new HotelSummaryResponse(h.getId(), h.getName(), h.getCity(), h.getAddress(),
                            h.getDescription(), h.getStarRating(), h.getCoverImageUrl(),
                            List.copyOf(h.getAmenities()), room.getName(), room.getPricePerNight(),
                            currency, room.getTotalRooms(), hasDates ? available.get(room.getId()) : null);
                })
                .toList();
    }

    public HotelDetailResponse getDetail(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        boolean hasDates = validateOptionalDates(checkIn, checkOut);
        Hotel hotel = findHotel(hotelId);
        RoomType room = findRoom(hotelId);

        StayAvailabilityResponse stay = null;
        if (hasDates) {
            int nights = BookingRules.nightsBetween(checkIn, checkOut);
            int left = availabilityService.availableRooms(room, checkIn, checkOut);
            stay = new StayAvailabilityResponse(checkIn, checkOut, nights, left, left > 0,
                    BookingRules.totalFor(room.getPricePerNight(), nights));
        }
        return new HotelDetailResponse(hotel.getId(), hotel.getName(), hotel.getCity(), hotel.getAddress(),
                hotel.getDescription(), hotel.getStarRating(), hotel.getCoverImageUrl(),
                List.copyOf(hotel.getImages()), List.copyOf(hotel.getAmenities()), currency,
                toRoomResponse(room), stay);
    }

    public List<String> locations() {
        return hotelRepository.findDistinctCities();
    }

    /** Surprise feature: rooms left per night for the next {@code days} days. */
    public List<DayAvailabilityResponse> calendar(Long hotelId, LocalDate from, int days) {
        findHotel(hotelId);
        RoomType room = findRoom(hotelId);
        LocalDate today = LocalDate.now(clock);
        LocalDate start = (from == null || from.isBefore(today)) ? today : from;

        return availabilityService.availableByNight(room, start, start.plusDays(days)).entrySet().stream()
                .map(e -> new DayAvailabilityResponse(e.getKey(), e.getValue(), room.getTotalRooms(),
                        levelFor(e.getValue(), room.getTotalRooms()), room.getPricePerNight()))
                .toList();
    }

    /** Surprise feature: nearest same-length stays that still have a room. */
    public List<AlternativeStayResponse> alternatives(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        LocalDate today = LocalDate.now(clock);
        int nights = rules.validateStay(checkIn, checkOut, today);
        findHotel(hotelId);
        RoomType room = findRoom(hotelId);

        LocalDate windowStart = checkIn.minusDays(ALTERNATIVE_SEARCH_DAYS);
        if (windowStart.isBefore(today)) {
            windowStart = today;
        }
        LocalDate windowEnd = checkOut.plusDays(ALTERNATIVE_SEARCH_DAYS);
        Map<LocalDate, Integer> byNight = availabilityService.availableByNight(room, windowStart, windowEnd);

        return AlternativeDateFinder.find(byNight, checkIn, nights, today,
                        today.plusDays(rules.getMaxDaysInAdvance()), ALTERNATIVE_SEARCH_DAYS, ALTERNATIVE_LIMIT)
                .stream()
                .map(w -> new AlternativeStayResponse(w.checkIn(), w.checkOut(), nights, w.shiftDays(),
                        w.availableRooms(), BookingRules.totalFor(room.getPricePerNight(), nights)))
                .toList();
    }

    static DayAvailabilityResponse.Level levelFor(int available, int total) {
        if (available <= 0) {
            return DayAvailabilityResponse.Level.SOLD_OUT;
        }
        int limitedThreshold = Math.max(1, (int) Math.ceil(total * 0.3));
        return available <= limitedThreshold
                ? DayAvailabilityResponse.Level.LIMITED
                : DayAvailabilityResponse.Level.AVAILABLE;
    }

    /** Dates are optional on browse endpoints, but if one is given both must be valid. */
    private boolean validateOptionalDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null && checkOut == null) {
            return false;
        }
        if (checkIn == null || checkOut == null) {
            throw new BookingValidationException("DATES_INCOMPLETE",
                    "Please choose both check-in and check-out dates.");
        }
        rules.validateStay(checkIn, checkOut, LocalDate.now(clock));
        return true;
    }

    private Hotel findHotel(Long hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("HOTEL_NOT_FOUND", "Hotel not found."));
    }

    private RoomType findRoom(Long hotelId) {
        return roomTypeRepository.findFirstByHotel_IdOrderByIdAsc(hotelId)
                .orElseThrow(() -> new NotFoundException("ROOM_NOT_FOUND", "This hotel has no rooms to book."));
    }

    static RoomResponse toRoomResponse(RoomType r) {
        return new RoomResponse(r.getId(), r.getName(), r.getDescription(), r.getBedType(),
                r.getMaxGuests(), r.getPricePerNight(), r.getTotalRooms());
    }
}
