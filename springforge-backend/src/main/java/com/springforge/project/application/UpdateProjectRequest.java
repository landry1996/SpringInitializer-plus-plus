package com.springforge.project.application;

import jakarta.validation.constraints.NotNull;

public record UpdateProjectRequest(
        String name,
        String description,
        @NotNull Object configuration
) {}
