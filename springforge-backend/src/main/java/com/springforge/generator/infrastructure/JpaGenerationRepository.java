package com.springforge.generator.infrastructure;

import com.springforge.generator.domain.Generation;
import com.springforge.generator.domain.GenerationRepository;
import com.springforge.generator.domain.GenerationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface SpringDataGenerationRepository extends JpaRepository<Generation, UUID> {
    List<Generation> findByStatusAndCreatedAtBefore(GenerationStatus status, LocalDateTime before);
}

@Repository
public class JpaGenerationRepository implements GenerationRepository {

    private final SpringDataGenerationRepository jpaRepository;

    public JpaGenerationRepository(SpringDataGenerationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Generation save(Generation generation) {
        return jpaRepository.save(generation);
    }

    @Override
    public Optional<Generation> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Generation> findByStatusAndCreatedAtBefore(GenerationStatus status, LocalDateTime before) {
        return jpaRepository.findByStatusAndCreatedAtBefore(status, before);
    }
}
