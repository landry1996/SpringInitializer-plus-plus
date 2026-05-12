package ${packageName}.${moduleName}.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ${entityName}EventPublisher {

    private final ApplicationEventPublisher publisher;

    public ${entityName}EventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(${entityName}Event event) {
        publisher.publishEvent(event);
    }
}
