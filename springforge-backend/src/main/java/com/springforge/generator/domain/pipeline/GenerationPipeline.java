package com.springforge.generator.domain.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GenerationPipeline {

    private static final Logger log = LoggerFactory.getLogger(GenerationPipeline.class);
    private final List<PipelineStep> steps;

    public GenerationPipeline(List<PipelineStep> steps) {
        this.steps = steps;
    }

    public PipelineResult run(GenerationContext context) {
        List<String> allErrors = new ArrayList<>();
        for (PipelineStep step : steps) {
            log.info("Executing pipeline step: {}", step.name());
            StepResult result = step.execute(context);
            if (!result.success()) {
                log.error("Pipeline step '{}' failed: {}", step.name(), result.errors());
                allErrors.addAll(result.errors());
                return PipelineResult.failed(step.name(), allErrors);
            }
            log.info("Pipeline step '{}' completed successfully", step.name());
        }
        return PipelineResult.success(context.getOutputDirectory());
    }

    public record PipelineResult(boolean success, String failedStep, List<String> errors, Path outputPath) {
        public static PipelineResult success(Path outputPath) {
            return new PipelineResult(true, null, List.of(), outputPath);
        }
        public static PipelineResult failed(String step, List<String> errors) {
            return new PipelineResult(false, step, errors, null);
        }
    }
}
