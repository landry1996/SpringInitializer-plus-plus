package ${packageName}.${moduleName}.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ${entityName}Repository {
    Optional<${entityName}> findById(UUID id);
    List<${entityName}> findAll(int page, int size);
    ${entityName} save(${entityName} aggregate);
    void deleteById(UUID id);
}
