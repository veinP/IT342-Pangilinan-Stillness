package edu.cit.pangilinan.stillness.service;

import edu.cit.pangilinan.stillness.dto.request.CreateSessionRequest;
import edu.cit.pangilinan.stillness.dto.response.SessionDetailDto;
import edu.cit.pangilinan.stillness.dto.response.SessionDto;
import edu.cit.pangilinan.stillness.model.Instructor;
import edu.cit.pangilinan.stillness.model.Session;
import edu.cit.pangilinan.stillness.model.User;
import edu.cit.pangilinan.stillness.repository.BookingRepository;
import edu.cit.pangilinan.stillness.repository.InstructorRepository;
import edu.cit.pangilinan.stillness.repository.SessionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@SuppressWarnings("unchecked")
public class SessionService {

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private InstructorRepository instructorRepository;

	@Autowired
	private BookingRepository bookingRepository;

	@Transactional
	public Page<SessionDto> getAllSessions(Pageable pageable) {
		return sessionRepository.findAll(pageable).map(this::convertToDto);
	}

	@Transactional
	public SessionDetailDto getSessionByIdDetail(UUID id) {
		Optional<Session> session = sessionRepository.findById(id);
		return session.map(this::convertToDetailDto).orElse(null);
	}

	@Transactional
	public SessionDetailDto createSession(CreateSessionRequest request, User createdBy) {
		if (createdBy == null) {
			throw new IllegalStateException("Authenticated user is required");
		}
		if (!"ROLE_INSTRUCTOR".equals(createdBy.getRole())) {
			throw new IllegalStateException("Only instructors can create sessions");
		}

		Instructor instructor = resolveInstructor(createdBy, request.getInstructorId());

		Session session = Session.builder()
				.title(request.getTitle())
				.description(request.getDescription())
				.instructor(instructor)
				.sessionType(request.getSessionType())
				.startTime(request.getStartTime())
				.endTime(request.getEndTime())
				.capacity(request.getCapacity())
				.price(request.getPrice())
				.location(request.getLocation())
				.status("ACTIVE")
				.createdBy(createdBy)
				.build();

		Session saved = sessionRepository.save(session);
		return convertToDetailDto(saved);
	}

	@Transactional
	public SessionDetailDto updateSession(UUID id, CreateSessionRequest request, User currentUser) {
		Optional<Session> optionalSession = sessionRepository.findById(id);
		if (optionalSession.isEmpty()) {
			return null;
		}

		Session session = optionalSession.get();
		Instructor instructor = currentUser != null ? resolveInstructor(currentUser, request.getInstructorId()) : null;

		session.setTitle(request.getTitle());
		session.setDescription(request.getDescription());
		session.setInstructor(instructor);
		session.setSessionType(request.getSessionType());
		session.setStartTime(request.getStartTime());
		session.setEndTime(request.getEndTime());
		session.setCapacity(request.getCapacity());
		session.setPrice(request.getPrice());
		session.setLocation(request.getLocation());

		Session saved = sessionRepository.save(session);
		return convertToDetailDto(saved);
	}

	@Transactional
	public boolean deleteSession(UUID id) {
		if (!sessionRepository.existsById(id)) {
			return false;
		}

		sessionRepository.deleteById(id);
		return true;
	}

	@Transactional
	public void validateCapacity(UUID sessionId) {
		Optional<Session> sessionOpt = sessionRepository.findById(sessionId);
		if (sessionOpt.isEmpty()) {
			throw new IllegalStateException("Session not found");
		}

		Session session = sessionOpt.get();
		int capacity = session.getCapacity() != null ? session.getCapacity() : 0;
		long bookedCount = bookingRepository.countBySessionAndStatus(session, "CONFIRMED");
		if (bookedCount >= capacity) {
			throw new IllegalStateException("Session is fully booked");
		}
	}

	private SessionDto convertToDto(Session session) {
		SessionDto.InstructorDto instructorDto = null;
		if (session.getInstructor() != null && session.getInstructor().getUser() != null) {
			User instructorUser = session.getInstructor().getUser();
			instructorDto = SessionDto.InstructorDto.builder()
					.id(session.getInstructor().getId())
					.fullName(instructorUser.getFullName())
					.profileImageUrl(session.getInstructor().getProfileImageUrl())
					.build();
		}

		long bookedCount = bookingRepository.countBySessionAndStatus(session, "CONFIRMED");
		int limitedBookedCount = (int) Math.min(bookedCount, session.getCapacity());

		return SessionDto.builder()
				.id(session.getId())
				.title(session.getTitle())
				.description(session.getDescription())
				.instructor(instructorDto)
				.type(session.getSessionType())
				.startTime(session.getStartTime())
				.endTime(session.getEndTime())
				.capacity(session.getCapacity())
				.bookedCount(limitedBookedCount)
				.price(session.getPrice())
				.thumbnailUrl(session.getThumbnailUrl())
				.location(session.getLocation())
				.status(session.getStatus())
				.createdAt(session.getCreatedAt())
				.build();
	}

	private Instructor resolveInstructor(User user, UUID requestedInstructorId) {
		if (requestedInstructorId != null) {
			Instructor requested = instructorRepository.findById(requestedInstructorId).orElse(null);
			if (requested != null) {
				return requested;
			}
		}

		Instructor instructor = instructorRepository.findByUserId(user.getId()).orElse(null);
		if (instructor != null) {
			return instructor;
		}

		Instructor createdInstructor = Instructor.builder()
				.user(user)
				.bio(user.getFullName() != null ? user.getFullName() + " instructor profile" : "Instructor profile")
				.profileImageUrl(user.getProfileImageUrl())
				.build();
		return instructorRepository.save(createdInstructor);
	}

	private SessionDetailDto convertToDetailDto(Session session) {
		SessionDetailDto.InstructorDto instructorDto = null;
		if (session.getInstructor() != null && session.getInstructor().getUser() != null) {
			User instructorUser = session.getInstructor().getUser();
			instructorDto = SessionDetailDto.InstructorDto.builder()
					.id(session.getInstructor().getId())
					.fullName(instructorUser.getFullName())
					.profileImageUrl(session.getInstructor().getProfileImageUrl())
					.build();
		}

		int capacity = session.getCapacity() != null ? session.getCapacity() : 0;
		long bookedCount = bookingRepository.countBySessionAndStatus(session, "CONFIRMED");
		int limitedBookedCount = (int) Math.min(bookedCount, capacity);

		return SessionDetailDto.builder()
				.id(session.getId())
				.title(session.getTitle())
				.description(session.getDescription())
				.instructor(instructorDto)
				.type(session.getSessionType())
				.startTime(session.getStartTime())
				.endTime(session.getEndTime())
				.capacity(capacity)
				.bookedCount(limitedBookedCount)
				.price(session.getPrice())
				.thumbnailUrl(session.getThumbnailUrl())
				.location(session.getLocation())
				.status(session.getStatus())
				.createdAt(session.getCreatedAt())
				.available("ACTIVE".equals(session.getStatus()) && capacity > limitedBookedCount)
				.build();
	}
}
