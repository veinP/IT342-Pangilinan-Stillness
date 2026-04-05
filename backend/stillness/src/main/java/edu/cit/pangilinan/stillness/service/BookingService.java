package edu.cit.pangilinan.stillness.service;

import edu.cit.pangilinan.stillness.dto.request.CreateBookingRequest;
import edu.cit.pangilinan.stillness.dto.response.BookingDto;
import edu.cit.pangilinan.stillness.model.Booking;
import edu.cit.pangilinan.stillness.model.Session;
import edu.cit.pangilinan.stillness.model.User;
import edu.cit.pangilinan.stillness.repository.BookingRepository;
import edu.cit.pangilinan.stillness.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SessionRepository sessionRepository;

    public BookingDto createBooking(CreateBookingRequest request, User user) {
        Optional<Session> sessionOpt = sessionRepository.findById(request.getSessionId());
        if (sessionOpt.isEmpty()) {
            return null;
        }

        Session session = sessionOpt.get();

        // Generate unique booking number
        String bookingNumber = "STN-" + System.currentTimeMillis();

        Booking booking = Booking.builder()
                .bookingNumber(bookingNumber)
                .user(user)
                .session(session)
                .status("CONFIRMED")
                .bookedAt(LocalDateTime.now())
                .attendeeNotes(request.getAttendeeNotes())
                .build();

        Booking saved = bookingRepository.save(booking);
        return convertToDto(saved);
    }

    public BookingDto getBookingById(UUID id) {
        Optional<Booking> booking = bookingRepository.findById(id);
        return booking.map(this::convertToDto).orElse(null);
    }

    public List<BookingDto> getUserBookings(User user) {
        return bookingRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> getSessionBookings(Session session) {
        return bookingRepository.findBySession(session).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public BookingDto cancelBooking(UUID id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return null;
        }

        Booking booking = bookingOpt.get();
        booking.setStatus("CANCELLED");
        booking.setCancelledAt(LocalDateTime.now());

        Booking updated = bookingRepository.save(booking);
        return convertToDto(updated);
    }

    public long getSessionBookingCount(UUID sessionId) {
        Optional<Session> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return 0;
        }
        return bookingRepository.countBySessionAndStatus(sessionOpt.get(), "CONFIRMED");
    }

    private BookingDto convertToDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .bookingNumber(booking.getBookingNumber())
                .userId(booking.getUser() != null ? booking.getUser().getId() : null)
                .sessionId(booking.getSession() != null ? booking.getSession().getId() : null)
                .status(booking.getStatus())
                .bookedAt(booking.getBookedAt())
                .cancelledAt(booking.getCancelledAt())
                .cancellationReason(booking.getCancellationReason())
                .attendeeNotes(booking.getAttendeeNotes())
                .build();
    }
}
