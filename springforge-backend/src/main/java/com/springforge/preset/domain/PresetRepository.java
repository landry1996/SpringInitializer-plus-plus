package com.springforge.preset.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PresetRepository {
    Preset save(Preset preset);
    Optional<Preset> findById(UUID id);
    List<Preset> findByOwnerId(UUID ownerId);
    List<Preset> findShared();
    void deleteById(UUID id);
}
