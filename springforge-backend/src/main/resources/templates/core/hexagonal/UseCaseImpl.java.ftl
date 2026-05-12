package ${packageName}.${moduleName}.application;

import ${packageName}.${moduleName}.domain.${entityName};
import ${packageName}.${moduleName}.domain.${entityName}Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ${entityName}UseCaseImpl implements ${entityName}UseCase {

    private final ${entityName}Repository repository;

    public ${entityName}UseCaseImpl(${entityName}Repository repository) {
        this.repository = repository;
    }

    @Override
    public ${entityName}Response execute(${entityName}Command command) {
        ${entityName} entity = new ${entityName}(command.name());
        ${entityName} saved = repository.save(entity);
        return new ${entityName}Response(saved.getId(), saved.getName());
    }
}
