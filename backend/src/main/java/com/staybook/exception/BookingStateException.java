package com.staybook.exception;

import org.springframework.http.HttpStatus;

/** The booking exists but is in a state that does not allow the action. */
public class BookingStateException extends ApiException {
    public BookingStateException(String code, String message) {
        super(HttpStatus.CONFLICT, code, message);
    }
}
