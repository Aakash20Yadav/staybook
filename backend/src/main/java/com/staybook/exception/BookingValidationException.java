package com.staybook.exception;

import org.springframework.http.HttpStatus;

/** A request that is well-formed but breaks a booking rule (e.g. check-in in the past). */
public class BookingValidationException extends ApiException {
    public BookingValidationException(String code, String message) {
        super(HttpStatus.BAD_REQUEST, code, message);
    }
}
