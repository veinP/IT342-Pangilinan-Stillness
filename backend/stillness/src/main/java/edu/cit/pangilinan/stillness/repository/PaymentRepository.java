package edu.cit.pangilinan.stillness.repository;

import edu.cit.pangilinan.stillness.model.Payment;
import edu.cit.pangilinan.stillness.model.Booking;
import edu.cit.pangilinan.stillness.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByPaymentIntentId(String paymentIntentId);
    Optional<Payment> findByBooking(Booking booking);
    List<Payment> findByUser(User user);
}
