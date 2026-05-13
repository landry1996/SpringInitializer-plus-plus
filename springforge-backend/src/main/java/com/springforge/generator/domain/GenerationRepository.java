package com.springforge.generator.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GenerationRepository {
    Generation save(Generation generation);
    Optional<Generation> findById(UUID id);
    List<Generation> findByStatusAndCreatedAtBefore(GenerationStatus status, LocalDateTime before);
}
