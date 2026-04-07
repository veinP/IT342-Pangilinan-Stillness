package com.stillness.notification;

import edu.cit.pangilinan.stillness.dto.response.UserDto;
import edu.cit.pangilinan.stillness.service.EmailService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WelcomeEmailNotification implements EmailNotification {

    private final EmailService emailService;

    private String recipientEmail;
    private UserDto user;

    public WelcomeEmailNotification(EmailService emailService) {
        this.emailService = emailService;
    }

    public WelcomeEmailNotification configure(String email, UserDto user) {
        this.recipientEmail = email;
        this.user = user;
        return this;
    }

    @Override
    public void send() {
        String fullName = user != null && user.getFullName() != null ? user.getFullName() : "there";
        emailService.sendWelcomeEmail(recipientEmail, fullName);
    }

    @Override
    public String getType() {
        return "WELCOME";
    }
}
