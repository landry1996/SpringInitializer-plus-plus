package com.springforge.generator.application;

import java.util.UUID;

public record GenerationProgressEvent(
    UUID generationId,
    String step,
    int progress,
    String message,
    String status
) {
    public static GenerationProgressEvent stepStarted(UUID generationId, String step, int progress) {
        return new GenerationProgressEvent(generationId, step, progress, "Executing " + step, "IN_PROGRESS");
    }

    public static GenerationProgressEvent completed(UUID generationId) {
        return new GenerationProgressEvent(generationId, "done", 100, "Generation completed", "COMPLETED");
    }

    public static GenerationProgressEvent failed(UUID generationId, String error) {
        return new GenerationProgressEvent(generationId, "error", -1, error, "FAILED");
    }
}
