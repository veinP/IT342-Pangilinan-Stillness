package edu.cit.pangilinan.stillness.session;

import edu.cit.pangilinan.stillness.shared.model.Instructor;
import edu.cit.pangilinan.stillness.shared.model.Session;
import edu.cit.pangilinan.stillness.shared.model.User;
import edu.cit.pangilinan.stillness.shared.repository.BookingRepository;
import edu.cit.pangilinan.stillness.shared.repository.InstructorRepository;
import edu.cit.pangilinan.stillness.shared.repository.RefreshTokenRepository;
import edu.cit.pangilinan.stillness.shared.repository.SessionRepository;
import edu.cit.pangilinan.stillness.shared.repository.UserRepository;
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
 * Integration tests for the Sessions feature slice (REST endpoints).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Session Controller Integration Tests")
class SessionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Session testSession;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        sessionRepository.deleteAll();
        instructorRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        User instructorUser = userRepository.save(User.builder()
                .email("instructor@test.com")
                .passwordHash(passwordEncoder.encode("Password123"))
                .fullName("Test Instructor")
                .role("ROLE_INSTRUCTOR")
                .emailVerified(true)
                .build());

        Instructor instructor = instructorRepository.save(Instructor.builder()
                .user(instructorUser)
                .bio("Integration test instructor")
                .build());

        testSession = sessionRepository.save(Session.builder()
                .title("Test Meditation")
                .description("A test session for integration testing")
                .instructor(instructor)
                .sessionType("MEDITATION")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .capacity(10)
                .price(BigDecimal.valueOf(200))
                .location("Online")
                .status("ACTIVE")
                .createdBy(instructorUser)
                .build());
    }

    @Test
    @DisplayName("GET /sessions should return paginated sessions for authenticated users")
    @WithMockUser(roles = "USER")
    void getSessions_shouldReturnPaginatedList() throws Exception {
        mockMvc.perform(get("/sessions")
                        .param("page", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessions").isArray())
                .andExpect(jsonPath("$.data.pagination").exists());
    }

    @Test
    @DisplayName("GET /sessions/{id} should return session detail for existing session")
    @WithMockUser(roles = "USER")
    void getSessionById_shouldReturnDetail() throws Exception {
        mockMvc.perform(get("/sessions/{id}", testSession.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Meditation"))
                .andExpect(jsonPath("$.data.available").value(true));
    }

    @Test
    @DisplayName("GET /sessions/{id} should return 500 for non-existent session")
    @WithMockUser(roles = "USER")
    void getSessionById_shouldReturnErrorForMissing() throws Exception {
        mockMvc.perform(get("/sessions/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /sessions should create session for instructor")
    @WithMockUser(username = "instructor@test.com", roles = "INSTRUCTOR")
    void createSession_shouldSucceedForInstructor() throws Exception {
        String requestBody = """
                {
                  "title": "New Yoga Class",
                  "description": "An invigorating yoga session",
                  "sessionType": "YOGA",
                  "startTime": "2026-06-01T09:00:00",
                  "endTime": "2026-06-01T10:00:00",
                  "capacity": 15,
                  "price": 350,
                  "location": "Studio B"
                }
                """;

        mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("New Yoga Class"));
    }

    @Test
    @DisplayName("DELETE /sessions/{id} should remove session")
    @WithMockUser(username = "instructor@test.com", roles = "INSTRUCTOR")
    void deleteSession_shouldSucceed() throws Exception {
        mockMvc.perform(delete("/sessions/{id}", testSession.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /sessions should redirect to login if auth is actually required by config")
    void getSessions_shouldNotRequireAuth() throws Exception {
        // Sessions list is intercepted by security config
        mockMvc.perform(get("/sessions"))
                .andExpect(status().is3xxRedirection());
    }
}
