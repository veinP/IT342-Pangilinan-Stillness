package edu.cit.pangilinan.stillness.controller;

import edu.cit.pangilinan.stillness.dto.request.CreateBookingRequest;
import edu.cit.pangilinan.stillness.dto.response.ApiResponse;
import edu.cit.pangilinan.stillness.dto.response.BookingDto;
import edu.cit.pangilinan.stillness.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        try {
            // TODO: Get current user from Security Context
            BookingDto booking = bookingService.createBooking(request, null);
            if (booking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("SESSION_NOT_FOUND", "Session not found"));
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.builder()
                            .success(true)
                            .data(booking)
                            .timestamp(LocalDateTime.now().toString())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("BOOKING_CREATION_FAILED", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable UUID id) {
        try {
            BookingDto booking = bookingService.getBookingById(id);
            if (booking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("BOOKING_NOT_FOUND", "Booking not found"));
            }
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .data(booking)
                    .timestamp(LocalDateTime.now().toString())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("BOOKING_FETCH_FAILED", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyBookings() {
        try {
            // TODO: Get current user from Security Context
            List<BookingDto> bookings = bookingService.getUserBookings(null);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .data(bookings)
                    .timestamp(LocalDateTime.now().toString())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("BOOKINGS_FETCH_FAILED", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable UUID id) {
        try {
            BookingDto booking = bookingService.cancelBooking(id);
            if (booking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("BOOKING_NOT_FOUND", "Booking not found"));
            }
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .data(booking)
                    .timestamp(LocalDateTime.now().toString())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("BOOKING_CANCELLATION_FAILED", e.getMessage()));
        }
    }
}
