package com.springforge.recommendation;

import com.springforge.core.model.ProjectConfiguration;
import com.springforge.recommendation.rules.AntiPatternRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AntiPatternRuleTest {

    private AntiPatternRule rule;

    @BeforeEach
    void setUp() {
        rule = new AntiPatternRule();
    }

    @Test
    void shouldReturnRuleId() {
        assertThat(rule.getRuleId()).isEqualTo("anti-pattern");
    }

    @Test
    void shouldDetectJpaAndMybatisConflict() {
        ProjectConfiguration config = configWith(List.of(
            new ProjectConfiguration.Dependency("org.springframework.boot", "spring-boot-starter-data-jpa", null, null),
            new ProjectConfiguration.Dependency("org.mybatis.spring.boot", "mybatis-spring-boot-starter", null, null)
        ));

        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).anyMatch(r ->
            r.type() == RecommendationType.ANTI_PATTERN_WARNING &&
            r.title().contains("Incompatible"));
    }

    @Test
    void shouldDetectReactiveAndBlockingConflict() {
        ProjectConfiguration config = configWith(List.of(
            new ProjectConfiguration.Dependency("org.springframework.boot", "spring-boot-starter-web", null, null),
            new ProjectConfiguration.Dependency("org.springframework.boot", "spring-boot-starter-webflux", null, null)
        ));

        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).anyMatch(r -> r.type() == RecommendationType.ANTI_PATTERN_WARNING);
    }

    @Test
    void shouldSuggestValidationForWebWithoutIt() {
        ProjectConfiguration config = configWith(List.of(
            new ProjectConfiguration.Dependency("org.springframework.boot", "spring-boot-starter-web", null, null)
        ));

        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).anyMatch(r ->
            r.type() == RecommendationType.BEST_PRACTICE &&
            r.title().contains("validation"));
    }

    @Test
    void shouldWarnTestingEnabledWithoutTestDeps() {
        ProjectConfiguration config = new ProjectConfiguration(
            new ProjectConfiguration.Metadata("com.example", "demo", "demo", "", "com.example.demo", "21", "3.3.5", "MAVEN"),
            new ProjectConfiguration.Architecture("LAYERED", List.of()),
            List.of(new ProjectConfiguration.Dependency("org.springframework.boot", "spring-boot-starter-web", null, null)),
            null, null,
            new ProjectConfiguration.TestingConfig(true, true, false, false),
            null, null, null, null
        );

        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).anyMatch(r -> r.title().contains("Testing enabled without test dependencies"));
    }

    @Test
    void shouldReturnEmptyForCompatibleDeps() {
        ProjectConfiguration config = configWith(List.of(
            new ProjectConfiguration.Dependency("org.springframework.boot", "spring-boot-starter-web", null, null),
            new ProjectConfiguration.Dependency("org.springframework.boot", "spring-boot-starter-validation", null, null)
        ));

        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).noneMatch(r -> r.type() == RecommendationType.ANTI_PATTERN_WARNING);
    }

    private ProjectConfiguration configWith(List<ProjectConfiguration.Dependency> deps) {
        return new ProjectConfiguration(
            new ProjectConfiguration.Metadata("com.example", "demo", "demo", "", "com.example.demo", "21", "3.3.5", "MAVEN"),
            new ProjectConfiguration.Architecture("LAYERED", List.of()),
            deps, null, null, null, null, null, null, null
        );
    }
}
