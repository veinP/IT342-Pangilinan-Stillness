package edu.cit.pangilinan.stillness.booking.event;

import edu.cit.pangilinan.stillness.shared.model.Booking;
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