package com.staybook.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDate;

/** No room left for at least one night of the requested stay. */
public class RoomUnavailableException extends ApiException {

    private final LocalDate soldOutNight;

    public RoomUnavailableException(LocalDate soldOutNight) {
        super(HttpStatus.CONFLICT, "ROOM_UNAVAILABLE",
                "Sorry, no rooms are left for the night of " + soldOutNight
                        + ". Please try different dates.");
        this.soldOutNight = soldOutNight;
    }

    public LocalDate getSoldOutNight() { return soldOutNight; }
}
