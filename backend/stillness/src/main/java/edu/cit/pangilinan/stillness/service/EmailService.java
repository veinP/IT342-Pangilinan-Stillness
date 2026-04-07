package edu.cit.pangilinan.stillness.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Send welcome email to newly registered users
     */
    @Async
    public void sendWelcomeEmail(String to, String fullName) {
        try {
            String subject = "Welcome to StillNess – Begin Your Wellness Journey";
            String htmlContent = buildWelcomeEmailTemplate(fullName);
            
            sendHtmlEmail(to, subject, htmlContent);
            log.info("Welcome email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", to, e.getMessage());
            log.error("Stack trace:", e);
            // Don't propagate - email failure should not break registration
        }
    }

    /**
     * Send booking confirmation email to users
     */
    @Async
    public void sendBookingConfirmation(String to, String fullName, String sessionTitle, 
                                       LocalDateTime sessionStartTime, String location) {
        try {
            String subject = "Booking Confirmed – " + sessionTitle;
            String htmlContent = buildBookingConfirmationTemplate(fullName, sessionTitle, 
                                                                 sessionStartTime, location);
            
            sendHtmlEmail(to, subject, htmlContent);
            log.info("Booking confirmation email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send booking confirmation to {}: {}", to, e.getMessage());
            log.error("Stack trace:", e);
            // Don't propagate - email failure should not break booking
        }
    }

    /**
     * Send cancellation confirmation email to users
    @Async
     */
    public void sendCancellationConfirmation(String to, String fullName, String sessionTitle) {
        try {
            String subject = "Booking Cancelled – " + sessionTitle;
            String htmlContent = buildCancellationEmailTemplate(fullName, sessionTitle);
            
            sendHtmlEmail(to, subject, htmlContent);
            log.info("Cancellation confirmation email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send cancellation email to {}: {}", to, e.getMessage());
            log.error("Stack trace:", e);
        }
    }

    /**
     * Generic method to send HTML emails
     */
    @SuppressWarnings("unchecked")
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        
        helper.setFrom(fromEmail, "StillNess");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML content
        
        mailSender.send(mimeMessage);
    }

    /**
     * Build HTML template for welcome email
     */
    private String buildWelcomeEmailTemplate(String fullName) {
        return "<!DOCTYPE html>\n" +
               "<html>\n" +
               "<head>\n" +
               "  <meta charset='UTF-8'>\n" +
               "  <style>\n" +
               "    body { font-family: Arial, sans-serif; color: #333; background-color: #f9fafb; }\n" +
               "    .container { max-width: 600px; margin: 0 auto; background-color: #fff; padding: 40px; border-radius: 8px; }\n" +
               "    .header { text-align: center; margin-bottom: 30px; }\n" +
               "    .header h1 { color: #2563EB; margin: 0; font-size: 28px; }\n" +
               "    .content { line-height: 1.6; color: #555; }\n" +
               "    .cta-button { display: inline-block; margin-top: 20px; padding: 12px 24px; background-color: #2563EB; color: white; text-decoration: none; border-radius: 6px; font-weight: bold; }\n" +
               "    .cta-button:hover { background-color: #1e40af; }\n" +
               "    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; font-size: 12px; color: #999; }\n" +
               "  </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "  <div class='container'>\n" +
               "    <div class='header'>\n" +
               "      <h1>🧘 Welcome to StillNess</h1>\n" +
               "    </div>\n" +
               "    <div class='content'>\n" +
               "      <p>Hi <strong>" + escapeHtml(fullName) + "</strong>,</p>\n" +
               "      <p>We're thrilled to welcome you to the StillNess community! You're now part of a vibrant wellness journey.</p>\n" +
               "      <p>Get started by:</p>\n" +
               "      <ul>\n" +
               "        <li>Exploring our meditation and wellness sessions</li>\n" +
               "        <li>Booking your first class</li>\n" +
               "        <li>Connecting with our inspiring instructors</li>\n" +
               "      </ul>\n" +
               "      <p>Ready to begin?</p>\n" +
               "      <a href='" + frontendUrl + "/sessions' class='cta-button'>Explore Sessions</a>\n" +
               "      <p>If you have any questions, feel free to reach out to our support team.</p>\n" +
               "      <p>Namaste,<br/>The StillNess Team</p>\n" +
               "    </div>\n" +
               "    <div class='footer'>\n" +
               "      <p>© 2026 StillNess. All rights reserved.</p>\n" +
               "      <p>This is an automated message, please do not reply to this email.</p>\n" +
               "    </div>\n" +
               "  </div>\n" +
               "</body>\n" +
               "</html>";
    }

    /**
     * Build HTML template for booking confirmation
     */
    private String buildBookingConfirmationTemplate(String fullName, String sessionTitle, 
                                                    LocalDateTime sessionStartTime, String location) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
        String formattedTime = sessionStartTime != null ? sessionStartTime.format(formatter) : "TBD";

        return "<!DOCTYPE html>\n" +
               "<html>\n" +
               "<head>\n" +
               "  <meta charset='UTF-8'>\n" +
               "  <style>\n" +
               "    body { font-family: Arial, sans-serif; color: #333; background-color: #f9fafb; }\n" +
               "    .container { max-width: 600px; margin: 0 auto; background-color: #fff; padding: 40px; border-radius: 8px; }\n" +
               "    .header { text-align: center; margin-bottom: 30px; }\n" +
               "    .header h1 { color: #10B981; margin: 0; font-size: 28px; }\n" +
               "    .confirmation-box { background-color: #f0fdf4; border-left: 4px solid #10B981; padding: 20px; margin: 20px 0; border-radius: 4px; }\n" +
               "    .session-details { background-color: #f3f4f6; padding: 15px; border-radius: 4px; margin: 20px 0; }\n" +
               "    .detail-row { margin: 10px 0; display: flex; align-items: flex-start; }\n" +
               "    .detail-label { font-weight: bold; color: #666; min-width: 100px; }\n" +
               "    .detail-value { color: #333; }\n" +
               "    .cta-button { display: inline-block; margin-top: 20px; padding: 12px 24px; background-color: #2563EB; color: white; text-decoration: none; border-radius: 6px; font-weight: bold; }\n" +
               "    .cta-button:hover { background-color: #1e40af; }\n" +
               "    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; font-size: 12px; color: #999; }\n" +
               "  </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "  <div class='container'>\n" +
               "    <div class='header'>\n" +
               "      <h1>✓ Booking Confirmed</h1>\n" +
               "    </div>\n" +
               "    <div class='content'>\n" +
               "      <p>Hi <strong>" + escapeHtml(fullName) + "</strong>,</p>\n" +
               "      <div class='confirmation-box'>\n" +
               "        <p>Great! Your booking has been confirmed. We can't wait to see you!</p>\n" +
               "      </div>\n" +
               "      <h3>Session Details:</h3>\n" +
               "      <div class='session-details'>\n" +
               "        <div class='detail-row'>\n" +
               "          <span class='detail-label'>Session:</span>\n" +
               "          <span class='detail-value'><strong>" + escapeHtml(sessionTitle) + "</strong></span>\n" +
               "        </div>\n" +
               "        <div class='detail-row'>\n" +
               "          <span class='detail-label'>Date & Time:</span>\n" +
               "          <span class='detail-value'>" + formattedTime + "</span>\n" +
               "        </div>\n" +
               "        <div class='detail-row'>\n" +
               "          <span class='detail-label'>Location:</span>\n" +
               "          <span class='detail-value'>" + (location != null ? escapeHtml(location) : "Online") + "</span>\n" +
               "        </div>\n" +
               "      </div>\n" +
               "      <p><strong>What to expect:</strong></p>\n" +
               "      <ul>\n" +
               "        <li>Arrive a few minutes early to settle in</li>\n" +
               "        <li>Bring a yoga mat if practicing at a physical location</li>\n" +
               "        <li>Wear comfortable clothing</li>\n" +
               "        <li>Come with an open mind and heart</li>\n" +
               "      </ul>\n" +
               "      <a href='" + frontendUrl + "/bookings' class='cta-button'>View My Bookings</a>\n" +
               "      <p>If you need to cancel, please do so at least 2 hours before the session.</p>\n" +
               "      <p>Namaste,<br/>The StillNess Team</p>\n" +
               "    </div>\n" +
               "    <div class='footer'>\n" +
               "      <p>© 2026 StillNess. All rights reserved.</p>\n" +
               "      <p>This is an automated message, please do not reply to this email.</p>\n" +
               "    </div>\n" +
               "  </div>\n" +
               "</body>\n" +
               "</html>";
    }

    /**
     * Build HTML template for cancellation email
     */
    private String buildCancellationEmailTemplate(String fullName, String sessionTitle) {
        return "<!DOCTYPE html>\n" +
               "<html>\n" +
               "<head>\n" +
               "  <meta charset='UTF-8'>\n" +
               "  <style>\n" +
               "    body { font-family: Arial, sans-serif; color: #333; background-color: #f9fafb; }\n" +
               "    .container { max-width: 600px; margin: 0 auto; background-color: #fff; padding: 40px; border-radius: 8px; }\n" +
               "    .header { text-align: center; margin-bottom: 30px; }\n" +
               "    .header h1 { color: #EF4444; margin: 0; font-size: 28px; }\n" +
               "    .cancellation-box { background-color: #fef2f2; border-left: 4px solid #EF4444; padding: 20px; margin: 20px 0; border-radius: 4px; }\n" +
               "    .cta-button { display: inline-block; margin-top: 20px; padding: 12px 24px; background-color: #2563EB; color: white; text-decoration: none; border-radius: 6px; font-weight: bold; }\n" +
               "    .cta-button:hover { background-color: #1e40af; }\n" +
               "    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; font-size: 12px; color: #999; }\n" +
               "  </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "  <div class='container'>\n" +
               "    <div class='header'>\n" +
               "      <h1>Booking Cancelled</h1>\n" +
               "    </div>\n" +
               "    <div class='content'>\n" +
               "      <p>Hi <strong>" + escapeHtml(fullName) + "</strong>,</p>\n" +
               "      <div class='cancellation-box'>\n" +
               "        <p>Your booking for <strong>" + escapeHtml(sessionTitle) + "</strong> has been cancelled.</p>\n" +
               "      </div>\n" +
               "      <p>If you'd like to book a different session, please visit our website:</p>\n" +
               "      <a href='" + frontendUrl + "/sessions' class='cta-button'>Browse Sessions</a>\n" +
               "      <p>If you have any questions or need further assistance, please let us know.</p>\n" +
               "      <p>Namaste,<br/>The StillNess Team</p>\n" +
               "    </div>\n" +
               "    <div class='footer'>\n" +
               "      <p>© 2026 StillNess. All rights reserved.</p>\n" +
               "      <p>This is an automated message, please do not reply to this email.</p>\n" +
               "    </div>\n" +
               "  </div>\n" +
               "</body>\n" +
               "</html>";
    }

    /**
     * Escape HTML special characters to prevent injection
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
