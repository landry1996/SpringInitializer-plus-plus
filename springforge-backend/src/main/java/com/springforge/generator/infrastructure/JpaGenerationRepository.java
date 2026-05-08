package com.springforge.generator.infrastructure;

import com.springforge.generator.domain.Generation;
import com.springforge.generator.domain.GenerationRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
interface SpringDataGenerationRepository extends JpaRepository<Generation, UUID> {}

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
}
