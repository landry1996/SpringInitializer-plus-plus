package com.springforge.generator.application.steps;

import com.springforge.generator.domain.ProjectConfiguration;
import com.springforge.generator.domain.pipeline.GenerationContext;
import com.springforge.generator.domain.pipeline.PipelineStep;
import com.springforge.generator.domain.pipeline.StepResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(2)
public class ResolveStep implements PipelineStep {

    @Override
    public StepResult execute(GenerationContext context) {
        ProjectConfiguration config = context.getConfiguration();
        List<String> resolvedDependencies = new ArrayList<>();

        resolvedDependencies.add("spring-boot-starter");
        resolvedDependencies.add("spring-boot-starter-test");

        if (config.dependencies() != null) {
            resolvedDependencies.addAll(config.dependencies());
        }

        if (resolvedDependencies.contains("spring-boot-starter-data-jpa")) {
            if (!resolvedDependencies.contains("spring-boot-starter-validation")) {
                resolvedDependencies.add("spring-boot-starter-validation");
            }
        }

        if (config.security() != null && "JWT".equals(config.security().type())) {
            resolvedDependencies.add("spring-boot-starter-security");
            resolvedDependencies.add("jjwt-api");
            resolvedDependencies.add("jjwt-impl");
            resolvedDependencies.add("jjwt-jackson");
        }

        context.put("resolvedDependencies", resolvedDependencies);
        context.put("packageName", resolvePackageName(config));

        return StepResult.ok();
    }

    private String resolvePackageName(ProjectConfiguration config) {
        if (config.metadata().packageName() != null && !config.metadata().packageName().isBlank()) {
            return config.metadata().packageName();
        }
        return config.metadata().groupId() + "." + config.metadata().artifactId().replace("-", "");
    }

    @Override
    public String name() { return "resolve"; }
}
