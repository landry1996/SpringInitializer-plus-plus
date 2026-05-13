package com.springforge.recommendation;

import com.springforge.generator.domain.ProjectConfiguration;
import com.springforge.recommendation.rules.DependencyRecommendationRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DependencyRecommendationRuleTest {

    private DependencyRecommendationRule rule;

    @BeforeEach
    void setUp() {
        rule = new DependencyRecommendationRule();
    }

    @Test
    void shouldReturnRuleId() {
        assertThat(rule.getRuleId()).isEqualTo("dependency-complement");
    }

    @Test
    void shouldSuggestFlywayForJpa() {
        ProjectConfiguration config = configWith(List.of(
            new ProjectConfiguration.Dependency("org.springframework.boot", "spring-boot-starter-data-jpa", null, null)
        ));

        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).isNotEmpty();
        assertThat(recommendations).anyMatch(r -> r.title().contains("flyway") || r.description().contains("Flyway"));
    }

    @Test
    void shouldSuggestOpenApiForWeb() {
        ProjectConfiguration config = configWith(List.of(
            new ProjectConfiguration.Dependency("org.springframework.boot", "spring-boot-starter-web", null, null)
        ));

        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).isNotEmpty();
        assertThat(recommendations).anyMatch(r -> r.description().contains("OpenAPI") || r.description().contains("Swagger"));
    }

    @Test
    void shouldNotSuggestAlreadyPresentDependency() {
        ProjectConfiguration config = configWith(List.of(
            new ProjectConfiguration.Dependency("org.springframework.boot", "spring-boot-starter-data-jpa", null, null),
            new ProjectConfiguration.Dependency("org.flywaydb", "flyway-core", null, null)
        ));

        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).noneMatch(r -> r.id().equals("dep-flyway-core"));
    }

    @Test
    void shouldReturnEmptyForNullDependencies() {
        ProjectConfiguration config = new ProjectConfiguration(
            new ProjectConfiguration.Metadata("com.example", "demo", "demo", "", "com.example.demo", "21", "3.3.5", "MAVEN"),
            new ProjectConfiguration.Architecture("LAYERED", List.of()),
            null, null, null, null, null, null, null, null
        );

        List<Recommendation> recommendations = rule.evaluate(config);
        assertThat(recommendations).isEmpty();
    }

    private ProjectConfiguration configWith(List<ProjectConfiguration.Dependency> deps) {
        return new ProjectConfiguration(
            new ProjectConfiguration.Metadata("com.example", "demo", "demo", "", "com.example.demo", "21", "3.3.5", "MAVEN"),
            new ProjectConfiguration.Architecture("LAYERED", List.of()),
            deps, null, null, null, null, null, null, null
        );
    }
}
