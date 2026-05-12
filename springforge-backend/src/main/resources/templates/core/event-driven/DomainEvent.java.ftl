package ${packageName}.${moduleName}.event;

import java.time.Instant;
import java.util.UUID;

public sealed interface ${entityName}Event {

    UUID eventId();
    Instant occurredOn();
    String aggregateId();

    record Created(UUID eventId, Instant occurredOn, String aggregateId, String name) implements ${entityName}Event {
        public Created(String aggregateId, String name) {
            this(UUID.randomUUID(), Instant.now(), aggregateId, name);
        }
    }

    record Updated(UUID eventId, Instant occurredOn, String aggregateId) implements ${entityName}Event {
        public Updated(String aggregateId) {
            this(UUID.randomUUID(), Instant.now(), aggregateId);
        }
    }

    record Deleted(UUID eventId, Instant occurredOn, String aggregateId) implements ${entityName}Event {
        public Deleted(String aggregateId) {
            this(UUID.randomUUID(), Instant.now(), aggregateId);
        }
    }
}
