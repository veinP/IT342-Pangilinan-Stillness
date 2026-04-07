package com.stillness.notification;

import edu.cit.pangilinan.stillness.dto.response.UserDto;
import edu.cit.pangilinan.stillness.model.Booking;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationFactory {

    private final ObjectProvider<WelcomeEmailNotification> welcomeNotificationProvider;
    private final ObjectProvider<BookingConfirmationNotification> bookingConfirmationNotificationProvider;

    public EmailNotificationFactory(
            ObjectProvider<WelcomeEmailNotification> welcomeNotificationProvider,
            ObjectProvider<BookingConfirmationNotification> bookingConfirmationNotificationProvider
    ) {
        this.welcomeNotificationProvider = welcomeNotificationProvider;
        this.bookingConfirmationNotificationProvider = bookingConfirmationNotificationProvider;
    }

    public EmailNotification createNotification(String type, String email, Object data) {
        switch (type) {
            case "WELCOME":
                return welcomeNotificationProvider.getObject().configure(email, (UserDto) data);
            case "BOOKING_CONFIRMATION":
                return bookingConfirmationNotificationProvider.getObject().configure(email, (Booking) data);
            default:
                throw new IllegalArgumentException("Unknown notification type: " + type);
        }
    }
}
