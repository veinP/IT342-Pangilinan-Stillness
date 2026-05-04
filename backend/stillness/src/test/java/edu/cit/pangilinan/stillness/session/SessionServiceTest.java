package edu.cit.pangilinan.stillness.session;

import edu.cit.pangilinan.stillness.shared.model.Instructor;
import edu.cit.pangilinan.stillness.shared.model.Session;
import edu.cit.pangilinan.stillness.shared.model.User;
import edu.cit.pangilinan.stillness.shared.repository.BookingRepository;
import edu.cit.pangilinan.stillness.shared.repository.InstructorRepository;
import edu.cit.pangilinan.stillness.shared.repository.SessionRepository;
import edu.cit.pangilinan.stillness.session.dto.CreateSessionRequest;
import edu.cit.pangilinan.stillness.session.dto.SessionDetailDto;
import edu.cit.pangilinan.stillness.session.dto.SessionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SessionService — Vertical Slice: session
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SessionService Unit Tests")
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private SessionService sessionService;

    private User instructorUser;
    private Instructor instructor;
    private Session session;

    @BeforeEach
    void setUp() {
        instructorUser = User.builder()
                .id(UUID.randomUUID())
                .email("instructor@stillness.com")
                .fullName("Test Instructor")
                .role("ROLE_INSTRUCTOR")
                .emailVerified(true)
                .build();

        instructor = Instructor.builder()
                .id(UUID.randomUUID())
                .user(instructorUser)
                .bio("Test bio")
                .build();

        session = Session.builder()
                .id(UUID.randomUUID())
                .title("Morning Meditation")
                .description("A calming morning session")
                .instructor(instructor)
                .sessionType("MEDITATION")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .capacity(20)
                .price(BigDecimal.valueOf(500))
                .location("Main Hall")
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("getAllSessions should return paginated session DTOs")
    void getAllSessions_shouldReturnPaginatedSessions() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Session> sessionPage = new PageImpl<>(List.of(session));
        when(sessionRepository.findAll(pageable)).thenReturn(sessionPage);
        when(bookingRepository.countBySessionAndStatus(session, "CONFIRMED")).thenReturn(5L);

        Page<SessionDto> result = sessionService.getAllSessions(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Morning Meditation");
        assertThat(result.getContent().get(0).getBookedCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("getSessionByIdDetail should return session detail DTO when found")
    void getSessionByIdDetail_shouldReturnDetailDtoWhenFound() {
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(bookingRepository.countBySessionAndStatus(session, "CONFIRMED")).thenReturn(0L);

        SessionDetailDto result = sessionService.getSessionByIdDetail(session.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(session.getId());
        assertThat(result.getTitle()).isEqualTo("Morning Meditation");
        assertThat(result.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("getSessionByIdDetail should return null when session not found")
    void getSessionByIdDetail_shouldReturnNullWhenNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(sessionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        SessionDetailDto result = sessionService.getSessionByIdDetail(nonExistentId);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("createSession should throw exception for non-instructor users")
    void createSession_shouldThrowForNonInstructor() {
        User regularUser = User.builder()
                .id(UUID.randomUUID())
                .email("user@stillness.com")
                .role("ROLE_USER")
                .build();

        CreateSessionRequest request = new CreateSessionRequest();
        request.setTitle("Test Session");

        assertThatThrownBy(() -> sessionService.createSession(request, regularUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only instructors");
    }

    @Test
    @DisplayName("createSession should persist and return new session")
    void createSession_shouldPersistNewSession() {
        when(instructorRepository.findByUserId(instructorUser.getId())).thenReturn(Optional.of(instructor));
        when(sessionRepository.save(any(Session.class))).thenReturn(session);
        when(bookingRepository.countBySessionAndStatus(any(), eq("CONFIRMED"))).thenReturn(0L);

        CreateSessionRequest request = new CreateSessionRequest();
        request.setTitle("Morning Meditation");
        request.setDescription("A calming morning session");
        request.setSessionType("MEDITATION");
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        request.setCapacity(20);
        request.setPrice(BigDecimal.valueOf(500));
        request.setLocation("Main Hall");

        SessionDetailDto result = sessionService.createSession(request, instructorUser);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Morning Meditation");
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    @DisplayName("deleteSession should return true when session exists")
    void deleteSession_shouldReturnTrueWhenExists() {
        when(sessionRepository.existsById(session.getId())).thenReturn(true);
        doNothing().when(sessionRepository).deleteById(session.getId());

        boolean deleted = sessionService.deleteSession(session.getId());

        assertThat(deleted).isTrue();
        verify(sessionRepository).deleteById(session.getId());
    }

    @Test
    @DisplayName("deleteSession should return false when session does not exist")
    void deleteSession_shouldReturnFalseWhenNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(sessionRepository.existsById(nonExistentId)).thenReturn(false);

        boolean deleted = sessionService.deleteSession(nonExistentId);

        assertThat(deleted).isFalse();
        verify(sessionRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("validateCapacity should throw exception when session is full")
    void validateCapacity_shouldThrowWhenFull() {
        session.setCapacity(10);
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(bookingRepository.countBySessionAndStatus(session, "CONFIRMED")).thenReturn(10L);

        assertThatThrownBy(() -> sessionService.validateCapacity(session.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fully booked");
    }
}
