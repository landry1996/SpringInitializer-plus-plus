package ${packageName}.${moduleName}.application;

import ${packageName}.${moduleName}.domain.${entityName};
import ${packageName}.${moduleName}.domain.${entityName}Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ${entityName}CommandHandler {

    private final ${entityName}Repository repository;

    public ${entityName}CommandHandler(${entityName}Repository repository) {
        this.repository = repository;
    }

    public UUID handle(${entityName}Command command) {
        return switch (command) {
            case ${entityName}Command.Create${entityName} c -> {
                ${entityName} aggregate = ${entityName}.create(c.name());
                repository.save(aggregate);
                yield aggregate.getId();
            }
            case ${entityName}Command.Update${entityName} u -> {
                ${entityName} existing = repository.findById(u.id())
                    .orElseThrow(() -> new IllegalArgumentException("${entityName} not found: " + u.id()));
                repository.save(existing);
                yield existing.getId();
            }
            case ${entityName}Command.Delete${entityName} d -> {
                repository.deleteById(d.id());
                yield d.id();
            }
        };
    }
}
