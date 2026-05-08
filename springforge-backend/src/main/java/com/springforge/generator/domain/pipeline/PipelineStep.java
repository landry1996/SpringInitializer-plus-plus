package com.springforge.generator.domain.pipeline;

public interface PipelineStep {
    StepResult execute(GenerationContext context);
    String name();
}
