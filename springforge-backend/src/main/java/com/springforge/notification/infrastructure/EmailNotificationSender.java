package com.springforge.notification.infrastructure;

import com.springforge.notification.domain.NotificationEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

@Component
@ConditionalOnProperty(name = "notification.email.enabled", havingValue = "true")
public class EmailNotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailNotificationSender(JavaMailSender mailSender,
                                   @Value("${notification.email.from:notifications@springforge.io}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void send(String toAddress, NotificationEventType eventType, String payload) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toAddress);
            helper.setSubject("[SpringForge] " + formatSubject(eventType));
            helper.setText(buildHtmlBody(eventType, payload), true);
            mailSender.send(message);
            log.info("Email notification sent to {} for event {}", toAddress, eventType);
        } catch (Exception e) {
            log.error("Failed to send email notification to {}: {}", toAddress, e.getMessage());
            throw new RuntimeException("Email delivery failed: " + e.getMessage(), e);
        }
    }

    private String formatSubject(NotificationEventType eventType) {
        return switch (eventType) {
            case GENERATION_COMPLETED -> "Project Generation Completed";
            case GENERATION_FAILED -> "Project Generation Failed";
            case QUOTA_WARNING -> "Quota Warning - 80% Reached";
            case QUOTA_EXCEEDED -> "Quota Exceeded";
            case SUBSCRIPTION_CHANGED -> "Subscription Plan Changed";
            case MEMBER_JOINED -> "New Member Joined Your Organization";
        };
    }

    private String buildHtmlBody(NotificationEventType eventType, String payload) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                    <h2 style="color: #1976d2; margin-top: 0;">SpringForge Notification</h2>
                    <p style="color: #333; font-size: 16px;"><strong>Event:</strong> %s</p>
                    <div style="background: #f8f9fa; padding: 15px; border-radius: 4px; margin: 15px 0;">
                        <pre style="margin: 0; white-space: pre-wrap; font-size: 13px;">%s</pre>
                    </div>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #666; font-size: 12px;">This notification was sent by SpringForge. Manage your preferences in organization settings.</p>
                </div>
            </body>
            </html>
            """.formatted(formatSubject(eventType), payload);
    }
}
