package edu.cit.pangilinan.stillness.config;

import edu.cit.pangilinan.stillness.model.Instructor;
import edu.cit.pangilinan.stillness.model.Session;
import edu.cit.pangilinan.stillness.model.User;
import edu.cit.pangilinan.stillness.repository.InstructorRepository;
import edu.cit.pangilinan.stillness.repository.SessionRepository;
import edu.cit.pangilinan.stillness.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@SuppressWarnings("unchecked")
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (sessionRepository.count() == 0) {
            User admin = User.builder()
                .email("admin@stillness.com")
                .passwordHash(passwordEncoder.encode("password"))
                .fullName("Admin")
                .role("ADMIN")
                .build();
            userRepository.save(admin);

            Instructor instructor = Instructor.builder()
                .user(admin)
                .specialty("Yoga")
                .bio("10 years experience")
                .yearsExperience(10)
                .build();
            instructorRepository.save(instructor);

            Session s1 = Session.builder()
                .title("Guided Morning Meditation")
                .description("Relax and clear your mind before the day starts.")
                .sessionType("Meditation")
                .location("Virtual Room A")
                .price(new BigDecimal("0.00"))
                .capacity(50)
                .instructor(instructor)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status("SCHEDULED")
                .createdBy(admin)
                .build();
            sessionRepository.save(s1);

            Session s2 = Session.builder()
                .title("Evening Breathwork")
                .description("Deep breathing exercises for peaceful sleep.")
                .sessionType("Breathwork")
                .location("Virtual Room B")
                .price(new BigDecimal("25.00"))
                .capacity(20)
                .instructor(instructor)
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(1))
                .status("SCHEDULED")
                .createdBy(admin)
                .build();
            sessionRepository.save(s2);
        }
    }
}