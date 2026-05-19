package edu.cit.pangilinan.stillness.shared.repository;

import edu.cit.pangilinan.stillness.shared.model.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    @Modifying
    @Query("UPDATE Session s SET s.status = 'ARCHIVED' WHERE s.status != 'ARCHIVED' AND (s.endTime < :now OR (s.endTime IS NULL AND s.startTime < :now))")
    int archivePastSessions(@Param("now") LocalDateTime now);

    Page<Session> findByStatusNot(String status, Pageable pageable);
}
