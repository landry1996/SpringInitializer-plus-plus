package com.springforge.recommendation;

import com.springforge.generator.domain.ProjectConfiguration;
import com.springforge.recommendation.rules.SecurityRecommendationRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityRecommendationRuleTest {

    private SecurityRecommendationRule rule;

    @BeforeEach
    void setUp() {
        rule = new SecurityRecommendationRule();
    }

    @Test
    void shouldReturnRuleId() {
        assertThat(rule.getRuleId()).isEqualTo("security-recommendation");
    }

    @Test
    void shouldWarnWebAppWithoutSecurity() {
        ProjectConfiguration config = new ProjectConfiguration(
            new ProjectConfiguration.Metadata("com.example", "demo", "demo", "", "com.example.demo", "21", "3.3.5", "MAVEN"),
            new ProjectConfiguration.Architecture("LAYERED", List.of()),
            List.of(new ProjectConfiguration.Dependency("org.springframework.boot", "spring-boot-starter-web", null, null)),
            null, null, null, null, null, null, null
        );

        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).anyMatch(r ->
            r.type() == RecommendationType.SECURITY_ADVISORY &&
            r.title().contains("No security configured"));
    }

    @Test
    void shouldSuggestRateLimitingForWebApps() {
        ProjectConfiguration config = new ProjectConfiguration(
            new ProjectConfiguration.Metadata("com.example", "demo", "demo", "", "com.example.demo", "21", "3.3.5", "MAVEN"),
            new ProjectConfiguration.Architecture("LAYERED", List.of()),
            List.of(new ProjectConfiguration.Dependency("org.springframework.boot", "spring-boot-starter-web", null, null)),
            null, null, null, null,
            new ProjectConfiguration.SecurityConfig(true, "OAUTH2", "keycloak"),
            null, null
        );

        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).anyMatch(r -> r.title().contains("rate limiting"));
    }

    @Test
    void shouldNotWarnWhenNoWebDependency() {
        ProjectConfiguration config = new ProjectConfiguration(
            new ProjectConfiguration.Metadata("com.example", "demo", "demo", "", "com.example.demo", "21", "3.3.5", "MAVEN"),
            new ProjectConfiguration.Architecture("LAYERED", List.of()),
            List.of(new ProjectConfiguration.Dependency("org.springframework.boot", "spring-boot-starter-batch", null, null)),
            null, null, null, null, null, null, null
        );

        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).noneMatch(r -> r.title().contains("No security configured"));
    }
}
