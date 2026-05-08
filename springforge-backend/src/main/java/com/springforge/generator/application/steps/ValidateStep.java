package com.springforge.generator.application.steps;

import com.springforge.generator.domain.ProjectConfiguration;
import com.springforge.generator.domain.pipeline.GenerationContext;
import com.springforge.generator.domain.pipeline.PipelineStep;
import com.springforge.generator.domain.pipeline.StepResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Order(1)
public class ValidateStep implements PipelineStep {

    private static final Set<String> SUPPORTED_JAVA_VERSIONS = Set.of("11", "17", "21", "25");
    private static final Set<String> SUPPORTED_SPRING_BOOT_PREFIXES = Set.of("2.7", "3.0", "3.1", "3.2", "3.3");

    @Override
    public StepResult execute(GenerationContext context) {
        ProjectConfiguration config = context.getConfiguration();
        List<String> errors = new ArrayList<>();

        if (config.metadata() == null) {
            errors.add("Project metadata is required");
            return StepResult.failed(errors);
        }

        var meta = config.metadata();
        if (meta.groupId() == null || meta.groupId().isBlank()) {
            errors.add("groupId is required");
        }
        if (meta.artifactId() == null || meta.artifactId().isBlank()) {
            errors.add("artifactId is required");
        }
        if (meta.javaVersion() == null || !SUPPORTED_JAVA_VERSIONS.contains(meta.javaVersion())) {
            errors.add("Unsupported Java version: " + meta.javaVersion());
        }
        if (meta.springBootVersion() != null) {
            boolean supported = SUPPORTED_SPRING_BOOT_PREFIXES.stream()
                    .anyMatch(prefix -> meta.springBootVersion().startsWith(prefix));
            if (!supported) {
                errors.add("Unsupported Spring Boot version: " + meta.springBootVersion());
            }
        }

        if (config.architecture() == null || config.architecture().type() == null) {
            errors.add("Architecture type is required");
        }

        return errors.isEmpty() ? StepResult.ok() : StepResult.failed(errors);
    }

    @Override
    public String name() { return "validate"; }
}
