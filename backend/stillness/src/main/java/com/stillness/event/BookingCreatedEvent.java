package com.stillness.event;

import edu.cit.pangilinan.stillness.model.Booking;
import org.springframework.context.ApplicationEvent;

public class BookingCreatedEvent extends ApplicationEvent {

    private final Booking booking;
    private final String userEmail;

    public BookingCreatedEvent(Object source, Booking booking, String userEmail) {
        super(source);
        this.booking = booking;
        this.userEmail = userEmail;
    }

    public Booking getBooking() {
        return booking;
    }

    public String getUserEmail() {
        return userEmail;
    }
}