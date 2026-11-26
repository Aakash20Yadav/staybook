package com.staybook.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Surprise feature, pure logic: given rooms left per night, find the nearest
 * stays of the same length that still have a room on every night.
 *
 * Candidates are tried closest-first, alternating later and earlier
 * (+1, -1, +2, -2, ...), so the guest's plans move as little as possible.
 */
public final class AlternativeDateFinder {

    public record Window(LocalDate checkIn, LocalDate checkOut, int shiftDays, int availableRooms) { }

    private AlternativeDateFinder() { }

    /**
     * @param availableByNight rooms left keyed by night; a missing night counts as unknown (skipped)
     * @param checkIn          the guest's requested check-in
     * @param nights           requested length of stay
     * @param earliest         earliest allowed check-in (usually today)
     * @param latest           latest allowed check-in (booking window)
     * @param maxShift         furthest shift in days to try, in each direction
     * @param limit            how many suggestions to return
     */
    public static List<Window> find(Map<LocalDate, Integer> availableByNight, LocalDate checkIn,
                                    int nights, LocalDate earliest, LocalDate latest,
                                    int maxShift, int limit) {
        List<Window> result = new ArrayList<>();
        for (int distance = 1; distance <= maxShift && result.size() < limit; distance++) {
            for (int shift : new int[]{distance, -distance}) {
                if (result.size() >= limit) {
                    break;
                }
                LocalDate start = checkIn.plusDays(shift);
                if (start.isBefore(earliest) || start.isAfter(latest)) {
                    continue;
                }
                int minLeft = minAvailable(availableByNight, start, nights);
                if (minLeft > 0) {
                    result.add(new Window(start, start.plusDays(nights), shift, minLeft));
                }
            }
        }
        return result;
    }

    private static int minAvailable(Map<LocalDate, Integer> availableByNight, LocalDate start, int nights) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < nights; i++) {
            Integer left = availableByNight.get(start.plusDays(i));
            if (left == null) {
                return 0;
            }
            min = Math.min(min, left);
        }
        return min;
    }
}
