package ${packageName}.${moduleName}.api;

import java.util.UUID;

public record ${entityName}Api() {

    public record Create${entityName}(String name) {}

    public record ${entityName}Created(UUID id, String name) {}

    public record ${entityName}Dto(UUID id, String name) {}
}
