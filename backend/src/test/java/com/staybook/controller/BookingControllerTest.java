package com.staybook.controller;

import com.staybook.dto.CreateBookingRequest;
import com.staybook.exception.RoomUnavailableException;
import com.staybook.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Checks that errors come back in the one consistent JSON shape. */
@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean BookingService bookingService;

    @Test
    void invalidFieldsReturn400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content("""
                        {"hotelId": 1, "checkIn": "2030-01-12", "checkOut": "2030-01-14",
                         "guestName": "", "guestEmail": "not-an-email"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/bookings"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'guestEmail')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'guestName')]").exists());
    }

    @Test
    void malformedDateReturns400() throws Exception {
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content("""
                        {"hotelId": 1, "checkIn": "12/01/2030", "checkOut": "2030-01-14",
                         "guestName": "Asha", "guestEmail": "asha@example.com"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void soldOutReturns409() throws Exception {
        when(bookingService.create(any(CreateBookingRequest.class)))
                .thenThrow(new RoomUnavailableException(LocalDate.of(2030, 1, 12)));

        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content("""
                        {"hotelId": 1, "checkIn": "2030-01-12", "checkOut": "2030-01-14",
                         "guestName": "Asha Kumar", "guestEmail": "asha@example.com"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ROOM_UNAVAILABLE"));
    }
}
