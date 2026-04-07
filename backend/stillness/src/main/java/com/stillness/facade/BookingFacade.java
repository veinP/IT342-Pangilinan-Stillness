package com.stillness.facade;

import com.stillness.payment.PaymentContext;
import com.stillness.payment.PaymentResult;
import edu.cit.pangilinan.stillness.dto.request.CreateBookingRequest;
import edu.cit.pangilinan.stillness.dto.response.BookingDto;
import edu.cit.pangilinan.stillness.dto.response.SessionDetailDto;
import edu.cit.pangilinan.stillness.model.User;
import edu.cit.pangilinan.stillness.service.BookingService;
import edu.cit.pangilinan.stillness.service.SessionService;
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
        String paymentMethodId = amount.compareTo(BigDecimal.ZERO) == 0 ? "FREE" : "SANDBOX_CARD";
        PaymentResult paymentResult = paymentContext.executePayment(currentUser.getId(), amount, paymentMethodId);
        if (!paymentResult.isSuccessful()) {
            throw new IllegalStateException("Payment could not be completed");
        }

        BookingDto booking = bookingService.createBooking(request, currentUser);
        if (booking == null || booking.getId() == null) {
            throw new IllegalStateException("Booking could not be created");
        }

        return booking;
    }
}
