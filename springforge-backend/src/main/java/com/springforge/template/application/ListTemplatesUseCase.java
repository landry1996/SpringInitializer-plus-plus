package com.springforge.template.application;

import com.springforge.template.domain.TemplateRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListTemplatesUseCase {

    private final TemplateRepository templateRepository;

    public ListTemplatesUseCase(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public List<TemplateResponse> execute(String category, String blueprintType) {
        if (category != null && !category.isBlank()) {
            return templateRepository.findByCategory(category).stream().map(TemplateResponse::from).toList();
        }
        if (blueprintType != null && !blueprintType.isBlank()) {
            return templateRepository.findByBlueprintType(blueprintType).stream().map(TemplateResponse::from).toList();
        }
        return templateRepository.findAll().stream().map(TemplateResponse::from).toList();
    }
}
