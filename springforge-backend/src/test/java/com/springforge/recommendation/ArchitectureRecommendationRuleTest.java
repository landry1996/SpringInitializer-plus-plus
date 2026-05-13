package com.springforge.recommendation;

import com.springforge.generator.domain.ProjectConfiguration;
import com.springforge.recommendation.rules.ArchitectureRecommendationRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArchitectureRecommendationRuleTest {

    private ArchitectureRecommendationRule rule;

    @BeforeEach
    void setUp() {
        rule = new ArchitectureRecommendationRule();
    }

    @Test
    void shouldReturnRuleId() {
        assertThat(rule.getRuleId()).isEqualTo("architecture-pattern");
    }

    @ParameterizedTest
    @ValueSource(strings = {"HEXAGONAL", "DDD", "CQRS", "MICROSERVICES", "MODULITH", "EVENT_DRIVEN"})
    void shouldReturnRecommendationsForKnownArchitectures(String archType) {
        ProjectConfiguration config = configWithArch(archType);
        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).isNotEmpty();
        assertThat(recommendations).allMatch(r -> r.type() == RecommendationType.PATTERN_SUGGESTION);
    }

    @Test
    void shouldReturnEmptyForLayeredArchitecture() {
        ProjectConfiguration config = configWithArch("LAYERED");
        List<Recommendation> recommendations = rule.evaluate(config);
        assertThat(recommendations).isEmpty();
    }

    @Test
    void shouldSuggestMapStructForHexagonal() {
        ProjectConfiguration config = configWithArch("HEXAGONAL");
        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).anyMatch(r -> r.title().contains("MapStruct"));
    }

    @Test
    void shouldSuggestCircuitBreakerForMicroservices() {
        ProjectConfiguration config = configWithArch("MICROSERVICES");
        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).anyMatch(r -> r.title().contains("Circuit Breaker"));
    }

    @Test
    void shouldReturnEmptyForNullArchitecture() {
        ProjectConfiguration config = new ProjectConfiguration(
            new ProjectConfiguration.Metadata("com.example", "demo", "demo", "", "com.example.demo", "21", "3.3.5", "MAVEN"),
            null, List.of(), null, null, null, null, null, null, null
        );

        List<Recommendation> recommendations = rule.evaluate(config);
        assertThat(recommendations).isEmpty();
    }

    private ProjectConfiguration configWithArch(String archType) {
        return new ProjectConfiguration(
            new ProjectConfiguration.Metadata("com.example", "demo", "demo", "", "com.example.demo", "21", "3.3.5", "MAVEN"),
            new ProjectConfiguration.Architecture(archType, List.of()),
            List.of(), null, null, null, null, null, null, null
        );
    }
}
