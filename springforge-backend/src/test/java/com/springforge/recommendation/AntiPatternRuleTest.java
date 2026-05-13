package com.springforge.recommendation;

import com.springforge.generator.domain.BuildTool;
import com.springforge.generator.domain.ProjectConfiguration;
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
            "spring-boot-starter-data-jpa",
            "mybatis-spring-boot-starter"
        ));

        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).anyMatch(r ->
            r.type() == RecommendationType.ANTI_PATTERN_WARNING &&
            r.title().contains("Incompatible"));
    }

    @Test
    void shouldDetectReactiveAndBlockingConflict() {
        ProjectConfiguration config = configWith(List.of(
            "spring-boot-starter-web",
            "spring-boot-starter-webflux"
        ));

        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).anyMatch(r -> r.type() == RecommendationType.ANTI_PATTERN_WARNING);
    }

    @Test
    void shouldSuggestValidationForWebWithoutIt() {
        ProjectConfiguration config = configWith(List.of(
            "spring-boot-starter-web"
        ));

        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).anyMatch(r ->
            r.type() == RecommendationType.BEST_PRACTICE &&
            r.title().contains("validation"));
    }

    @Test
    void shouldWarnTestingEnabledWithoutTestDeps() {
        ProjectConfiguration config = new ProjectConfiguration(
            new ProjectConfiguration.Metadata("com.example", "demo", "demo", "", "com.example.demo", "21", "3.3.5", BuildTool.MAVEN),
            new ProjectConfiguration.Architecture("LAYERED", List.of(), false, false),
            List.of("spring-boot-starter-web"),
            null, null, null, null,
            new ProjectConfiguration.TestingConfig(true, true, false, false),
            null, null
        );

        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).anyMatch(r -> r.title().contains("Testing enabled without test dependencies"));
    }

    @Test
    void shouldReturnEmptyForCompatibleDeps() {
        ProjectConfiguration config = configWith(List.of(
            "spring-boot-starter-web",
            "spring-boot-starter-validation"
        ));

        List<Recommendation> recommendations = rule.evaluate(config);

        assertThat(recommendations).noneMatch(r -> r.type() == RecommendationType.ANTI_PATTERN_WARNING);
    }

    private ProjectConfiguration configWith(List<String> deps) {
        return new ProjectConfiguration(
            new ProjectConfiguration.Metadata("com.example", "demo", "demo", "", "com.example.demo", "21", "3.3.5", BuildTool.MAVEN),
            new ProjectConfiguration.Architecture("LAYERED", List.of(), false, false),
            deps, null, null, null, null, null, null, null
        );
    }
}
