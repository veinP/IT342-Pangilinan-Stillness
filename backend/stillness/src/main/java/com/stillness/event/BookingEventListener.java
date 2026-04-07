package com.stillness.event;

import com.stillness.notification.EmailNotificationFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;

@Component
public class BookingEventListener {

    private final EmailNotificationFactory emailNotificationFactory;

    public BookingEventListener(EmailNotificationFactory emailNotificationFactory) {
        this.emailNotificationFactory = emailNotificationFactory;
    }

    @EventListener
    @Async
    public void onBookingCreated(BookingCreatedEvent event) {
        emailNotificationFactory
                .createNotification("BOOKING_CONFIRMATION", event.getUserEmail(), event.getBooking())
                .send();
    }
}