package com.staybook.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AlternativeDateFinderTest {

    private static final LocalDate TODAY = LocalDate.of(2030, 1, 1);

    /** 30 nights from TODAY with 2 rooms each, except the given sold-out nights. */
    private Map<LocalDate, Integer> calendar(int... soldOutOffsets) {
        Map<LocalDate, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < 30; i++) {
            map.put(TODAY.plusDays(i), 2);
        }
        for (int offset : soldOutOffsets) {
            map.put(TODAY.plusDays(offset), 0);
        }
        return map;
    }

    @Test
    void suggestsClosestWindowsFirstPreferringLater() {
        // Requested nights 10,11 ; night 10 is sold out.
        List<AlternativeDateFinder.Window> result = AlternativeDateFinder.find(
                calendar(10), TODAY.plusDays(10), 2, TODAY, TODAY.plusDays(365), 14, 3);

        assertThat(result).extracting(AlternativeDateFinder.Window::shiftDays)
                .containsExactly(1, 2, -2);   // -1 would include night 10, so it is skipped
        assertThat(result.get(0).checkIn()).isEqualTo(TODAY.plusDays(11));
        assertThat(result.get(0).checkOut()).isEqualTo(TODAY.plusDays(13));
    }

    @Test
    void neverSuggestsCheckInBeforeEarliestDate() {
        List<AlternativeDateFinder.Window> result = AlternativeDateFinder.find(
                calendar(1), TODAY.plusDays(1), 1, TODAY, TODAY.plusDays(365), 3, 10);

        assertThat(result).allSatisfy(w -> assertThat(w.checkIn()).isAfterOrEqualTo(TODAY));
        assertThat(result).extracting(AlternativeDateFinder.Window::shiftDays).containsExactly(1, -1, 2, 3);
    }

    @Test
    void reportsFewestRoomsLeftAcrossTheWindow() {
        Map<LocalDate, Integer> cal = calendar(5);
        cal.put(TODAY.plusDays(7), 1);
        List<AlternativeDateFinder.Window> result = AlternativeDateFinder.find(
                cal, TODAY.plusDays(5), 2, TODAY, TODAY.plusDays(365), 2, 1);

        assertThat(result).singleElement().satisfies(w -> {
            assertThat(w.shiftDays()).isEqualTo(1);       // nights 6,7
            assertThat(w.availableRooms()).isEqualTo(1);  // night 7 has 1 left
        });
    }

    @Test
    void returnsNothingWhenEverythingIsFull() {
        int[] all = new int[30];
        for (int i = 0; i < 30; i++) {
            all[i] = i;
        }
        assertThat(AlternativeDateFinder.find(calendar(all), TODAY.plusDays(10), 2,
                TODAY, TODAY.plusDays(365), 14, 4)).isEmpty();
    }
}
