package com.springforge.generator.application.steps;

import com.springforge.generator.domain.ProjectConfiguration;
import com.springforge.generator.domain.pipeline.GenerationContext;
import com.springforge.generator.domain.pipeline.PipelineStep;
import com.springforge.generator.domain.pipeline.StepResult;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Order(3)
public class GenerateStep implements PipelineStep {

    private static final Logger log = LoggerFactory.getLogger(GenerateStep.class);

    private final Configuration freemarkerConfig;
    private final String outputDir;

    public GenerateStep(Configuration freemarkerConfig,
                        @Value("${app.generation.output-dir}") String outputDir) {
        this.freemarkerConfig = freemarkerConfig;
        this.outputDir = outputDir;
    }

    @Override
    public StepResult execute(GenerationContext context) {
        try {
            ProjectConfiguration config = context.getConfiguration();
            String artifactId = config.metadata().artifactId();
            String generationId = UUID.randomUUID().toString();
            Path projectDir = Path.of(outputDir, generationId, artifactId);
            Files.createDirectories(projectDir);

            Map<String, Object> model = buildModel(context);

            generatePom(projectDir, model);
            generateMainClass(projectDir, model);
            generateApplicationYml(projectDir, model, config);
            generateDockerfile(projectDir, model);
            generateGitignore(projectDir);

            if (config.infrastructure() != null && config.infrastructure().docker()) {
                generateDockerCompose(projectDir, model, config);
            }

            if (config.architecture() != null && config.architecture().modules() != null) {
                generateModuleStructure(projectDir, config, model);
            }

            context.setOutputDirectory(projectDir);
            return StepResult.ok();
        } catch (Exception e) {
            log.error("Generation step failed", e);
            return StepResult.failed("Generation failed: " + e.getMessage());
        }
    }

    private Map<String, Object> buildModel(GenerationContext context) {
        ProjectConfiguration config = context.getConfiguration();
        var meta = config.metadata();

        Map<String, Object> model = new HashMap<>();
        model.put("groupId", meta.groupId());
        model.put("artifactId", meta.artifactId());
        model.put("projectName", meta.name() != null ? meta.name() : meta.artifactId());
        model.put("projectDescription", meta.description() != null ? meta.description() : "");
        model.put("javaVersion", meta.javaVersion());
        model.put("springBootVersion", meta.springBootVersion());

        String packageName = context.get("packageName", String.class);
        if (packageName == null) {
            packageName = meta.groupId() + "." + meta.artifactId().replace("-", "");
        }
        model.put("packageName", packageName);

        String className = toPascalCase(meta.artifactId()) + "Application";
        model.put("applicationClassName", className);

        model.put("dependencies", resolveDependencyModels(context));

        return model;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> resolveDependencyModels(GenerationContext context) {
        List<String> resolvedDeps = context.get("resolvedDependencies", List.class);
        List<Map<String, String>> deps = new ArrayList<>();

        if (resolvedDeps == null) return deps;

        for (String dep : resolvedDeps) {
            Map<String, String> depModel = new HashMap<>();
            if (dep.startsWith("spring-boot-starter")) {
                depModel.put("groupId", "org.springframework.boot");
                depModel.put("artifactId", dep);
            } else if (dep.startsWith("jjwt-")) {
                depModel.put("groupId", "io.jsonwebtoken");
                depModel.put("artifactId", dep);
            } else {
                depModel.put("groupId", "org.springframework.boot");
                depModel.put("artifactId", "spring-boot-starter-" + dep);
            }
            deps.add(depModel);
        }

        return deps;
    }

    private void generatePom(Path projectDir, Map<String, Object> model) throws Exception {
        String content = renderTemplate("core/common/pom.xml.ftl", model);
        Files.writeString(projectDir.resolve("pom.xml"), content);
    }

    private void generateMainClass(Path projectDir, Map<String, Object> model) throws Exception {
        String packageName = (String) model.get("packageName");
        Path packageDir = projectDir.resolve("src/main/java/" + packageName.replace(".", "/"));
        Files.createDirectories(packageDir);

        String content = renderTemplate("core/common/Application.java.ftl", model);
        String className = (String) model.get("applicationClassName");
        Files.writeString(packageDir.resolve(className + ".java"), content);
    }

    private void generateApplicationYml(Path projectDir, Map<String, Object> model, ProjectConfiguration config) throws Exception {
        Path resourcesDir = projectDir.resolve("src/main/resources");
        Files.createDirectories(resourcesDir);

        Map<String, Object> ymlModel = new HashMap<>(model);
        if (config.dependencies() != null && config.dependencies().stream()
                .anyMatch(d -> d.contains("data-jpa") || d.contains("jdbc"))) {
            Map<String, Object> database = new HashMap<>();
            database.put("type", "postgresql");
            database.put("port", "5432");
            ymlModel.put("database", database);
        }

        String content = renderTemplate("core/common/application.yml.ftl", ymlModel);
        Files.writeString(resourcesDir.resolve("application.yml"), content);
    }

    private void generateDockerfile(Path projectDir, Map<String, Object> model) throws Exception {
        String content = renderTemplate("core/common/Dockerfile.ftl", model);
        Files.writeString(projectDir.resolve("Dockerfile"), content);
    }

    private void generateGitignore(Path projectDir) throws Exception {
        String content = renderTemplate("core/common/gitignore.ftl", Map.of());
        Files.writeString(projectDir.resolve(".gitignore"), content);
    }

    private void generateDockerCompose(Path projectDir, Map<String, Object> model, ProjectConfiguration config) throws Exception {
        Map<String, Object> composeModel = new HashMap<>(model);
        Map<String, Object> database = new HashMap<>();
        database.put("serviceName", "postgres");
        database.put("image", "postgres:16-alpine");
        database.put("port", "5432");
        database.put("volumePath", "postgresql/data");
        database.put("healthCheck", "[\"CMD-SHELL\", \"pg_isready\"]");
        Map<String, String> envVars = new HashMap<>();
        envVars.put("POSTGRES_DB", config.metadata().artifactId());
        envVars.put("POSTGRES_USER", config.metadata().artifactId());
        envVars.put("POSTGRES_PASSWORD", config.metadata().artifactId());
        database.put("envVars", envVars);
        composeModel.put("database", database);

        String content = renderTemplate("core/common/docker-compose.yml.ftl", composeModel);
        Files.writeString(projectDir.resolve("docker-compose.yml"), content);
    }

    private void generateModuleStructure(Path projectDir, ProjectConfiguration config, Map<String, Object> model) throws Exception {
        String packageName = (String) model.get("packageName");
        String archType = config.architecture().type();
        Path srcDir = projectDir.resolve("src/main/java/" + packageName.replace(".", "/"));

        for (String module : config.architecture().modules()) {
            if ("HEXAGONAL".equalsIgnoreCase(archType) || "DDD".equalsIgnoreCase(archType)) {
                Files.createDirectories(srcDir.resolve(module + "/domain"));
                Files.createDirectories(srcDir.resolve(module + "/application"));
                Files.createDirectories(srcDir.resolve(module + "/infrastructure"));
                Files.createDirectories(srcDir.resolve(module + "/api"));
            } else if ("LAYERED".equalsIgnoreCase(archType) || "MONOLITHIC".equalsIgnoreCase(archType)) {
                Files.createDirectories(srcDir.resolve("controller"));
                Files.createDirectories(srcDir.resolve("service"));
                Files.createDirectories(srcDir.resolve("repository"));
                Files.createDirectories(srcDir.resolve("model"));
            }
        }
    }

    private String renderTemplate(String templateName, Map<String, Object> model) throws Exception {
        Template template = freemarkerConfig.getTemplate(templateName);
        StringWriter writer = new StringWriter();
        template.process(model, writer);
        return writer.toString();
    }

    private String toPascalCase(String input) {
        StringBuilder result = new StringBuilder();
        boolean nextUpper = true;
        for (char c : input.toCharArray()) {
            if (c == '-' || c == '_') {
                nextUpper = true;
            } else if (nextUpper) {
                result.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    @Override
    public String name() { return "generate"; }
}
