package ${packageName}.${moduleName}.domain;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID eventId();
    Instant occurredOn();
    String aggregateId();
}

record ${entityName}CreatedEvent(UUID aggregateId) implements DomainEvent {
    ${entityName}CreatedEvent(UUID aggregateId) {
        this.aggregateId = aggregateId;
    }

    @Override
    public UUID eventId() { return UUID.randomUUID(); }

    @Override
    public Instant occurredOn() { return Instant.now(); }

    @Override
    public String aggregateId() { return aggregateId.toString(); }
}
