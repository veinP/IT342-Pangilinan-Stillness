package edu.cit.pangilinan.stillness.shared.repository;

import edu.cit.pangilinan.stillness.shared.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, UUID> {
    Optional<Instructor> findByUserId(UUID userId);
}
