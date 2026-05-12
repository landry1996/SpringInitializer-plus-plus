package ${packageName}.service;

import ${packageName}.repository.${entityName}Repository;
import org.springframework.stereotype.Service;

@Service
public class ${entityName}Service {

    private final ${entityName}Repository repository;

    public ${entityName}Service(${entityName}Repository repository) {
        this.repository = repository;
    }
}
