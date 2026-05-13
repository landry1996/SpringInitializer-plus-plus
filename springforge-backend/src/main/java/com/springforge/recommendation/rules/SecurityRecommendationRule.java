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
public class SecurityRecommendationRule implements RecommendationRule {

    @Override
    public List<Recommendation> evaluate(ProjectConfiguration config) {
        List<Recommendation> recommendations = new ArrayList<>();

        boolean hasWebDep = config.dependencies() != null && config.dependencies().stream()
            .anyMatch(d -> d.artifactId().contains("web") || d.artifactId().contains("webflux"));

        if (hasWebDep && (config.security() == null || !config.security().enabled())) {
            recommendations.add(new Recommendation(
                "sec-no-security",
                RecommendationType.SECURITY_ADVISORY,
                "No security configured for web application",
                "Web applications should have at minimum basic authentication or OAuth2 configured",
                "security",
                5,
                0.9,
                List.of("Enable security in configuration", "Add spring-boot-starter-security", "Configure OAuth2 or basic auth")
            ));
        }

        if (config.security() != null && config.security().enabled()) {
            Set<String> artifacts = config.dependencies() != null
                ? config.dependencies().stream().map(d -> d.artifactId()).collect(Collectors.toSet())
                : Set.of();

            if (!artifacts.contains("spring-boot-starter-oauth2-resource-server") &&
                "OAUTH2".equals(config.security().type())) {
                recommendations.add(new Recommendation(
                    "sec-missing-resource-server",
                    RecommendationType.SECURITY_ADVISORY,
                    "OAuth2 configured without resource server",
                    "Add spring-boot-starter-oauth2-resource-server for proper JWT validation",
                    "security",
                    4,
                    0.85,
                    List.of("Add spring-boot-starter-oauth2-resource-server dependency")
                ));
            }

            if (!artifacts.contains("spring-security-test")) {
                recommendations.add(new Recommendation(
                    "sec-no-security-test",
                    RecommendationType.BEST_PRACTICE,
                    "Missing security test utilities",
                    "Add spring-security-test for testing secured endpoints",
                    "testing",
                    3,
                    0.8,
                    List.of("Add org.springframework.security:spring-security-test")
                ));
            }
        }

        if (hasWebDep) {
            Set<String> artifacts = config.dependencies() != null
                ? config.dependencies().stream().map(d -> d.artifactId()).collect(Collectors.toSet())
                : Set.of();

            if (!artifacts.contains("bucket4j-spring-boot-starter")) {
                recommendations.add(new Recommendation(
                    "sec-no-rate-limit",
                    RecommendationType.SECURITY_ADVISORY,
                    "No rate limiting configured",
                    "APIs exposed to the internet should implement rate limiting to prevent abuse",
                    "security",
                    3,
                    0.7,
                    List.of("Add bucket4j-spring-boot-starter for rate limiting", "Configure rate limits per endpoint")
                ));
            }
        }

        return recommendations;
    }

    @Override
    public String getRuleId() {
        return "security-recommendation";
    }
}
