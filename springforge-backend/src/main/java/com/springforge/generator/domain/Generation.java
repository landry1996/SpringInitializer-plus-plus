package com.springforge.generator.domain;

import com.springforge.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "generations", schema = "springforge")
public class Generation extends BaseEntity {

    @Column(nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenerationStatus status;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @Column(length = 500)
    private String outputPath;

    @Column(length = 1000)
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String configurationJson;

    protected Generation() {}

    public Generation(UUID projectId, UUID userId, String configurationJson) {
        this.projectId = projectId;
        this.userId = userId;
        this.configurationJson = configurationJson;
        this.status = GenerationStatus.QUEUED;
    }

    public void start() {
        this.status = GenerationStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    public void complete(String outputPath) {
        this.status = GenerationStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.outputPath = outputPath;
    }

    public void fail(String errorMessage) {
        this.status = GenerationStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    public UUID getProjectId() { return projectId; }
    public UUID getUserId() { return userId; }
    public GenerationStatus getStatus() { return status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public String getOutputPath() { return outputPath; }
    public String getErrorMessage() { return errorMessage; }
    public String getConfigurationJson() { return configurationJson; }
}
