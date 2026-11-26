package com.staybook.dto;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

/**
 * The single error format returned by every endpoint.
 *
 * <pre>
 * { "timestamp": "...", "status": 409, "error": "Conflict",
 *   "code": "ROOM_UNAVAILABLE", "message": "...", "path": "/api/bookings",
 *   "fieldErrors": [ { "field": "guestEmail", "message": "..." } ] }
 * </pre>
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) { }

    public static ApiError of(HttpStatus status, String code, String message,
                              String path, List<FieldError> fieldErrors) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(),
                code, message, path, fieldErrors);
    }
}
