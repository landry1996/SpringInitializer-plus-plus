package com.springforge.intellij.api;

import java.util.List;
import java.util.Map;

public class ApiModels {

    public static class ProjectConfiguration {
        public Metadata metadata = new Metadata();
        public Architecture architecture = new Architecture();
        public List<Dependency> dependencies;
        public SecurityConfig security;
        public ObservabilityConfig observability;
        public MessagingConfig messaging;
        public InfrastructureConfig infrastructure;
    }

    public static class Metadata {
        public String groupId = "com.example";
        public String artifactId = "demo";
        public String name = "demo";
        public String description = "";
        public String packageName = "com.example.demo";
        public String javaVersion = "21";
        public String springBootVersion = "3.3.5";
        public String buildTool = "MAVEN";
    }

    public static class Architecture {
        public String type = "LAYERED";
        public List<String> modules;
    }

    public static class Dependency {
        public String groupId;
        public String artifactId;
        public String version;
        public String scope;
    }

    public static class SecurityConfig {
        public boolean enabled;
        public String type;
        public String provider;
    }

    public static class ObservabilityConfig {
        public boolean enabled;
        public boolean metrics;
        public boolean tracing;
        public boolean structuredLogging;
    }

    public static class MessagingConfig {
        public String type;
        public List<String> topics;
    }

    public static class InfrastructureConfig {
        public boolean docker;
        public boolean helm;
        public boolean ci;
    }

    public static class ValidationResult {
        public boolean valid;
        public List<String> errors;
    }

    public static class GenerateResponse {
        public String generationId;
        public String status;
    }

    public static class StatusResponse {
        public String generationId;
        public String status;
        public int progress;
        public String errorMessage;
    }
}
