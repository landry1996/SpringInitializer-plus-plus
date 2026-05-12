package com.springforge.generator.application;

import com.springforge.generator.domain.Generation;
import com.springforge.generator.domain.GenerationRepository;
import com.springforge.generator.domain.ProjectConfiguration;
import com.springforge.generator.domain.pipeline.GenerationContext;
import com.springforge.generator.domain.pipeline.GenerationPipeline;
import com.springforge.generator.domain.pipeline.PipelineStep;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GenerateProjectUseCase {

    private static final Logger log = LoggerFactory.getLogger(GenerateProjectUseCase.class);

    private final List<PipelineStep> pipelineSteps;
    private final GenerationRepository generationRepository;
    private final ObjectMapper objectMapper;

    public GenerateProjectUseCase(List<PipelineStep> pipelineSteps,
                                   GenerationRepository generationRepository,
                                   ObjectMapper objectMapper) {
        this.pipelineSteps = pipelineSteps;
        this.generationRepository = generationRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UUID generate(ProjectConfiguration config, UUID userId) {
        try {
            String configJson = objectMapper.writeValueAsString(config);
            UUID projectId = UUID.randomUUID();
            Generation generation = new Generation(projectId, userId, configJson);
            generation = generationRepository.save(generation);
            UUID generationId = generation.getId();
            executeGeneration(generationId, config);
            return generationId;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate generation", e);
        }
    }

    @Async("generationExecutor")
    public void executeGeneration(UUID generationId, ProjectConfiguration config) {
        log.info("Starting generation pipeline for: {}", generationId);

        Generation generation = generationRepository.findById(generationId)
                .orElseThrow(() -> new IllegalStateException("Generation not found: " + generationId));
        generation.start();
        generationRepository.save(generation);

        try {
            GenerationPipeline pipeline = new GenerationPipeline(pipelineSteps);
            GenerationContext context = new GenerationContext(config);
            var result = pipeline.run(context);

            generation = generationRepository.findById(generationId).orElseThrow();
            if (result.success()) {
                generation.complete(result.outputPath().toString());
                log.info("Generation completed successfully: {}", generationId);
            } else {
                String errors = String.join("; ", result.errors());
                generation.fail("Pipeline failed at step '" + result.failedStep() + "': " + errors);
                log.error("Generation failed: {}", errors);
            }
            generationRepository.save(generation);
        } catch (Exception e) {
            log.error("Unexpected error during generation: {}", generationId, e);
            generation = generationRepository.findById(generationId).orElseThrow();
            generation.fail("Unexpected error: " + e.getMessage());
            generationRepository.save(generation);
        }
    }
}
