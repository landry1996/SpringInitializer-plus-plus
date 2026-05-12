package ${packageName}.${moduleName}.domain;

import java.util.Optional;
import java.util.UUID;

public interface ${entityName}Repository {
    Optional<${entityName}> findById(UUID id);
    ${entityName} save(${entityName} entity);
    void deleteById(UUID id);
}
