package ${packageName}.${moduleName}.domain;

import ${packageName}.${moduleName}.query.${entityName}View;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ${entityName}ReadRepository {
    Optional<${entityName}View> findViewById(UUID id);
    List<${entityName}View> findAllViews(int page, int size);
}
