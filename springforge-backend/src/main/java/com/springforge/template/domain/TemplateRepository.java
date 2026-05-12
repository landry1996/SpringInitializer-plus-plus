package com.springforge.template.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateRepository {
    List<Template> findAll();
    Optional<Template> findById(UUID id);
    List<Template> findByCategory(String category);
    List<Template> findByBlueprintType(String blueprintType);
    Optional<Template> findByPath(String path);
    Template save(Template template);
}
