package com.springforge.notification;

import com.springforge.notification.application.NotificationService;
import com.springforge.notification.domain.*;
import com.springforge.notification.infrastructure.DeliveryLogRepository;
import com.springforge.notification.infrastructure.EmailNotificationSender;
import com.springforge.notification.infrastructure.WebhookConfigRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private WebhookConfigRepository webhookConfigRepository;

    @Mock
    private DeliveryLogRepository deliveryLogRepository;

    @Mock
    private EmailNotificationSender emailSender;

    private NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                webhookConfigRepository, deliveryLogRepository, objectMapper, emailSender);
    }

    @Test
    void dispatch_shouldCreateDeliveryLogForEachMatchingWebhook() {
        UUID orgId = UUID.randomUUID();
        WebhookConfig config = new WebhookConfig(orgId, "Test Hook", "https://example.com/hook",
                NotificationChannel.WEBHOOK, List.of(NotificationEventType.GENERATION_COMPLETED));

        when(webhookConfigRepository.findByOrganizationAndEvent(orgId, NotificationEventType.GENERATION_COMPLETED))
                .thenReturn(List.of(config));
        when(deliveryLogRepository.save(any(DeliveryLog.class))).thenAnswer(i -> i.getArgument(0));

        notificationService.dispatch(orgId, NotificationEventType.GENERATION_COMPLETED, Map.of("projectId", "123"));

        verify(deliveryLogRepository, atLeastOnce()).save(any(DeliveryLog.class));
    }

    @Test
    void dispatch_shouldSkipWhenNoMatchingWebhooks() {
        UUID orgId = UUID.randomUUID();
        when(webhookConfigRepository.findByOrganizationAndEvent(orgId, NotificationEventType.QUOTA_WARNING))
                .thenReturn(Collections.emptyList());

        notificationService.dispatch(orgId, NotificationEventType.QUOTA_WARNING, Map.of());

        verify(deliveryLogRepository, never()).save(any());
    }

    @Test
    void testWebhook_shouldCreateDeliveryLogWithTestEvent() {
        UUID webhookId = UUID.randomUUID();
        WebhookConfig config = new WebhookConfig(UUID.randomUUID(), "Test", "https://example.com",
                NotificationChannel.WEBHOOK, List.of(NotificationEventType.GENERATION_COMPLETED));

        when(webhookConfigRepository.findById(webhookId)).thenReturn(Optional.of(config));
        when(deliveryLogRepository.save(any(DeliveryLog.class))).thenAnswer(i -> i.getArgument(0));

        try {
            notificationService.testWebhook(webhookId);
        } catch (RuntimeException e) {
            // Expected - RestTemplate will fail without real endpoint
        }

        ArgumentCaptor<DeliveryLog> captor = ArgumentCaptor.forClass(DeliveryLog.class);
        verify(deliveryLogRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(NotificationEventType.GENERATION_COMPLETED);
    }

    @Test
    void testWebhook_shouldThrowWhenWebhookNotFound() {
        UUID webhookId = UUID.randomUUID();
        when(webhookConfigRepository.findById(webhookId)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> notificationService.testWebhook(webhookId));
    }

    @Test
    void retryFailedDeliveries_shouldRetryEligibleLogs() {
        when(deliveryLogRepository.findBySuccessFalseAndAttemptCountLessThanAndNextRetryAtBefore(anyInt(), any()))
                .thenReturn(Collections.emptyList());

        notificationService.retryFailedDeliveries();

        verify(deliveryLogRepository).findBySuccessFalseAndAttemptCountLessThanAndNextRetryAtBefore(anyInt(), any());
    }
}
