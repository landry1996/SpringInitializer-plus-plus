package com.springforge.generator;

import com.springforge.generator.application.steps.ResolveStep;
import com.springforge.generator.application.steps.ValidateStep;
import com.springforge.generator.domain.BuildTool;
import com.springforge.generator.domain.ProjectConfiguration;
import com.springforge.generator.domain.pipeline.GenerationContext;
import com.springforge.generator.domain.pipeline.StepResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenerationPipelineTest {

    @Test
    void validateStep_withValidConfig_succeeds() {
        var config = validConfiguration();
        var context = new GenerationContext(config);
        var validateStep = new ValidateStep();

        StepResult result = validateStep.execute(context);

        assertTrue(result.success());
    }

    @Test
    void validateStep_withMissingGroupId_fails() {
        var meta = new ProjectConfiguration.Metadata(
                "", "my-app", "My App", "desc", "com.example.myapp", "21", "3.3.5", BuildTool.MAVEN);
        var config = new ProjectConfiguration(meta, architecture(), List.of(), null, null, null);
        var context = new GenerationContext(config);
        var validateStep = new ValidateStep();

        StepResult result = validateStep.execute(context);

        assertFalse(result.success());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("groupId")));
    }

    @Test
    void validateStep_withUnsupportedJavaVersion_fails() {
        var meta = new ProjectConfiguration.Metadata(
                "com.example", "my-app", "My App", "desc", "com.example.myapp", "8", "3.3.5", BuildTool.MAVEN);
        var config = new ProjectConfiguration(meta, architecture(), List.of(), null, null, null);
        var context = new GenerationContext(config);
        var validateStep = new ValidateStep();

        StepResult result = validateStep.execute(context);

        assertFalse(result.success());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("Java version")));
    }

    @Test
    void resolveStep_addsBaseDependencies() {
        var config = validConfiguration();
        var context = new GenerationContext(config);
        var resolveStep = new ResolveStep();

        StepResult result = resolveStep.execute(context);

        assertTrue(result.success());
        @SuppressWarnings("unchecked")
        List<String> resolved = context.get("resolvedDependencies", List.class);
        assertNotNull(resolved);
        assertTrue(resolved.contains("spring-boot-starter"));
        assertTrue(resolved.contains("spring-boot-starter-test"));
    }

    @Test
    void resolveStep_addsSecurityDepsForJwt() {
        var security = new ProjectConfiguration.SecurityConfig("JWT", List.of("ADMIN", "USER"));
        var config = new ProjectConfiguration(metadata(), architecture(), List.of("spring-boot-starter-web"), security, null, null);
        var context = new GenerationContext(config);
        var resolveStep = new ResolveStep();

        resolveStep.execute(context);

        @SuppressWarnings("unchecked")
        List<String> resolved = context.get("resolvedDependencies", List.class);
        assertTrue(resolved.contains("spring-boot-starter-security"));
        assertTrue(resolved.contains("jjwt-api"));
    }

    @Test
    void resolveStep_resolvesPackageName() {
        var config = validConfiguration();
        var context = new GenerationContext(config);
        var resolveStep = new ResolveStep();

        resolveStep.execute(context);

        String packageName = context.get("packageName", String.class);
        assertEquals("com.example.myservice", packageName);
    }

    private ProjectConfiguration validConfiguration() {
        return new ProjectConfiguration(metadata(), architecture(),
                List.of("spring-boot-starter-web", "spring-boot-starter-data-jpa"), null, null, null);
    }

    private ProjectConfiguration.Metadata metadata() {
        return new ProjectConfiguration.Metadata(
                "com.example", "my-service", "My Service", "A test service",
                "com.example.myservice", "21", "3.3.5", BuildTool.MAVEN);
    }

    private ProjectConfiguration.Architecture architecture() {
        return new ProjectConfiguration.Architecture("HEXAGONAL", List.of("user", "order"), false, false);
    }
}
