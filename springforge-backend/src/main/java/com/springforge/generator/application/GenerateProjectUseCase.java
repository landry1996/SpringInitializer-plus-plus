package com.springforge.generator.application;

import com.springforge.generator.domain.Generation;
import com.springforge.generator.domain.GenerationRepository;
import com.springforge.generator.domain.ProjectConfiguration;
import com.springforge.generator.domain.pipeline.GenerationContext;
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
    private final GenerationNotifier notifier;

    public GenerateProjectUseCase(List<PipelineStep> pipelineSteps,
                                   GenerationRepository generationRepository,
                                   ObjectMapper objectMapper,
                                   GenerationNotifier notifier) {
        this.pipelineSteps = pipelineSteps;
        this.generationRepository = generationRepository;
        this.objectMapper = objectMapper;
        this.notifier = notifier;
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
            GenerationContext context = new GenerationContext(config);
            int totalSteps = pipelineSteps.size();
            int stepIndex = 0;

            for (PipelineStep step : pipelineSteps) {
                stepIndex++;
                int progress = (stepIndex * 100) / totalSteps;
                notifier.notifyProgress(generationId, step.name(), progress);

                var stepResult = step.execute(context);
                if (!stepResult.success()) {
                    String errors = String.join("; ", stepResult.errors());
                    generation = generationRepository.findById(generationId).orElseThrow();
                    generation.fail("Pipeline failed at step '" + step.name() + "': " + errors);
                    generationRepository.save(generation);
                    notifier.notifyFailed(generationId, errors);
                    log.error("Generation failed at step {}: {}", step.name(), errors);
                    return;
                }
            }

            generation = generationRepository.findById(generationId).orElseThrow();
            if (context.getOutputDirectory() != null) {
                generation.complete(context.getOutputDirectory().toString());
            } else {
                generation.complete("");
            }
            generationRepository.save(generation);
            notifier.notifyCompleted(generationId);
            log.info("Generation completed successfully: {}", generationId);
        } catch (Exception e) {
            log.error("Unexpected error during generation: {}", generationId, e);
            generation = generationRepository.findById(generationId).orElseThrow();
            generation.fail("Unexpected error: " + e.getMessage());
            generationRepository.save(generation);
            notifier.notifyFailed(generationId, e.getMessage());
        }
    }
}
