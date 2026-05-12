package com.springforge.generator.api;

import com.springforge.generator.application.DependencyCheckResult;
import com.springforge.generator.application.DependencyConflictDetector;
import com.springforge.generator.application.GenerateProjectUseCase;
import com.springforge.generator.application.GenerateRequest;
import com.springforge.generator.application.GenerationResponse;
import com.springforge.generator.application.GenerationStatusResponse;
import com.springforge.generator.application.ValidationResult;
import com.springforge.generator.domain.Generation;
import com.springforge.generator.domain.GenerationRepository;
import com.springforge.generator.domain.ProjectConfiguration;
import com.springforge.generator.domain.pipeline.GenerationContext;
import com.springforge.generator.domain.pipeline.StepResult;
import com.springforge.generator.application.steps.ValidateStep;
import com.springforge.shared.exception.ResourceNotFoundException;
import com.springforge.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class GeneratorController {

    private final GenerateProjectUseCase generateProjectUseCase;
    private final GenerationRepository generationRepository;
    private final ValidateStep validateStep;
    private final DependencyConflictDetector conflictDetector;

    public GeneratorController(GenerateProjectUseCase generateProjectUseCase,
                               GenerationRepository generationRepository,
                               ValidateStep validateStep,
                               DependencyConflictDetector conflictDetector) {
        this.generateProjectUseCase = generateProjectUseCase;
        this.generationRepository = generationRepository;
        this.validateStep = validateStep;
        this.conflictDetector = conflictDetector;
    }

    @PostMapping("/projects/validate")
    public ResponseEntity<ValidationResult> validate(@Valid @RequestBody GenerateRequest request) {
        GenerationContext context = new GenerationContext(request.configuration());
        StepResult result = validateStep.execute(context);
        if (result.success()) {
            return ResponseEntity.ok(ValidationResult.valid());
        }
        return ResponseEntity.badRequest().body(ValidationResult.invalid(result.errors()));
    }

    @PostMapping("/dependencies/check")
    public ResponseEntity<DependencyCheckResult> checkDependencies(@RequestBody List<String> dependencies) {
        List<String> conflicts = conflictDetector.detectConflicts(dependencies);
        List<String> suggestions = conflictDetector.suggestAdditions(dependencies);
        if (conflicts.isEmpty()) {
            return ResponseEntity.ok(DependencyCheckResult.ok(suggestions));
        }
        return ResponseEntity.ok(DependencyCheckResult.withConflicts(conflicts, suggestions));
    }

    @PostMapping("/configurations/export")
    public ResponseEntity<ProjectConfiguration> exportConfig(@Valid @RequestBody GenerateRequest request) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"springforge-config.json\"")
            .body(request.configuration());
    }

    @PostMapping("/projects/generate")
    public ResponseEntity<GenerationResponse> generate(
            @Valid @RequestBody GenerateRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        UUID generationId = generateProjectUseCase.generate(request.configuration(), user.id());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(GenerationResponse.queued(generationId));
    }

    @GetMapping("/generations/{id}/status")
    public ResponseEntity<GenerationStatusResponse> getStatus(@PathVariable UUID id) {
        Generation generation = generationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Generation", id));
        return ResponseEntity.ok(GenerationStatusResponse.from(generation));
    }

    @GetMapping("/generations/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        Generation generation = generationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Generation", id));

        if (!"COMPLETED".equals(generation.getStatus().name())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Path zipPath = Path.of(generation.getOutputPath());
        if (!Files.exists(zipPath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(zipPath);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + zipPath.getFileName().toString() + "\"")
                .body(resource);
    }
}
