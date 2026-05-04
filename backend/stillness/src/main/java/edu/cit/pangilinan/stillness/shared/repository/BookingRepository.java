package edu.cit.pangilinan.stillness.shared.repository;

import edu.cit.pangilinan.stillness.shared.model.Booking;
import edu.cit.pangilinan.stillness.shared.model.Session;
import edu.cit.pangilinan.stillness.shared.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByUser(User user);
    List<Booking> findBySession(Session session);
    long countBySessionAndStatus(Session session, String status);
}
