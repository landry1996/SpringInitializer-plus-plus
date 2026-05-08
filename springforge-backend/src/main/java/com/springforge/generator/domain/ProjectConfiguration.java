package com.springforge.generator.domain;

import java.util.List;
import java.util.Map;

public record ProjectConfiguration(
        Metadata metadata,
        Architecture architecture,
        List<String> dependencies,
        SecurityConfig security,
        InfrastructureConfig infrastructure,
        GenerationOptions options
) {
    public record Metadata(
            String groupId,
            String artifactId,
            String name,
            String description,
            String packageName,
            String javaVersion,
            String springBootVersion,
            BuildTool buildTool
    ) {}

    public record Architecture(
            String type,
            List<String> modules,
            boolean enableCQRS,
            boolean enableEventSourcing
    ) {}

    public record SecurityConfig(
            String type,
            List<String> roles
    ) {}

    public record InfrastructureConfig(
            boolean docker,
            boolean dockerCompose,
            boolean kubernetes,
            String ci
    ) {}

    public record GenerationOptions(
            boolean includeExamples,
            boolean formatCode,
            boolean runCompileCheck,
            String outputFormat
    ) {}
}
