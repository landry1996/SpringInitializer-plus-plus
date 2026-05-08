package com.springforge.generator.domain.pipeline;

import com.springforge.generator.domain.ProjectConfiguration;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class GenerationContext {

    private final ProjectConfiguration configuration;
    private Path outputDirectory;
    private final Map<String, Object> metadata = new HashMap<>();

    public GenerationContext(ProjectConfiguration configuration) {
        this.configuration = configuration;
    }

    public ProjectConfiguration getConfiguration() { return configuration; }
    public Path getOutputDirectory() { return outputDirectory; }
    public void setOutputDirectory(Path outputDirectory) { this.outputDirectory = outputDirectory; }
    public void put(String key, Object value) { metadata.put(key, value); }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) { return (T) metadata.get(key); }
}
