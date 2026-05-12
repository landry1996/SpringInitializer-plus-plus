package com.springforge.preset.infrastructure;

import com.springforge.preset.domain.Preset;
import com.springforge.preset.domain.PresetRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaPresetRepository extends JpaRepository<Preset, UUID>, PresetRepository {

    @Override
    List<Preset> findByOwnerId(UUID ownerId);

    @Override
    @Query("SELECT p FROM Preset p WHERE p.shared = true")
    List<Preset> findShared();
}
