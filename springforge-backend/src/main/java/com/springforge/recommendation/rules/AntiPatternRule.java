package com.springforge.recommendation.rules;

import com.springforge.generator.domain.ProjectConfiguration;
import com.springforge.recommendation.Recommendation;
import com.springforge.recommendation.RecommendationRule;
import com.springforge.recommendation.RecommendationType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AntiPatternRule implements RecommendationRule {

    private static final Set<Set<String>> INCOMPATIBLE_GROUPS = Set.of(
        Set.of("spring-boot-starter-data-jpa", "mybatis-spring-boot-starter"),
        Set.of("spring-boot-starter-webflux", "spring-boot-starter-web"),
        Set.of("spring-boot-starter-data-r2dbc", "spring-boot-starter-data-jpa")
    );

    @Override
    public List<Recommendation> evaluate(ProjectConfiguration config) {
        List<Recommendation> recommendations = new ArrayList<>();

        if (config.dependencies() == null) return recommendations;

        Set<String> artifacts = config.dependencies().stream()
            .map(d -> d)
            .collect(Collectors.toSet());

        for (Set<String> incompatible : INCOMPATIBLE_GROUPS) {
            if (artifacts.containsAll(incompatible)) {
                String deps = String.join(" + ", incompatible);
                recommendations.add(new Recommendation(
                    "anti-incompatible-" + deps.hashCode(),
                    RecommendationType.ANTI_PATTERN_WARNING,
                    "Incompatible dependencies detected",
                    "Using " + deps + " together may cause conflicts and unexpected behavior",
                    "compatibility",
                    5,
                    0.95,
                    List.of("Remove one of: " + deps, "Choose either reactive or blocking stack")
                ));
            }
        }

        if (config.testing() != null && config.testing().enabled()) {
            boolean hasTestDep = artifacts.stream().anyMatch(a -> a.contains("test"));
            if (!hasTestDep) {
                recommendations.add(new Recommendation(
                    "anti-no-test-deps",
                    RecommendationType.ANTI_PATTERN_WARNING,
                    "Testing enabled without test dependencies",
                    "Testing configuration is enabled but no test framework dependency found",
                    "testing",
                    4,
                    0.8,
                    List.of("Add spring-boot-starter-test", "Add testcontainers if using integration tests")
                ));
            }
        }

        if (artifacts.contains("spring-boot-starter-web") && !artifacts.contains("spring-boot-starter-validation")) {
            recommendations.add(new Recommendation(
                "anti-no-validation",
                RecommendationType.BEST_PRACTICE,
                "Web API without input validation",
                "REST APIs should validate incoming requests to prevent invalid data",
                "quality",
                3,
                0.75,
                List.of("Add spring-boot-starter-validation", "Use @Valid on request bodies")
            ));
        }

        return recommendations;
    }

    @Override
    public String getRuleId() {
        return "anti-pattern";
    }
}
