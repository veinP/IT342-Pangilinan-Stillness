package edu.cit.pangilinan.stillness.booking;

import edu.cit.pangilinan.stillness.shared.model.Booking;
import edu.cit.pangilinan.stillness.shared.model.Instructor;
import edu.cit.pangilinan.stillness.shared.model.Payment;
import edu.cit.pangilinan.stillness.shared.model.Session;
import edu.cit.pangilinan.stillness.shared.model.User;
import edu.cit.pangilinan.stillness.shared.repository.BookingRepository;
import edu.cit.pangilinan.stillness.shared.repository.PaymentRepository;
import edu.cit.pangilinan.stillness.shared.repository.SessionRepository;
import edu.cit.pangilinan.stillness.shared.repository.UserRepository;
import edu.cit.pangilinan.stillness.auth.EmailService;
import edu.cit.pangilinan.stillness.booking.dto.BookingDto;
import edu.cit.pangilinan.stillness.booking.dto.CreateBookingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookingService — Vertical Slice: booking
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Tests")
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private UserRepository userRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private EmailService emailService;

    @InjectMocks
    private BookingService bookingService;

    private User user;
    private Session session;
    private Booking booking;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("user@stillness.com")
                .fullName("Test User")
                .role("ROLE_USER")
                .build();

        Instructor instructor = Instructor.builder()
                .id(UUID.randomUUID())
                .user(user)
                .bio("Test instructor")
                .build();

        session = Session.builder()
                .id(UUID.randomUUID())
                .title("Evening Yoga")
                .price(BigDecimal.valueOf(300))
                .capacity(15)
                .instructor(instructor)
                .location("Studio A")
                .startTime(LocalDateTime.now().plusDays(1))
                .status("ACTIVE")
                .build();

        booking = Booking.builder()
                .id(UUID.randomUUID())
                .bookingNumber("STN-001")
                .user(user)
                .session(session)
                .status("CONFIRMED")
                .bookedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("createBooking should create booking for a valid session and user")
    void createBooking_shouldCreateSuccessfully() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setSessionId(session.getId());

        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.empty());
        doNothing().when(eventPublisher).publishEvent(any());
        doNothing().when(emailService).sendBookingConfirmation(any(), any(), any(), any(), any());

        BookingDto result = bookingService.createBooking(request, user);

        assertThat(result).isNotNull();
        assertThat(result.getBookingNumber()).isEqualTo("STN-001");
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("createBooking should return null for non-existent session")
    void createBooking_shouldReturnNullForMissingSession() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setSessionId(UUID.randomUUID());

        when(sessionRepository.findById(any())).thenReturn(Optional.empty());

        BookingDto result = bookingService.createBooking(request, user);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getUserBookings should return list of bookings for user")
    void getUserBookings_shouldReturnUserBookings() {
        when(bookingRepository.findByUser(user)).thenReturn(List.of(booking));
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.empty());

        List<BookingDto> result = bookingService.getUserBookings(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBookingNumber()).isEqualTo("STN-001");
    }

    @Test
    @DisplayName("cancelBooking should update status to CANCELLED")
    void cancelBooking_shouldCancelSuccessfully() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.empty());
        doNothing().when(emailService).sendCancellationConfirmation(any(), any(), any());

        BookingDto result = bookingService.cancelBooking(booking.getId());

        assertThat(result).isNotNull();
        verify(bookingRepository).save(argThat(b -> "CANCELLED".equals(b.getStatus())));
    }

    @Test
    @DisplayName("cancelBooking should return null when booking not found")
    void cancelBooking_shouldReturnNullWhenNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(bookingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        BookingDto result = bookingService.cancelBooking(nonExistentId);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("createBooking should throw exception when user is null")
    void createBooking_shouldThrowWhenUserIsNull() {
        // Set up security context with anonymous user
        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("anonymousUser");
        SecurityContextHolder.setContext(ctx);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setSessionId(session.getId());

        assertThatThrownBy(() -> bookingService.createBooking(request, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Authenticated user is required");

        SecurityContextHolder.clearContext();
    }
}
