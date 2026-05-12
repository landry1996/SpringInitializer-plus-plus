package com.springforge.template.application;

import com.springforge.shared.exception.ResourceNotFoundException;
import com.springforge.template.domain.TemplateRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class GetTemplateUseCase {

    private final TemplateRepository templateRepository;

    public GetTemplateUseCase(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public TemplateResponse execute(UUID id) {
        return templateRepository.findById(id).map(TemplateResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Template", id));
    }
}
