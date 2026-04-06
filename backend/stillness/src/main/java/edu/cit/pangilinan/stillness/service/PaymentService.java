package edu.cit.pangilinan.stillness.service;

import edu.cit.pangilinan.stillness.dto.response.PaymentDto;
import edu.cit.pangilinan.stillness.model.Booking;
import edu.cit.pangilinan.stillness.model.Payment;
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
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

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
        return paymentRepository.findByUser(user).stream()
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
}
