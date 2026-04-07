package com.stillness.notification;

import edu.cit.pangilinan.stillness.model.Booking;
import edu.cit.pangilinan.stillness.service.EmailService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BookingConfirmationNotification implements EmailNotification {

    private final EmailService emailService;

    private String recipientEmail;
    private Booking booking;

    public BookingConfirmationNotification(EmailService emailService) {
        this.emailService = emailService;
    }

    public BookingConfirmationNotification configure(String email, Booking booking) {
        this.recipientEmail = email;
        this.booking = booking;
        return this;
    }

    @Override
    public void send() {
        String fullName = booking != null && booking.getUser() != null && booking.getUser().getFullName() != null
                ? booking.getUser().getFullName()
                : "there";
        String sessionTitle = booking != null && booking.getSession() != null && booking.getSession().getTitle() != null
                ? booking.getSession().getTitle()
                : "your session";
        emailService.sendBookingConfirmation(recipientEmail, fullName, sessionTitle);
    }

    @Override
    public String getType() {
        return "BOOKING_CONFIRMATION";
    }
}
