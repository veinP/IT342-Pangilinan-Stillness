package edu.cit.pangilinan.stillness.booking;

import edu.cit.pangilinan.stillness.payment.strategy.PaymentContext;
import edu.cit.pangilinan.stillness.payment.strategy.PaymentResult;
import edu.cit.pangilinan.stillness.booking.dto.CreateBookingRequest;
import edu.cit.pangilinan.stillness.booking.dto.BookingDto;
import edu.cit.pangilinan.stillness.session.dto.SessionDetailDto;
import edu.cit.pangilinan.stillness.shared.model.User;
import edu.cit.pangilinan.stillness.booking.BookingService;
import edu.cit.pangilinan.stillness.session.SessionService;
import edu.cit.pangilinan.stillness.payment.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BookingFacade {

    private final SessionService sessionService;
    private final PaymentContext paymentContext;
    private final BookingService bookingService;
    private final PaymentService paymentService;

    public BookingFacade(
            SessionService sessionService,
            PaymentContext paymentContext,
            BookingService bookingService,
            PaymentService paymentService
    ) {
        this.sessionService = sessionService;
        this.paymentContext = paymentContext;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
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

        // Process payment via Stripe for paid sessions
        String paymentIntentId = null;
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            PaymentResult result = paymentContext.executePayment(
                    currentUser.getId(),
                    amount,
                    "pm_card_visa" // Stripe test payment method
            );
            paymentIntentId = result.getPaymentReference();
        }
        
        BookingDto booking = bookingService.createBooking(request, currentUser);
        if (booking == null || booking.getId() == null) {
            throw new IllegalStateException("Booking could not be created");
        }

        // If payment was processed, confirm it and link to booking
        if (paymentIntentId != null) {
            try {
                paymentService.confirmPayment(paymentIntentId, "txn_" + System.currentTimeMillis());
            } catch (Exception e) {
                // Payment confirmation is best-effort; booking is already created
                System.err.println("Payment confirmation warning: " + e.getMessage());
            }
        }

        return booking;
    }
}
