package com.springforge.generator.domain;

import java.util.List;
import java.util.Map;

public record ProjectConfiguration(
        Metadata metadata,
        Architecture architecture,
        List<String> dependencies,
        SecurityConfig security,
        InfrastructureConfig infrastructure,
        MessagingConfig messaging,
        ObservabilityConfig observability,
        TestingConfig testing,
        MultiTenantConfig multiTenant,
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

    public record MessagingConfig(
            String type,
            List<Map<String, String>> topics
    ) {}

    public record ObservabilityConfig(
            boolean enabled,
            boolean metrics,
            boolean tracing,
            boolean structuredLogging
    ) {}

    public record TestingConfig(
            boolean enabled,
            boolean testcontainers,
            boolean archunit
    ) {}

    public record MultiTenantConfig(
            boolean enabled,
            String strategy
    ) {}

    public record GenerationOptions(
            boolean includeExamples,
            boolean formatCode,
            boolean runCompileCheck,
            String outputFormat
    ) {}
}
