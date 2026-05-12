package ${packageName}.${moduleName}.command;

import ${packageName}.${moduleName}.domain.${entityName};
import ${packageName}.${moduleName}.domain.${entityName}WriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ${entityName}CommandHandler {

    private final ${entityName}WriteRepository writeRepository;

    public ${entityName}CommandHandler(${entityName}WriteRepository writeRepository) {
        this.writeRepository = writeRepository;
    }

    public UUID handle(${entityName}Command command) {
        return switch (command) {
            case ${entityName}Command.Create c -> {
                ${entityName} entity = new ${entityName}(c.name());
                yield writeRepository.save(entity).getId();
            }
            case ${entityName}Command.Update u -> {
                ${entityName} entity = writeRepository.findById(u.id())
                    .orElseThrow(() -> new IllegalArgumentException("Not found: " + u.id()));
                yield writeRepository.save(entity).getId();
            }
            case ${entityName}Command.Delete d -> {
                writeRepository.deleteById(d.id());
                yield d.id();
            }
        };
    }
}
