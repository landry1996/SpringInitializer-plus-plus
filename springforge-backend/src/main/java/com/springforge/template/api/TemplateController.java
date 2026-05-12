package com.springforge.template.api;

import com.springforge.template.application.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {

    private final ListTemplatesUseCase listTemplatesUseCase;
    private final GetTemplateUseCase getTemplateUseCase;
    private final CreateTemplateUseCase createTemplateUseCase;

    public TemplateController(ListTemplatesUseCase listTemplatesUseCase,
                              GetTemplateUseCase getTemplateUseCase,
                              CreateTemplateUseCase createTemplateUseCase) {
        this.listTemplatesUseCase = listTemplatesUseCase;
        this.getTemplateUseCase = getTemplateUseCase;
        this.createTemplateUseCase = createTemplateUseCase;
    }

    @GetMapping
    public ResponseEntity<List<TemplateResponse>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String blueprintType) {
        return ResponseEntity.ok(listTemplatesUseCase.execute(category, blueprintType));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(getTemplateUseCase.execute(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TemplateResponse> create(@Valid @RequestBody CreateTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createTemplateUseCase.execute(request));
    }
}
