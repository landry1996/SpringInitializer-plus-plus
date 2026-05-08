package com.springforge.generator.domain;

import java.util.Optional;
import java.util.UUID;

public interface GenerationRepository {
    Generation save(Generation generation);
    Optional<Generation> findById(UUID id);
}
