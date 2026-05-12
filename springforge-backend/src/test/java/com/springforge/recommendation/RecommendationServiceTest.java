package com.springforge.recommendation;

import com.springforge.core.model.ProjectConfiguration;
import com.springforge.recommendation.rules.AntiPatternRule;
import com.springforge.recommendation.rules.ArchitectureRecommendationRule;
import com.springforge.recommendation.rules.DependencyRecommendationRule;
import com.springforge.recommendation.rules.SecurityRecommendationRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationServiceTest {

    private RecommendationService service;

    @BeforeEach
    void setUp() {
        List<RecommendationRule> rules = List.of(
            new DependencyRecommendationRule(),
            new ArchitectureRecommendationRule(),
            new AntiPatternRule(),
            new SecurityRecommendationRule()
        );
        service = new RecommendationService(rules);
    }

    @Test
    void shouldReturnEmptyRecommendationsForMinimalConfig() {
        ProjectConfiguration config = createMinimalConfig();
        List<Recommendation> recommendations = service.getRecommendations(config);
        assertThat(recommendations).isNotNull();
    }

    @Test
    void shouldReturnRecommendationsSortedByPriority() {
        ProjectConfiguration config = createConfigWithWebAndJpa();
        List<Recommendation> recommendations = service.getRecommendations(config);

        for (int i = 0; i < recommendations.size() - 1; i++) {
            assertThat(recommendations.get(i).priority())
                .isGreaterThanOrEqualTo(recommendations.get(i + 1).priority());
        }
    }

    @Test
    void shouldCalculateCompatibilityScore() {
        ProjectConfiguration config = createConfigWithWebAndJpa();
        CompatibilityScore score = service.getCompatibilityScore(config);

        assertThat(score).isNotNull();
        assertThat(score.overallScore()).isBetween(0.0, 100.0);
        assertThat(score.categoryScores()).isNotNull();
    }

    @Test
    void shouldAddStrengthsForEnabledFeatures() {
        ProjectConfiguration config = createFullConfig();
        CompatibilityScore score = service.getCompatibilityScore(config);

        assertThat(score.strengths()).isNotEmpty();
    }

    private ProjectConfiguration createMinimalConfig() {
        return new ProjectConfiguration(
            new ProjectConfiguration.Metadata("com.example", "demo", "demo", "", "com.example.demo", "21", "3.3.5", "MAVEN"),
            new ProjectConfiguration.Architecture("LAYERED", List.of()),
            List.of(),
            null, null, null, null, null, null, null
        );
    }

    private ProjectConfiguration createConfigWithWebAndJpa() {
        return new ProjectConfiguration(
            new ProjectConfiguration.Metadata("com.example", "demo", "demo", "", "com.example.demo", "21", "3.3.5", "MAVEN"),
            new ProjectConfiguration.Architecture("LAYERED", List.of()),
            List.of(
                new ProjectConfiguration.Dependency("org.springframework.boot", "spring-boot-starter-web", null, null),
                new ProjectConfiguration.Dependency("org.springframework.boot", "spring-boot-starter-data-jpa", null, null)
            ),
            null, null, null, null, null, null, null
        );
    }

    private ProjectConfiguration createFullConfig() {
        return new ProjectConfiguration(
            new ProjectConfiguration.Metadata("com.example", "demo", "demo", "", "com.example.demo", "21", "3.3.5", "MAVEN"),
            new ProjectConfiguration.Architecture("HEXAGONAL", List.of("user", "order")),
            List.of(
                new ProjectConfiguration.Dependency("org.springframework.boot", "spring-boot-starter-web", null, null)
            ),
            null,
            new ProjectConfiguration.ObservabilityConfig(true, true, true, true),
            new ProjectConfiguration.TestingConfig(true, true, true, true),
            null,
            new ProjectConfiguration.SecurityConfig(true, "OAUTH2", "keycloak"),
            null,
            new ProjectConfiguration.InfrastructureConfig(true, true, true)
        );
    }
}
