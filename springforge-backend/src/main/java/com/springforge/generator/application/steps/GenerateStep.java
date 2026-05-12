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

            generateBuildFile(projectDir, model, config);
            generateMainClass(projectDir, model);
            generateApplicationYml(projectDir, model, config);
            generateDockerfile(projectDir, model);
            generateGitignore(projectDir);

            if (config.infrastructure() != null && config.infrastructure().docker()) {
                generateDockerCompose(projectDir, model, config);
            }

            if (config.infrastructure() != null && config.infrastructure().kubernetes()) {
                generateKubernetesManifests(projectDir, model);
            }

            if (config.infrastructure() != null && config.infrastructure().ci() != null) {
                generateCiCd(projectDir, model, config);
            }

            if (config.architecture() != null && config.architecture().modules() != null) {
                generateModuleStructure(projectDir, config, model);
            }

            if (config.messaging() != null && "KAFKA".equalsIgnoreCase(config.messaging().type())) {
                generateKafkaConfig(projectDir, model, config);
            }

            if (config.observability() != null && config.observability().enabled()) {
                generateObservability(projectDir, model);
            }

            if (config.testing() != null && config.testing().enabled()) {
                generateTestingConfig(projectDir, model, config);
            }

            if (config.multiTenant() != null && config.multiTenant().enabled()) {
                generateMultiTenant(projectDir, model, config);
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

    private void generateBuildFile(Path projectDir, Map<String, Object> model, ProjectConfiguration config) throws Exception {
        String buildTool = config.metadata().buildTool() != null ? config.metadata().buildTool().name() : "MAVEN";
        model.put("buildTool", buildTool);

        if ("GRADLE_KOTLIN".equals(buildTool)) {
            Files.writeString(projectDir.resolve("build.gradle.kts"),
                    renderTemplate("core/common/build.gradle.kts.ftl", model));
            Files.writeString(projectDir.resolve("settings.gradle.kts"),
                    renderTemplate("core/common/settings.gradle.kts.ftl", model));
        } else if ("GRADLE_GROOVY".equals(buildTool)) {
            Files.writeString(projectDir.resolve("build.gradle"),
                    renderTemplate("core/common/build.gradle.ftl", model));
            Files.writeString(projectDir.resolve("settings.gradle.kts"),
                    renderTemplate("core/common/settings.gradle.kts.ftl", model));
        } else {
            Files.writeString(projectDir.resolve("pom.xml"),
                    renderTemplate("core/common/pom.xml.ftl", model));
        }
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

    private void generateKubernetesManifests(Path projectDir, Map<String, Object> model) throws Exception {
        Path k8sDir = projectDir.resolve("k8s");
        Files.createDirectories(k8sDir);
        Files.writeString(k8sDir.resolve("deployment.yaml"), renderTemplate("core/kubernetes/deployment.yaml.ftl", model));
        Files.writeString(k8sDir.resolve("service.yaml"), renderTemplate("core/kubernetes/service.yaml.ftl", model));
        Files.writeString(k8sDir.resolve("hpa.yaml"), renderTemplate("core/kubernetes/hpa.yaml.ftl", model));
        Files.writeString(k8sDir.resolve("ingress.yaml"), renderTemplate("core/kubernetes/ingress.yaml.ftl", model));
        Files.writeString(k8sDir.resolve("configmap.yaml"), renderTemplate("core/kubernetes/configmap.yaml.ftl", model));
        Files.writeString(k8sDir.resolve("networkpolicy.yaml"), renderTemplate("core/kubernetes/networkpolicy.yaml.ftl", model));
    }

    private void generateCiCd(Path projectDir, Map<String, Object> model, ProjectConfiguration config) throws Exception {
        String ci = config.infrastructure().ci();
        if ("GITHUB_ACTIONS".equals(ci)) {
            Path workflowsDir = projectDir.resolve(".github/workflows");
            Files.createDirectories(workflowsDir);
            Files.writeString(workflowsDir.resolve("ci.yml"), renderTemplate("core/cicd/github-actions.yml.ftl", model));
        } else if ("GITLAB_CI".equals(ci)) {
            Files.writeString(projectDir.resolve(".gitlab-ci.yml"), renderTemplate("core/cicd/gitlab-ci.yml.ftl", model));
        }
    }

    private void generateModuleStructure(Path projectDir, ProjectConfiguration config, Map<String, Object> model) throws Exception {
        String packageName = (String) model.get("packageName");
        String archType = config.architecture().type();
        Path srcDir = projectDir.resolve("src/main/java/" + packageName.replace(".", "/"));

        for (String module : config.architecture().modules()) {
            String entityName = toPascalCase(module);
            Map<String, Object> moduleModel = new HashMap<>(model);
            moduleModel.put("moduleName", module);
            moduleModel.put("entityName", entityName);

            if ("HEXAGONAL".equalsIgnoreCase(archType)) {
                generateHexagonalModule(srcDir, module, moduleModel);
            } else if ("DDD".equalsIgnoreCase(archType)) {
                generateDddModule(srcDir, module, moduleModel);
            } else if ("CQRS".equalsIgnoreCase(archType)) {
                generateCqrsModule(srcDir, module, moduleModel);
            } else if ("EVENT_DRIVEN".equalsIgnoreCase(archType)) {
                generateEventDrivenModule(srcDir, module, moduleModel);
            } else if ("MODULITH".equalsIgnoreCase(archType)) {
                generateModulithModule(srcDir, module, moduleModel);
            } else if ("MICROSERVICES".equalsIgnoreCase(archType)) {
                generateMicroservicesScaffold(projectDir, config, model);
                return;
            } else {
                generateLayeredModule(srcDir, module, moduleModel);
            }
        }
    }

    private void generateHexagonalModule(Path srcDir, String module, Map<String, Object> model) throws Exception {
        String entityName = (String) model.get("entityName");
        Path domainDir = srcDir.resolve(module + "/domain");
        Path appDir = srcDir.resolve(module + "/application");
        Path infraDir = srcDir.resolve(module + "/infrastructure");
        Path apiDir = srcDir.resolve(module + "/api");
        Files.createDirectories(domainDir);
        Files.createDirectories(appDir);
        Files.createDirectories(infraDir);
        Files.createDirectories(apiDir);

        Files.writeString(domainDir.resolve(entityName + ".java"),
                renderTemplate("core/hexagonal/DomainEntity.java.ftl", model));
        Files.writeString(domainDir.resolve(entityName + "Repository.java"),
                renderTemplate("core/hexagonal/Repository.java.ftl", model));
        Files.writeString(appDir.resolve(entityName + "UseCase.java"),
                renderTemplate("core/hexagonal/UseCase.java.ftl", model));
        Files.writeString(appDir.resolve(entityName + "UseCaseImpl.java"),
                renderTemplate("core/hexagonal/UseCaseImpl.java.ftl", model));
        Files.writeString(infraDir.resolve(entityName + "JpaAdapter.java"),
                renderTemplate("core/hexagonal/JpaAdapter.java.ftl", model));
        Files.writeString(apiDir.resolve(entityName + "Controller.java"),
                renderTemplate("core/hexagonal/Controller.java.ftl", model));
        Files.writeString(srcDir.resolve(module + "/package-info.java"),
                renderTemplate("core/hexagonal/package-info.java.ftl", model));
    }

    private void generateDddModule(Path srcDir, String module, Map<String, Object> model) throws Exception {
        String entityName = (String) model.get("entityName");
        Path domainDir = srcDir.resolve(module + "/domain");
        Path appDir = srcDir.resolve(module + "/application");
        Path infraDir = srcDir.resolve(module + "/infrastructure");
        Path apiDir = srcDir.resolve(module + "/api");
        Files.createDirectories(domainDir);
        Files.createDirectories(appDir);
        Files.createDirectories(infraDir);
        Files.createDirectories(apiDir);

        Files.writeString(domainDir.resolve(entityName + ".java"),
                renderTemplate("core/ddd/Aggregate.java.ftl", model));
        Files.writeString(domainDir.resolve(entityName + "Id.java"),
                renderTemplate("core/ddd/ValueObject.java.ftl", model));
        Files.writeString(domainDir.resolve("DomainEvent.java"),
                renderTemplate("core/ddd/DomainEvent.java.ftl", model));
        Files.writeString(domainDir.resolve(entityName + "Repository.java"),
                renderTemplate("core/ddd/Repository.java.ftl", model));
        Files.writeString(appDir.resolve(entityName + "Command.java"),
                renderTemplate("core/ddd/Command.java.ftl", model));
        Files.writeString(appDir.resolve(entityName + "Query.java"),
                renderTemplate("core/ddd/Query.java.ftl", model));
        Files.writeString(appDir.resolve(entityName + "CommandHandler.java"),
                renderTemplate("core/ddd/CommandHandler.java.ftl", model));
        Files.writeString(srcDir.resolve(module + "/package-info.java"),
                renderTemplate("core/ddd/package-info.java.ftl", model));
    }

    private void generateLayeredModule(Path srcDir, String module, Map<String, Object> model) throws Exception {
        String entityName = (String) model.get("entityName");
        Files.createDirectories(srcDir.resolve("controller"));
        Files.createDirectories(srcDir.resolve("service"));
        Files.createDirectories(srcDir.resolve("repository"));
        Files.createDirectories(srcDir.resolve("model"));

        Files.writeString(srcDir.resolve("controller/" + entityName + "Controller.java"),
                renderTemplate("core/layered/Controller.java.ftl", model));
        Files.writeString(srcDir.resolve("service/" + entityName + "Service.java"),
                renderTemplate("core/layered/Service.java.ftl", model));
        Files.writeString(srcDir.resolve("repository/" + entityName + "Repository.java"),
                renderTemplate("core/layered/Repository.java.ftl", model));
    }

    private void generateCqrsModule(Path srcDir, String module, Map<String, Object> model) throws Exception {
        String entityName = (String) model.get("entityName");
        Path domainDir = srcDir.resolve(module + "/domain");
        Path commandDir = srcDir.resolve(module + "/command");
        Path queryDir = srcDir.resolve(module + "/query");
        Path apiDir = srcDir.resolve(module + "/api");
        Files.createDirectories(domainDir);
        Files.createDirectories(commandDir);
        Files.createDirectories(queryDir);
        Files.createDirectories(apiDir);

        Files.writeString(commandDir.resolve(entityName + "Command.java"),
                renderTemplate("core/cqrs/Command.java.ftl", model));
        Files.writeString(commandDir.resolve(entityName + "CommandHandler.java"),
                renderTemplate("core/cqrs/CommandHandler.java.ftl", model));
        Files.writeString(queryDir.resolve(entityName + "Query.java"),
                renderTemplate("core/cqrs/Query.java.ftl", model));
        Files.writeString(queryDir.resolve(entityName + "QueryHandler.java"),
                renderTemplate("core/cqrs/QueryHandler.java.ftl", model));
        Files.writeString(queryDir.resolve(entityName + "View.java"),
                renderTemplate("core/cqrs/View.java.ftl", model));
        Files.writeString(domainDir.resolve(entityName + "WriteRepository.java"),
                renderTemplate("core/cqrs/WriteRepository.java.ftl", model));
        Files.writeString(domainDir.resolve(entityName + "ReadRepository.java"),
                renderTemplate("core/cqrs/ReadRepository.java.ftl", model));
    }

    private void generateEventDrivenModule(Path srcDir, String module, Map<String, Object> model) throws Exception {
        String entityName = (String) model.get("entityName");
        Path domainDir = srcDir.resolve(module + "/domain");
        Path eventDir = srcDir.resolve(module + "/event");
        Path apiDir = srcDir.resolve(module + "/api");
        Files.createDirectories(domainDir);
        Files.createDirectories(eventDir);
        Files.createDirectories(apiDir);

        Files.writeString(eventDir.resolve(entityName + "Event.java"),
                renderTemplate("core/event-driven/DomainEvent.java.ftl", model));
        Files.writeString(eventDir.resolve(entityName + "EventPublisher.java"),
                renderTemplate("core/event-driven/EventPublisher.java.ftl", model));
        Files.writeString(eventDir.resolve(entityName + "EventListener.java"),
                renderTemplate("core/event-driven/EventListener.java.ftl", model));
    }

    private void generateModulithModule(Path srcDir, String module, Map<String, Object> model) throws Exception {
        String entityName = (String) model.get("entityName");
        Path apiDir = srcDir.resolve(module + "/api");
        Path internalDir = srcDir.resolve(module + "/internal");
        Files.createDirectories(apiDir);
        Files.createDirectories(internalDir);

        Files.writeString(srcDir.resolve(module + "/package-info.java"),
                renderTemplate("core/modulith/package-info.java.ftl", model));
        Files.writeString(apiDir.resolve(entityName + "Api.java"),
                renderTemplate("core/modulith/ModuleApi.java.ftl", model));
        Files.writeString(internalDir.resolve(entityName + "Service.java"),
                renderTemplate("core/modulith/ModuleService.java.ftl", model));
    }

    private void generateMicroservicesScaffold(Path projectDir, ProjectConfiguration config, Map<String, Object> model) throws Exception {
        String packageName = (String) model.get("packageName");
        List<Map<String, String>> services = new ArrayList<>();
        for (String module : config.architecture().modules()) {
            services.add(Map.of("name", module));
        }
        Map<String, Object> msModel = new HashMap<>(model);
        msModel.put("services", services);

        Path registryDir = projectDir.resolve("service-registry");
        Files.createDirectories(registryDir);
        Path registryPkg = registryDir.resolve("src/main/java/" + packageName.replace(".", "/") + "/registry");
        Path registryRes = registryDir.resolve("src/main/resources");
        Files.createDirectories(registryPkg);
        Files.createDirectories(registryRes);
        Files.writeString(registryDir.resolve("pom.xml"),
                renderTemplate("core/microservices/service-registry-pom.xml.ftl", msModel));
        Files.writeString(registryPkg.resolve("ServiceRegistryApplication.java"),
                renderTemplate("core/microservices/ServiceRegistryApplication.java.ftl", msModel));
        Files.writeString(registryRes.resolve("application.yml"),
                renderTemplate("core/microservices/service-registry-application.yml.ftl", msModel));

        Path gatewayDir = projectDir.resolve("api-gateway");
        Files.createDirectories(gatewayDir);
        Path gatewayPkg = gatewayDir.resolve("src/main/java/" + packageName.replace(".", "/") + "/gateway");
        Path gatewayRes = gatewayDir.resolve("src/main/resources");
        Files.createDirectories(gatewayPkg);
        Files.createDirectories(gatewayRes);
        Files.writeString(gatewayDir.resolve("pom.xml"),
                renderTemplate("core/microservices/api-gateway-pom.xml.ftl", msModel));
        Files.writeString(gatewayPkg.resolve("ApiGatewayApplication.java"),
                renderTemplate("core/microservices/ApiGatewayApplication.java.ftl", msModel));
        Files.writeString(gatewayRes.resolve("application.yml"),
                renderTemplate("core/microservices/api-gateway-application.yml.ftl", msModel));

        for (String module : config.architecture().modules()) {
            Map<String, Object> svcModel = new HashMap<>(msModel);
            svcModel.put("serviceName", module);
            Path svcDir = projectDir.resolve(module);
            Files.createDirectories(svcDir);
            Path svcRes = svcDir.resolve("src/main/resources");
            Files.createDirectories(svcRes);
            Files.writeString(svcDir.resolve("pom.xml"),
                    renderTemplate("core/microservices/service-pom.xml.ftl", svcModel));
            Files.writeString(svcRes.resolve("application.yml"),
                    renderTemplate("core/microservices/service-application.yml.ftl", svcModel));
        }

        Files.writeString(projectDir.resolve("docker-compose.yml"),
                renderTemplate("core/microservices/docker-compose.yml.ftl", msModel));
    }

    private void generateKafkaConfig(Path projectDir, Map<String, Object> model, ProjectConfiguration config) throws Exception {
        String packageName = (String) model.get("packageName");
        Path kafkaDir = projectDir.resolve("src/main/java/" + packageName.replace(".", "/") + "/config/kafka");
        Path resourcesDir = projectDir.resolve("src/main/resources");
        Files.createDirectories(kafkaDir);

        Map<String, Object> kafkaModel = new HashMap<>(model);
        if (config.messaging().topics() != null) {
            kafkaModel.put("topics", config.messaging().topics());
        } else {
            kafkaModel.put("topics", List.of(Map.of("name", model.get("artifactId") + ".events", "partitions", "3", "replicas", "1")));
        }

        Files.writeString(kafkaDir.resolve("KafkaProducerConfig.java"),
                renderTemplate("core/kafka/KafkaProducerConfig.java.ftl", kafkaModel));
        Files.writeString(kafkaDir.resolve("KafkaConsumerConfig.java"),
                renderTemplate("core/kafka/KafkaConsumerConfig.java.ftl", kafkaModel));
        Files.writeString(kafkaDir.resolve("DomainEventPublisher.java"),
                renderTemplate("core/kafka/DomainEventPublisher.java.ftl", kafkaModel));
        Files.writeString(kafkaDir.resolve("KafkaTopicConfig.java"),
                renderTemplate("core/kafka/KafkaTopicConfig.java.ftl", kafkaModel));
        Files.writeString(resourcesDir.resolve("application-kafka.yml"),
                renderTemplate("core/kafka/application-kafka.yml.ftl", kafkaModel));
    }

    private void generateObservability(Path projectDir, Map<String, Object> model) throws Exception {
        String packageName = (String) model.get("packageName");
        Path obsDir = projectDir.resolve("src/main/java/" + packageName.replace(".", "/") + "/config/observability");
        Path resourcesDir = projectDir.resolve("src/main/resources");
        Files.createDirectories(obsDir);

        Files.writeString(obsDir.resolve("MetricsConfig.java"),
                renderTemplate("core/observability/MetricsConfig.java.ftl", model));
        Files.writeString(obsDir.resolve("TracingConfig.java"),
                renderTemplate("core/observability/TracingConfig.java.ftl", model));
        Files.writeString(obsDir.resolve("ApplicationHealthIndicator.java"),
                renderTemplate("core/observability/HealthIndicatorConfig.java.ftl", model));
        Files.writeString(resourcesDir.resolve("logback-spring.xml"),
                renderTemplate("core/observability/logback-spring.xml.ftl", model));
        Files.writeString(resourcesDir.resolve("application-observability.yml"),
                renderTemplate("core/observability/application-observability.yml.ftl", model));
    }

    private void generateTestingConfig(Path projectDir, Map<String, Object> model, ProjectConfiguration config) throws Exception {
        String packageName = (String) model.get("packageName");
        Path testDir = projectDir.resolve("src/test/java/" + packageName.replace(".", "/"));
        Files.createDirectories(testDir);

        Map<String, Object> testModel = new HashMap<>(model);
        testModel.put("mainClassName", model.get("applicationClassName"));
        testModel.put("testcontainers", config.testing().testcontainers());
        if (config.architecture() != null) {
            testModel.put("architecture", config.architecture().type());
        }
        if (config.messaging() != null && "KAFKA".equalsIgnoreCase(config.messaging().type())) {
            testModel.put("kafka", true);
        }

        Files.writeString(testDir.resolve(model.get("applicationClassName") + "Test.java"),
                renderTemplate("core/testing/ApplicationTest.java.ftl", testModel));
        Files.writeString(testDir.resolve("ArchitectureTest.java"),
                renderTemplate("core/testing/ArchitectureTest.java.ftl", testModel));

        if (config.testing().testcontainers()) {
            Path configDir = testDir.resolve("config");
            Files.createDirectories(configDir);
            Files.writeString(configDir.resolve("TestcontainersConfig.java"),
                    renderTemplate("core/testing/TestcontainersConfig.java.ftl", testModel));
            Files.writeString(testDir.resolve("IntegrationTest.java"),
                    renderTemplate("core/testing/IntegrationTest.java.ftl", testModel));
        }
    }

    private void generateMultiTenant(Path projectDir, Map<String, Object> model, ProjectConfiguration config) throws Exception {
        String packageName = (String) model.get("packageName");
        Path mtDir = projectDir.resolve("src/main/java/" + packageName.replace(".", "/") + "/config/multitenant");
        Files.createDirectories(mtDir);

        Map<String, Object> mtModel = new HashMap<>(model);
        mtModel.put("multiTenantStrategy", config.multiTenant().strategy());

        Files.writeString(mtDir.resolve("TenantContext.java"),
                renderTemplate("core/multitenant/TenantContext.java.ftl", mtModel));
        Files.writeString(mtDir.resolve("TenantFilter.java"),
                renderTemplate("core/multitenant/TenantFilter.java.ftl", mtModel));
        Files.writeString(mtDir.resolve("TenantResolver.java"),
                renderTemplate("core/multitenant/TenantResolver.java.ftl", mtModel));
        Files.writeString(mtDir.resolve("TenantAwareEntity.java"),
                renderTemplate("core/multitenant/TenantAwareEntity.java.ftl", mtModel));
        Files.writeString(mtDir.resolve("TenantHibernateConfig.java"),
                renderTemplate("core/multitenant/TenantHibernateConfig.java.ftl", mtModel));
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
