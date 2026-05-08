package com.springforge.generator.application;

import com.springforge.generator.domain.Generation;

import java.time.LocalDateTime;
import java.util.UUID;

public record GenerationStatusResponse(
        UUID id,
        String status,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String downloadUrl,
        String errorMessage
) {
    public static GenerationStatusResponse from(Generation gen) {
        String downloadUrl = gen.getStatus().name().equals("COMPLETED")
                ? "/api/v1/generations/" + gen.getId() + "/download"
                : null;
        return new GenerationStatusResponse(gen.getId(), gen.getStatus().name(),
                gen.getStartedAt(), gen.getCompletedAt(), downloadUrl, gen.getErrorMessage());
    }
}
