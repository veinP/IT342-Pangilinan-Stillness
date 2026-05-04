package edu.cit.pangilinan.stillness.booking;

import edu.cit.pangilinan.stillness.payment.strategy.PaymentContext;
import edu.cit.pangilinan.stillness.payment.strategy.PaymentResult;
import edu.cit.pangilinan.stillness.booking.dto.CreateBookingRequest;
import edu.cit.pangilinan.stillness.booking.dto.BookingDto;
import edu.cit.pangilinan.stillness.session.dto.SessionDetailDto;
import edu.cit.pangilinan.stillness.shared.model.User;
import edu.cit.pangilinan.stillness.booking.BookingService;
import edu.cit.pangilinan.stillness.session.SessionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BookingFacade {

    private final SessionService sessionService;
    private final PaymentContext paymentContext;
    private final BookingService bookingService;

    public BookingFacade(
            SessionService sessionService,
            PaymentContext paymentContext,
            BookingService bookingService
    ) {
        this.sessionService = sessionService;
        this.paymentContext = paymentContext;
        this.bookingService = bookingService;
    }

    public BookingDto completeBooking(User currentUser, CreateBookingRequest request) {
        if (currentUser == null) {
            throw new IllegalStateException("Authenticated user is required to create a booking");
        }

        sessionService.validateCapacity(request.getSessionId());
        SessionDetailDto session = sessionService.getSessionByIdDetail(request.getSessionId());
        if (session == null) {
            throw new IllegalStateException("Session not found");
        }

        BigDecimal amount = session.getPrice() != null ? session.getPrice() : BigDecimal.ZERO;
        
        // For non-free sessions, skip Stripe payment for now (test mode)
        // In production, implement proper Stripe client secret flow
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            // For sandbox/test: just log the payment intent and continue
            System.out.println("TEST MODE: Processing payment of " + amount + " PHP for session " + request.getSessionId());
        }
        
        BookingDto booking = bookingService.createBooking(request, currentUser);
        if (booking == null || booking.getId() == null) {
            throw new IllegalStateException("Booking could not be created");
        }

        return booking;
    }
}
