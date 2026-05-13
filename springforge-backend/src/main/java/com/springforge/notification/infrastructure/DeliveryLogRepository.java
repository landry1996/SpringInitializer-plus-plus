package com.springforge.notification.infrastructure;

import com.springforge.notification.domain.DeliveryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, UUID> {

    Page<DeliveryLog> findByWebhookConfigIdOrderByCreatedAtDesc(UUID webhookConfigId, Pageable pageable);

    List<DeliveryLog> findBySuccessFalseAndAttemptCountLessThanAndNextRetryAtBefore(
            int maxAttempts, LocalDateTime before);
}
