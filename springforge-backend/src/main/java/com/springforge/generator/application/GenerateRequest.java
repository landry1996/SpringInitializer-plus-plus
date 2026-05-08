package com.springforge.generator.application;

import com.springforge.generator.domain.ProjectConfiguration;
import jakarta.validation.constraints.NotNull;

public record GenerateRequest(@NotNull ProjectConfiguration configuration) {}
