package com.springforge.generator.application;

import java.util.UUID;

public record GenerationResponse(UUID generationId, String status, String statusUrl) {
    public static GenerationResponse queued(UUID id) {
        return new GenerationResponse(id, "QUEUED", "/api/v1/generations/" + id + "/status");
    }
}
