package edu.cit.pangilinan.stillness.booking;

import edu.cit.pangilinan.stillness.shared.model.Instructor;
import edu.cit.pangilinan.stillness.shared.model.Session;
import edu.cit.pangilinan.stillness.shared.model.User;
import edu.cit.pangilinan.stillness.shared.repository.BookingRepository;
import edu.cit.pangilinan.stillness.shared.repository.InstructorRepository;
import edu.cit.pangilinan.stillness.shared.repository.RefreshTokenRepository;
import edu.cit.pangilinan.stillness.shared.repository.SessionRepository;
import edu.cit.pangilinan.stillness.shared.repository.UserRepository;
import edu.cit.pangilinan.stillness.shared.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Booking feature slice (REST endpoints).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Booking Controller Integration Tests")
class BookingControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private InstructorRepository instructorRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Session testSession;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        bookingRepository.deleteAll();
        sessionRepository.deleteAll();
        instructorRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        User instructorUser = userRepository.save(User.builder()
                .email("instructor@booking-test.com")
                .passwordHash(passwordEncoder.encode("Password123"))
                .fullName("Booking Instructor")
                .role("ROLE_INSTRUCTOR")
                .emailVerified(true)
                .build());

        userRepository.save(User.builder()
                .email("user@booking-test.com")
                .passwordHash(passwordEncoder.encode("Password123"))
                .fullName("Booking User")
                .role("ROLE_USER")
                .emailVerified(true)
                .build());

        Instructor instructor = instructorRepository.save(Instructor.builder()
                .user(instructorUser)
                .bio("Booking test instructor")
                .build());

        testSession = sessionRepository.save(Session.builder()
                .title("Yoga Session")
                .description("A yoga session for booking tests")
                .instructor(instructor)
                .sessionType("YOGA")
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(1))
                .capacity(10)
                .price(BigDecimal.ZERO)
                .location("Studio A")
                .status("ACTIVE")
                .createdBy(instructorUser)
                .build());
    }

    @Test
    @DisplayName("POST /bookings should create booking for authenticated user")
    @WithMockUser(username = "user@booking-test.com", roles = "USER")
    void createBooking_shouldSucceed() throws Exception {
        String requestBody = String.format("""
                {
                  "sessionId": "%s"
                }
                """, testSession.getId());

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookingNumber").exists());
    }

    @Test
    @DisplayName("GET /bookings/me should return user bookings")
    @WithMockUser(username = "user@booking-test.com", roles = "USER")
    void getMyBookings_shouldReturnBookings() throws Exception {
        mockMvc.perform(get("/bookings/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /bookings should return 401 for unauthenticated request")
    void createBooking_shouldReturn401WhenNotAuthenticated() throws Exception {
        String requestBody = String.format("""
                {
                  "sessionId": "%s"
                }
                """, testSession.getId());

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().is3xxRedirection()); // Spring Security redirects to login (302)
    }
}
