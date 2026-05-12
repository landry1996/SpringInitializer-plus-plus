package com.springforge.template.application;

import com.springforge.shared.exception.BusinessException;
import com.springforge.template.domain.Template;
import com.springforge.template.domain.TemplateRepository;
import com.springforge.template.domain.TemplateScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateTemplateUseCase {

    private final TemplateRepository templateRepository;

    public CreateTemplateUseCase(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Transactional
    public TemplateResponse execute(CreateTemplateRequest request) {
        if (templateRepository.findByPath(request.path()).isPresent()) {
            throw new BusinessException("TEMPLATE_EXISTS", "Template already exists at path: " + request.path());
        }
        Template template = new Template(request.name(), request.path(), request.category(),
                TemplateScope.CUSTOM, request.content(), request.blueprintType());
        return TemplateResponse.from(templateRepository.save(template));
    }
}
