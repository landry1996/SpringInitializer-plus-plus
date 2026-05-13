package com.springforge.notification.application;

import com.springforge.notification.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final WebhookConfigRepository webhookConfigRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final EmailSender emailSender;

    public NotificationService(WebhookConfigRepository webhookConfigRepository,
                              DeliveryLogRepository deliveryLogRepository,
                              ObjectMapper objectMapper,
                              @org.springframework.beans.factory.annotation.Autowired(required = false)
                              EmailSender emailSender) {
        this.webhookConfigRepository = webhookConfigRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.emailSender = emailSender;
    }

    @Async
    public void dispatch(UUID organizationId, NotificationEventType eventType, Map<String, Object> data) {
        List<WebhookConfig> configs = webhookConfigRepository.findByOrganizationAndEvent(organizationId, eventType);

        for (WebhookConfig config : configs) {
            try {
                String payload = objectMapper.writeValueAsString(Map.of(
                        "event", eventType.name(),
                        "timestamp", LocalDateTime.now().toString(),
                        "data", data
                ));

                DeliveryLog deliveryLog = new DeliveryLog(config.getId(), eventType, payload);
                deliveryLogRepository.save(deliveryLog);

                deliver(config, deliveryLog, payload);
            } catch (Exception e) {
                log.error("Failed to dispatch notification to webhook {}: {}", config.getId(), e.getMessage());
            }
        }
    }

    private void deliver(WebhookConfig config, DeliveryLog deliveryLog, String payload) {
        try {
            if (config.getChannel() == NotificationChannel.EMAIL) {
                deliverEmail(config, deliveryLog, payload);
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (config.getSecretToken() != null) {
                String signature = computeSignature(payload, config.getSecretToken());
                headers.set("X-Webhook-Signature", signature);
            }

            if (config.getChannel() == NotificationChannel.SLACK) {
                payload = objectMapper.writeValueAsString(Map.of("text", formatSlackMessage(deliveryLog)));
            }

            HttpEntity<String> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(config.getUrl(), request, String.class);

            deliveryLog.recordSuccess(response.getStatusCode().value(),
                    response.getBody() != null ? response.getBody().substring(0, Math.min(500, response.getBody().length())) : "");
            deliveryLogRepository.save(deliveryLog);
        } catch (Exception e) {
            LocalDateTime nextRetry = LocalDateTime.now().plusMinutes((long) Math.pow(2, deliveryLog.getAttemptCount()));
            deliveryLog.recordFailure(0, e.getMessage(), nextRetry);
            deliveryLogRepository.save(deliveryLog);
            log.warn("Webhook delivery failed for {}, attempt {}: {}", config.getUrl(), deliveryLog.getAttemptCount(), e.getMessage());
        }
    }

    private void deliverEmail(WebhookConfig config, DeliveryLog deliveryLog, String payload) {
        try {
            if (emailSender == null) {
                throw new IllegalStateException("Email notifications are not enabled");
            }
            emailSender.send(config.getUrl(), deliveryLog.getEventType(), payload);
            deliveryLog.recordSuccess(200, "Email sent successfully");
            deliveryLogRepository.save(deliveryLog);
        } catch (Exception e) {
            LocalDateTime nextRetry = LocalDateTime.now().plusMinutes((long) Math.pow(2, deliveryLog.getAttemptCount()));
            deliveryLog.recordFailure(0, e.getMessage(), nextRetry);
            deliveryLogRepository.save(deliveryLog);
            log.warn("Email delivery failed for {}, attempt {}: {}", config.getUrl(), deliveryLog.getAttemptCount(), e.getMessage());
        }
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void retryFailedDeliveries() {
        List<DeliveryLog> failed = deliveryLogRepository
                .findBySuccessFalseAndAttemptCountLessThanAndNextRetryAtBefore(MAX_RETRY_ATTEMPTS, LocalDateTime.now());

        for (DeliveryLog log : failed) {
            webhookConfigRepository.findById(log.getWebhookConfigId()).ifPresent(config -> {
                deliver(config, log, log.getPayload());
            });
        }
    }

    public void testWebhook(UUID webhookConfigId) {
        WebhookConfig config = webhookConfigRepository.findById(webhookConfigId)
                .orElseThrow(() -> new IllegalArgumentException("Webhook not found"));

        Map<String, Object> testData = Map.of("message", "Test notification from SpringForge", "test", true);
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "event", "TEST",
                    "timestamp", LocalDateTime.now().toString(),
                    "data", testData
            ));

            DeliveryLog deliveryLog = new DeliveryLog(config.getId(), NotificationEventType.GENERATION_COMPLETED, payload);
            deliveryLogRepository.save(deliveryLog);
            deliver(config, deliveryLog, payload);
        } catch (Exception e) {
            throw new RuntimeException("Test delivery failed: " + e.getMessage(), e);
        }
    }

    private String computeSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return "";
        }
    }

    private String formatSlackMessage(DeliveryLog deliveryLog) {
        return String.format("[SpringForge] %s at %s",
                deliveryLog.getEventType().name().replace("_", " ").toLowerCase(),
                LocalDateTime.now());
    }
}
