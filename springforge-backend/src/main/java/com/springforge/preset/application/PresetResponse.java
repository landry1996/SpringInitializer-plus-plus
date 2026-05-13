package com.springforge.preset.application;

import com.springforge.preset.domain.Preset;

import java.time.LocalDateTime;
import java.util.UUID;

public record PresetResponse(
    UUID id,
    String name,
    String description,
    String configuration,
    UUID ownerId,
    boolean shared,
    LocalDateTime createdAt
) {
    public static PresetResponse from(Preset preset) {
        return new PresetResponse(
            preset.getId(),
            preset.getName(),
            preset.getDescription(),
            preset.getConfiguration(),
            preset.getOwnerId(),
            preset.isShared(),
            preset.getCreatedAt()
        );
    }
}
