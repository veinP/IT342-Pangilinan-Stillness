package edu.cit.pangilinan.stillness.shared.repository;

import edu.cit.pangilinan.stillness.shared.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
}
