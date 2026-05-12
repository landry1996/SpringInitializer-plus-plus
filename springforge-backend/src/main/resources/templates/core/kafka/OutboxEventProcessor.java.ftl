package ${packageName}.config.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class OutboxEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventProcessor.class);

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OutboxEventProcessor(OutboxEventRepository outboxRepository,
                                 KafkaTemplate<String, Object> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> events = outboxRepository.findByPublishedFalseOrderByCreatedAtAsc();
        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload())
                    .get();
                event.markPublished();
                outboxRepository.save(event);
                log.debug("Published outbox event {} to topic {}", event.getId(), event.getTopic());
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
                break;
            }
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupPublished() {
        Instant threshold = Instant.now().minus(7, ChronoUnit.DAYS);
        int deleted = outboxRepository.deletePublishedBefore(threshold);
        if (deleted > 0) {
            log.info("Cleaned up {} published outbox events older than 7 days", deleted);
        }
    }
}
