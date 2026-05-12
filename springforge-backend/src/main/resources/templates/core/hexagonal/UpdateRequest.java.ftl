package ${packageName}.${moduleName}.application;

import jakarta.validation.constraints.NotBlank;

public record Update${entityName}Request(
    @NotBlank String name
) {}
