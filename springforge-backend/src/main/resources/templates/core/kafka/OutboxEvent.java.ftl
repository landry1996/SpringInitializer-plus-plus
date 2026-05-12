package ${packageName}.config.kafka;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "published", nullable = false)
    private boolean published = false;

    protected OutboxEvent() {}

    public OutboxEvent(String aggregateType, String aggregateId, String eventType, String topic, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.topic = topic;
        this.payload = payload;
    }

    public UUID getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getTopic() { return topic; }
    public String getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isPublished() { return published; }

    public void markPublished() {
        this.published = true;
        this.publishedAt = Instant.now();
    }
}
