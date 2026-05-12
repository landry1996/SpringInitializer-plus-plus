package com.springforge.project.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProjectRequest(
        @NotBlank String name,
        @NotBlank String groupId,
        @NotBlank String artifactId,
        String description,
        @NotNull Object configuration
) {}
