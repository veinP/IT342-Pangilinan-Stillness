package edu.cit.pangilinan.stillness.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendWelcomeEmail(String to, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Welcome to StillNess!");
            message.setText(
                    "Hi " + fullName + ",\n\n" +
                    "Welcome to StillNess! We're thrilled to have you join our wellness community.\n\n" +
                    "Start exploring meditation and wellness sessions today.\n\n" +
                    "Namaste,\nThe StillNess Team"
            );
            mailSender.send(message);
        } catch (Exception e) {
            // Log but don't propagate - email failure should not break registration
        }
    }

    @Async
    public void sendBookingConfirmation(String to, String fullName, String sessionTitle) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Booking Confirmed – " + sessionTitle);
            message.setText(
                    "Hi " + fullName + ",\n\n" +
                    "Your booking for \"" + sessionTitle + "\" has been confirmed.\n\n" +
                    "We look forward to seeing you there!\n\n" +
                    "Namaste,\nThe StillNess Team"
            );
            mailSender.send(message);
        } catch (Exception e) {
            // Log but don't propagate - email failure should not break booking
        }
    }
}
