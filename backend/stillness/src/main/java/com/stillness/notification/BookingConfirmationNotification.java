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
        if (booking == null || booking.getSession() == null || booking.getUser() == null) {
            return;
        }
        emailService.sendBookingConfirmation(
            recipientEmail, 
            booking.getUser().getFullName(), 
            booking.getSession().getTitle(),
            booking.getSession().getStartTime(),
            booking.getSession().getLocation()
        );
    }

    @Override
    public String getType() {
        return "BOOKING_CONFIRMATION";
    }
}
