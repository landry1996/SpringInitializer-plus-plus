package com.springforge.notification.api;

import com.springforge.notification.application.NotificationService;
import com.springforge.notification.domain.*;
import com.springforge.notification.domain.DeliveryLogRepository;
import com.springforge.notification.domain.WebhookConfigRepository;
import com.springforge.shared.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/webhooks")
public class WebhookController {

    private final WebhookConfigRepository webhookConfigRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final NotificationService notificationService;

    public WebhookController(WebhookConfigRepository webhookConfigRepository,
                            DeliveryLogRepository deliveryLogRepository,
                            NotificationService notificationService) {
        this.webhookConfigRepository = webhookConfigRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<WebhookConfigResponse>> list(
            @PathVariable UUID orgId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        List<WebhookConfig> configs = webhookConfigRepository.findByOrganizationIdAndActiveTrue(orgId);
        return ResponseEntity.ok(configs.stream().map(WebhookConfigResponse::from).toList());
    }

    @PostMapping
    public ResponseEntity<WebhookConfigResponse> create(
            @PathVariable UUID orgId,
            @RequestBody CreateWebhookRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        WebhookConfig config = new WebhookConfig(
                orgId, request.name(), request.url(),
                request.channel(), request.events());
        if (request.secretToken() != null) {
            config.setSecretToken(request.secretToken());
        }
        config = webhookConfigRepository.save(config);
        return ResponseEntity.status(HttpStatus.CREATED).body(WebhookConfigResponse.from(config));
    }

    @PutMapping("/{webhookId}")
    public ResponseEntity<WebhookConfigResponse> update(
            @PathVariable UUID orgId,
            @PathVariable UUID webhookId,
            @RequestBody UpdateWebhookRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        WebhookConfig config = webhookConfigRepository.findById(webhookId)
                .orElseThrow(() -> new IllegalArgumentException("Webhook not found"));
        if (request.name() != null) config.setName(request.name());
        if (request.url() != null) config.setUrl(request.url());
        if (request.channel() != null) config.setChannel(request.channel());
        if (request.events() != null) config.setSubscribedEvents(request.events());
        if (request.active() != null) config.setActive(request.active());
        config = webhookConfigRepository.save(config);
        return ResponseEntity.ok(WebhookConfigResponse.from(config));
    }

    @DeleteMapping("/{webhookId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID orgId,
            @PathVariable UUID webhookId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        webhookConfigRepository.deleteById(webhookId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{webhookId}/test")
    public ResponseEntity<Void> test(
            @PathVariable UUID orgId,
            @PathVariable UUID webhookId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        notificationService.testWebhook(webhookId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{webhookId}/deliveries")
    public ResponseEntity<Page<DeliveryLogResponse>> deliveries(
            @PathVariable UUID orgId,
            @PathVariable UUID webhookId,
            Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser user) {
        Page<DeliveryLog> logs = deliveryLogRepository.findByWebhookConfigIdOrderByCreatedAtDesc(webhookId, pageable);
        return ResponseEntity.ok(logs.map(DeliveryLogResponse::from));
    }

    public record CreateWebhookRequest(
            String name, String url, String secretToken,
            NotificationChannel channel, List<NotificationEventType> events) {}

    public record UpdateWebhookRequest(
            String name, String url, NotificationChannel channel,
            List<NotificationEventType> events, Boolean active) {}

    public record WebhookConfigResponse(UUID id, String name, String url,
                                        NotificationChannel channel,
                                        List<NotificationEventType> events, boolean active) {
        public static WebhookConfigResponse from(WebhookConfig c) {
            return new WebhookConfigResponse(c.getId(), c.getName(), c.getUrl(),
                    c.getChannel(), c.getSubscribedEvents(), c.isActive());
        }
    }

    public record DeliveryLogResponse(UUID id, NotificationEventType eventType,
                                      int httpStatus, boolean success,
                                      int attemptCount, String createdAt) {
        public static DeliveryLogResponse from(DeliveryLog d) {
            return new DeliveryLogResponse(d.getId(), d.getEventType(),
                    d.getHttpStatus(), d.isSuccess(), d.getAttemptCount(),
                    d.getCreatedAt() != null ? d.getCreatedAt().toString() : null);
        }
    }
}
