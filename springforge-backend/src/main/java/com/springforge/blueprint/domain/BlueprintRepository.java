package com.springforge.blueprint.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlueprintRepository {
    List<Blueprint> findAll();
    Optional<Blueprint> findById(UUID id);
    Optional<Blueprint> findByName(String name);
    List<Blueprint> findByType(ArchitectureType type);
    Blueprint save(Blueprint blueprint);
}
