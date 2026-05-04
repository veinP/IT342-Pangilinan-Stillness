package edu.cit.pangilinan.stillness.auth.notification;

public interface EmailNotification {
    void send();

    String getType();
}
