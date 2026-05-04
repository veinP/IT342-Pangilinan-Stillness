package edu.cit.pangilinan.stillness;

import edu.cit.pangilinan.stillness.shared.model.User;
import edu.cit.pangilinan.stillness.shared.repository.InstructorRepository;
import edu.cit.pangilinan.stillness.shared.repository.BookingRepository;
import edu.cit.pangilinan.stillness.shared.repository.SessionRepository;
import edu.cit.pangilinan.stillness.shared.repository.PaymentRepository;
import edu.cit.pangilinan.stillness.shared.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings("unchecked")
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setupUser() {
        // Clear in FK-safe order: payments → bookings → sessions → instructors → users
        paymentRepository.deleteAll();
        bookingRepository.deleteAll();
        sessionRepository.deleteAll();
        instructorRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(User.builder()
                .email("safe.user@example.com")
                .passwordHash(passwordEncoder.encode("SafePass123"))
                .fullName("Safe User")
                .role("ROLE_USER")
                .emailVerified(true)
                .build());
    }

    @Test
    void loginWithValidCredentialsShouldSucceed() throws Exception {
        String requestBody = """
                {
                  "email": "safe.user@example.com",
                  "password": "SafePass123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void loginWithSqlInjectionPasswordShouldReturnUnauthorized() throws Exception {
        String requestBody = """
                {
                  "email": "safe.user@example.com",
                  "password": "' OR '1'='1"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH-001"));
    }

    @Test
        void loginWithSqlInjectionEmailShouldBeRejectedByValidation() throws Exception {
        String requestBody = """
                {
                  "email": "safe.user@example.com' OR '1'='1",
                  "password": "SafePass123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("VALID-001"));
    }
}
