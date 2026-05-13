package com.springforge.notification.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookConfigRepository extends JpaRepository<WebhookConfig, UUID> {

    List<WebhookConfig> findByOrganizationIdAndActiveTrue(UUID organizationId);

    @Query("SELECT w FROM WebhookConfig w JOIN w.subscribedEvents e " +
           "WHERE w.organizationId = :orgId AND e = :eventType AND w.active = true")
    List<WebhookConfig> findByOrganizationAndEvent(
            @Param("orgId") UUID organizationId,
            @Param("eventType") NotificationEventType eventType);
}
