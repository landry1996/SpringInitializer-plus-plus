package com.springforge.template.infrastructure;

import com.springforge.template.domain.Template;
import com.springforge.template.domain.TemplateRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface SpringDataTemplateRepository extends JpaRepository<Template, UUID> {
    List<Template> findByCategory(String category);
    List<Template> findByBlueprintType(String blueprintType);
    Optional<Template> findByPath(String path);
}

@Repository
public class JpaTemplateRepository implements TemplateRepository {

    private final SpringDataTemplateRepository jpaRepository;

    public JpaTemplateRepository(SpringDataTemplateRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override public List<Template> findAll() { return jpaRepository.findAll(); }
    @Override public Optional<Template> findById(UUID id) { return jpaRepository.findById(id); }
    @Override public List<Template> findByCategory(String category) { return jpaRepository.findByCategory(category); }
    @Override public List<Template> findByBlueprintType(String blueprintType) { return jpaRepository.findByBlueprintType(blueprintType); }
    @Override public Optional<Template> findByPath(String path) { return jpaRepository.findByPath(path); }
    @Override public Template save(Template template) { return jpaRepository.save(template); }
}
