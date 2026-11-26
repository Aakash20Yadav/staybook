package com.staybook.service;

import com.staybook.entity.RoomInventory;
import com.staybook.entity.RoomType;
import com.staybook.exception.RoomUnavailableException;
import com.staybook.repository.RoomInventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reads and changes per-night room inventory.
 *
 * Rooms available for a stay = total rooms - bookings on the busiest night of
 * that stay. A room is only bookable if it is free on every night.
 */
@Service
public class AvailabilityService {

    private final RoomInventoryRepository inventoryRepository;

    public AvailabilityService(RoomInventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public int availableRooms(RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
        return availableRooms(List.of(roomType), checkIn, checkOut).get(roomType.getId());
    }

    /** Rooms left for the stay, for several room types in one query (used by search). */
    @Transactional(readOnly = true)
    public Map<Long, Integer> availableRooms(Collection<RoomType> roomTypes, LocalDate checkIn, LocalDate checkOut) {
        if (roomTypes.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = roomTypes.stream().map(RoomType::getId).toList();
        Map<Long, Integer> peakBooked = new HashMap<>();
        for (Object[] row : inventoryRepository.findPeakBookedCounts(ids, checkIn, checkOut)) {
            peakBooked.put((Long) row[0], ((Number) row[1]).intValue());
        }
        Map<Long, Integer> result = new HashMap<>();
        for (RoomType rt : roomTypes) {
            result.put(rt.getId(), Math.max(0, rt.getTotalRooms() - peakBooked.getOrDefault(rt.getId(), 0)));
        }
        return result;
    }

    /** Rooms left for each night in [from, to), in date order. */
    @Transactional(readOnly = true)
    public Map<LocalDate, Integer> availableByNight(RoomType roomType, LocalDate from, LocalDate to) {
        Map<LocalDate, Integer> booked = inventoryRepository.findRange(roomType.getId(), from, to).stream()
                .collect(Collectors.toMap(RoomInventory::getStayDate, RoomInventory::getBookedCount));
        Map<LocalDate, Integer> result = new LinkedHashMap<>();
        for (LocalDate night = from; night.isBefore(to); night = night.plusDays(1)) {
            result.put(night, Math.max(0, roomType.getTotalRooms() - booked.getOrDefault(night, 0)));
        }
        return result;
    }

    /**
     * Takes one room on every night of the stay, or nothing at all.
     * The caller must already hold the room type's row lock and an open transaction.
     *
     * @throws RoomUnavailableException naming the first night that is full
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void reserve(RoomType lockedRoomType, LocalDate checkIn, LocalDate checkOut) {
        Map<LocalDate, RoomInventory> existing = inventoryRepository
                .findRangeForUpdate(lockedRoomType.getId(), checkIn, checkOut).stream()
                .collect(Collectors.toMap(RoomInventory::getStayDate, Function.identity()));

        List<RoomInventory> nights = new ArrayList<>();
        for (LocalDate night = checkIn; night.isBefore(checkOut); night = night.plusDays(1)) {
            RoomInventory inv = existing.get(night);
            if (inv == null) {
                inv = new RoomInventory(lockedRoomType.getId(), night);
            }
            if (inv.getBookedCount() >= lockedRoomType.getTotalRooms()) {
                throw new RoomUnavailableException(night);
            }
            nights.add(inv);
        }
        // Every night has space: only now change anything.
        nights.forEach(RoomInventory::reserveOne);
        inventoryRepository.saveAll(nights);
    }

    /** Gives back one room on every night of the stay. Caller holds the lock. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void release(RoomType lockedRoomType, LocalDate checkIn, LocalDate checkOut) {
        List<RoomInventory> nights = inventoryRepository.findRangeForUpdate(lockedRoomType.getId(), checkIn, checkOut);
        nights.forEach(RoomInventory::releaseOne);
        inventoryRepository.saveAll(nights);
    }
}
