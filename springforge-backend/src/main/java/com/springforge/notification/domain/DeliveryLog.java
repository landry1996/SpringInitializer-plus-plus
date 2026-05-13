package com.springforge.notification.domain;

import com.springforge.shared.domain.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_logs", schema = "springforge")
public class DeliveryLog extends BaseEntity {

    @Column(name = "webhook_config_id", nullable = false)
    private UUID webhookConfigId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationEventType eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "http_status")
    private int httpStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    protected DeliveryLog() {}

    public DeliveryLog(UUID webhookConfigId, NotificationEventType eventType, String payload) {
        this.webhookConfigId = webhookConfigId;
        this.eventType = eventType;
        this.payload = payload;
        this.attemptCount = 0;
        this.success = false;
    }

    public void recordSuccess(int httpStatus, String responseBody) {
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
        this.success = true;
        this.attemptCount++;
    }

    public void recordFailure(int httpStatus, String responseBody, LocalDateTime nextRetry) {
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
        this.success = false;
        this.attemptCount++;
        this.nextRetryAt = nextRetry;
    }

    public UUID getWebhookConfigId() { return webhookConfigId; }
    public NotificationEventType getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public int getHttpStatus() { return httpStatus; }
    public String getResponseBody() { return responseBody; }
    public boolean isSuccess() { return success; }
    public int getAttemptCount() { return attemptCount; }
    public LocalDateTime getNextRetryAt() { return nextRetryAt; }
}
