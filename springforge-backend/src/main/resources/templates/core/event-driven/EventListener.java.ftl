package ${packageName}.${moduleName}.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ${entityName}EventListener {

    private static final Logger log = LoggerFactory.getLogger(${entityName}EventListener.class);

    @Async
    @EventListener
    public void onCreated(${entityName}Event.Created event) {
        log.info("${entityName} created: {}", event.aggregateId());
    }

    @Async
    @EventListener
    public void onUpdated(${entityName}Event.Updated event) {
        log.info("${entityName} updated: {}", event.aggregateId());
    }

    @Async
    @EventListener
    public void onDeleted(${entityName}Event.Deleted event) {
        log.info("${entityName} deleted: {}", event.aggregateId());
    }
}
