package ${packageName}.${moduleName}.command;

import java.util.UUID;

public sealed interface ${entityName}Command {
    record Create(String name) implements ${entityName}Command {}
    record Update(UUID id, String name) implements ${entityName}Command {}
    record Delete(UUID id) implements ${entityName}Command {}
}
