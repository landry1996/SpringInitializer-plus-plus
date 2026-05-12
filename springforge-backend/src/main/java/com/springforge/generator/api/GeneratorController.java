package com.springforge.generator.api;

import com.springforge.generator.application.GenerateProjectUseCase;
import com.springforge.generator.application.GenerateRequest;
import com.springforge.generator.application.GenerationResponse;
import com.springforge.generator.application.GenerationStatusResponse;
import com.springforge.generator.domain.Generation;
import com.springforge.generator.domain.GenerationRepository;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class GeneratorController {

    private final GenerateProjectUseCase generateProjectUseCase;
    private final GenerationRepository generationRepository;

    public GeneratorController(GenerateProjectUseCase generateProjectUseCase,
                               GenerationRepository generationRepository) {
        this.generateProjectUseCase = generateProjectUseCase;
        this.generationRepository = generationRepository;
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
