package com.springforge.blueprint.infrastructure;

import com.springforge.blueprint.domain.ArchitectureType;
import com.springforge.blueprint.domain.Blueprint;
import com.springforge.blueprint.domain.BlueprintRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface SpringDataBlueprintRepository extends JpaRepository<Blueprint, UUID> {
    Optional<Blueprint> findByName(String name);
    List<Blueprint> findByType(ArchitectureType type);
}

@Repository
public class JpaBlueprintRepository implements BlueprintRepository {

    private final SpringDataBlueprintRepository jpaRepository;

    public JpaBlueprintRepository(SpringDataBlueprintRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Blueprint> findAll() { return jpaRepository.findAll(); }

    @Override
    public Optional<Blueprint> findById(UUID id) { return jpaRepository.findById(id); }

    @Override
    public Optional<Blueprint> findByName(String name) { return jpaRepository.findByName(name); }

    @Override
    public List<Blueprint> findByType(ArchitectureType type) { return jpaRepository.findByType(type); }

    @Override
    public Blueprint save(Blueprint blueprint) { return jpaRepository.save(blueprint); }
}
