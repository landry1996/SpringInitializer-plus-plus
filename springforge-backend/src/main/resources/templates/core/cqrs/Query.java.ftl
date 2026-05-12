package ${packageName}.${moduleName}.query;

import java.util.UUID;

public sealed interface ${entityName}Query {
    record FindById(UUID id) implements ${entityName}Query {}
    record FindAll(int page, int size) implements ${entityName}Query {}
    record Search(String keyword) implements ${entityName}Query {}
}
