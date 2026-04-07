package edu.cit.pangilinan.stillness.service;

import edu.cit.pangilinan.stillness.dto.response.PaymentDto;
import edu.cit.pangilinan.stillness.dto.response.PaymentRecordDto;
import edu.cit.pangilinan.stillness.model.Booking;
import edu.cit.pangilinan.stillness.model.Payment;
import edu.cit.pangilinan.stillness.model.Session;
import edu.cit.pangilinan.stillness.model.User;
import edu.cit.pangilinan.stillness.repository.BookingRepository;
import edu.cit.pangilinan.stillness.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("unchecked")
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User resolveCurrentUser(User user) {
        if (user != null && user.getId() != null) {
            return user;
        }

        return null;
    }

    public PaymentDto createPayment(UUID bookingId, User user, BigDecimal amount, String paymentIntentId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return null;
        }

        Booking booking = bookingOpt.get();

        Payment payment = Payment.builder()
                .booking(booking)
                .user(user)
                .amount(amount)
                .currency("PHP")
                .paymentIntentId(paymentIntentId)
                .status("PENDING")
                .paymentDate(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        return convertToDto(saved);
    }

    public PaymentDto confirmPayment(String paymentIntentId, String transactionId) {
        Optional<Payment> paymentOpt = paymentRepository.findByPaymentIntentId(paymentIntentId);
        if (paymentOpt.isEmpty()) {
            return null;
        }

        Payment payment = paymentOpt.get();
        payment.setStatus("PAID");
        payment.setTransactionId(transactionId);
        payment.setPaymentDate(LocalDateTime.now());

        // Update booking payment status
        Booking booking = payment.getBooking();
        if (booking != null) {
            booking.setStatus("CONFIRMED");
            bookingRepository.save(booking);
        }

        Payment updated = paymentRepository.save(payment);
        return convertToDto(updated);
    }

    public PaymentDto getPaymentById(UUID id) {
        Optional<Payment> payment = paymentRepository.findById(id);
        return payment.map(this::convertToDto).orElse(null);
    }

    public List<PaymentDto> getUserPayments(User user) {
        User resolvedUser = resolveCurrentUser(user);
        if (resolvedUser == null) {
            return List.of();
        }

        return paymentRepository.findByUser(resolvedUser).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PaymentDto> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public PaymentDto refundPayment(UUID paymentId, String refundReason) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            return null;
        }

        Payment payment = paymentOpt.get();
        payment.setStatus("REFUNDED");
        payment.setRefundDate(LocalDateTime.now());
        payment.setRefundReason(refundReason);

        Payment updated = paymentRepository.save(payment);
        return convertToDto(updated);
    }

    private PaymentDto convertToDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking() != null ? payment.getBooking().getId() : null)
                .userId(payment.getUser() != null ? payment.getUser().getId() : null)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .paymentIntentId(payment.getPaymentIntentId())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .refundDate(payment.getRefundDate())
                .refundReason(payment.getRefundReason())
                .build();
    }

    public List<PaymentRecordDto> getAllPaymentsAsRecords() {
        return paymentRepository.findAllWithDetails().stream()
                .map(this::convertToRecordDto)
                .collect(Collectors.toList());
    }

    private PaymentRecordDto convertToRecordDto(Payment payment) {
        Booking booking = payment.getBooking();
        User user = payment.getUser();
        Session session = booking != null ? booking.getSession() : null;

        String bookingNumber = booking != null ? "BK-" + booking.getId().toString().substring(0, 8).toUpperCase() : "N/A";
        String userName = user != null ? user.getFullName() : "Unknown User";
        String sessionTitle = session != null ? session.getTitle() : "Unknown Session";
        String cardMasked = payment.getPaymentMethod() != null ? maskCardNumber(payment.getPaymentMethod()) : "N/A";

        return PaymentRecordDto.builder()
                .id(payment.getId())
                .bookingNumber(bookingNumber)
                .userName(userName)
                .sessionTitle(sessionTitle)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .cardMasked(cardMasked)
                .transactionId(payment.getTransactionId() != null ? payment.getTransactionId() : "PENDING")
                .status(payment.getStatus())
                .date(payment.getPaymentDate())
                .refundDate(payment.getRefundDate())
                .refundReason(payment.getRefundReason())
                .build();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }
}
