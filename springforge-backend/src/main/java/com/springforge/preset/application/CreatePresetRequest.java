package com.springforge.preset.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePresetRequest(
    @NotBlank String name,
    String description,
    @NotNull String configuration,
    boolean shared
) {}
