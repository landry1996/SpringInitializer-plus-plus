package com.springforge.template.application;

import jakarta.validation.constraints.NotBlank;

public record CreateTemplateRequest(
        @NotBlank String name,
        @NotBlank String path,
        @NotBlank String category,
        @NotBlank String content,
        String blueprintType
) {}
