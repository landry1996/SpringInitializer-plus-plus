package ${packageName}.${moduleName}.domain;

import java.util.Optional;
import java.util.UUID;

public interface ${entityName}WriteRepository {
    ${entityName} save(${entityName} entity);
    Optional<${entityName}> findById(UUID id);
    void deleteById(UUID id);
}
