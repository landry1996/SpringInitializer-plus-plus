package ${packageName}.${moduleName}.application;

import java.time.Instant;
import java.util.UUID;

public record ${entityName}Response(
    UUID id,
    String name,
    Instant createdAt,
    Instant updatedAt
) {}
