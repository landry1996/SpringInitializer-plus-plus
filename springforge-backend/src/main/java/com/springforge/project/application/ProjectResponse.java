package com.springforge.project.application;

import com.springforge.project.domain.Project;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String groupId,
        String artifactId,
        String description,
        String configJson,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getGroupId(),
                project.getArtifactId(),
                project.getDescription(),
                project.getConfigJson(),
                project.getStatus().name(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
