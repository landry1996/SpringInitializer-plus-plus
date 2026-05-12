package ${packageName}.${moduleName}.application;

import java.util.UUID;

public sealed interface ${entityName}Command {

    record Create${entityName}(String name) implements ${entityName}Command {}

    record Update${entityName}(UUID id, String name) implements ${entityName}Command {}

    record Delete${entityName}(UUID id) implements ${entityName}Command {}
}
