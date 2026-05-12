package ${packageName}.${moduleName}.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ${entityName} {

    private UUID id;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected ${entityName}() {
        this.id = UUID.randomUUID();
    }

    public static ${entityName} create(String name) {
        ${entityName} aggregate = new ${entityName}();
        aggregate.registerEvent(new ${entityName}CreatedEvent(aggregate.getId()));
        return aggregate;
    }

    public UUID getId() { return id; }

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
