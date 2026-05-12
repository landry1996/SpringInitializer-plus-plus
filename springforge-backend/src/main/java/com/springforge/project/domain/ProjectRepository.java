package com.springforge.project.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {
    Project save(Project project);
    Optional<Project> findById(UUID id);
    List<Project> findByOwnerId(UUID ownerId);
    void deleteById(UUID id);
}
