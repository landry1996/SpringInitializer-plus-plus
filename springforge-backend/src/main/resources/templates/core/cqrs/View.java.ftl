package ${packageName}.${moduleName}.query;

import java.time.LocalDateTime;
import java.util.UUID;

public record ${entityName}View(
    UUID id,
    String name,
    LocalDateTime createdAt
) {}
