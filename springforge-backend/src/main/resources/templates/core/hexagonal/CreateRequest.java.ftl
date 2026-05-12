package ${packageName}.${moduleName}.application;

import jakarta.validation.constraints.NotBlank;

public record Create${entityName}Request(
    @NotBlank String name
) {}
