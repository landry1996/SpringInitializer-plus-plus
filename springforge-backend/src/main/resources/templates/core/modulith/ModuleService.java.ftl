package ${packageName}.${moduleName}.internal;

import ${packageName}.${moduleName}.api.${entityName}Api;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ${entityName}Service {

    private final ApplicationEventPublisher events;

    public ${entityName}Service(ApplicationEventPublisher events) {
        this.events = events;
    }

    public ${entityName}Api.${entityName}Dto create(${entityName}Api.Create${entityName} command) {
        UUID id = UUID.randomUUID();
        events.publishEvent(new ${entityName}Api.${entityName}Created(id, command.name()));
        return new ${entityName}Api.${entityName}Dto(id, command.name());
    }
}
