package com.springforge.recommendation.rules;

import com.springforge.generator.domain.ProjectConfiguration;
import com.springforge.recommendation.Recommendation;
import com.springforge.recommendation.RecommendationRule;
import com.springforge.recommendation.RecommendationType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DependencyRecommendationRule implements RecommendationRule {

    private static final Map<String, List<SuggestionEntry>> COMPLEMENTARY_DEPS = Map.of(
        "spring-boot-starter-data-jpa", List.of(
            new SuggestionEntry("org.flywaydb:flyway-core", "Flyway for database migrations", 4, 0.9),
            new SuggestionEntry("org.liquibase:liquibase-core", "Liquibase for database migrations", 3, 0.7)
        ),
        "spring-boot-starter-web", List.of(
            new SuggestionEntry("org.springdoc:springdoc-openapi-starter-webmvc-ui", "OpenAPI/Swagger documentation", 4, 0.85),
            new SuggestionEntry("org.springframework.boot:spring-boot-starter-validation", "Bean validation for request DTOs", 3, 0.8)
        ),
        "spring-kafka", List.of(
            new SuggestionEntry("io.confluent:kafka-avro-serializer", "Avro serialization with Schema Registry", 3, 0.75),
            new SuggestionEntry("org.springframework.kafka:spring-kafka-test", "Kafka testing support", 3, 0.8)
        ),
        "spring-boot-starter-security", List.of(
            new SuggestionEntry("org.springframework.session:spring-session-data-redis", "Distributed sessions with Redis", 3, 0.7),
            new SuggestionEntry("org.springframework.security:spring-security-test", "Security test utilities", 4, 0.9)
        ),
        "spring-boot-starter-actuator", List.of(
            new SuggestionEntry("io.micrometer:micrometer-registry-prometheus", "Prometheus metrics export", 4, 0.85),
            new SuggestionEntry("io.micrometer:micrometer-tracing-bridge-otel", "OpenTelemetry tracing", 3, 0.75)
        )
    );

    @Override
    public List<Recommendation> evaluate(ProjectConfiguration config) {
        List<Recommendation> recommendations = new ArrayList<>();

        if (config.dependencies() == null) return recommendations;

        Set<String> existingArtifacts = config.dependencies().stream()
            .map(d -> d)
            .collect(Collectors.toSet());

        for (var dep : config.dependencies()) {
            String key = dep;
            if (COMPLEMENTARY_DEPS.containsKey(key)) {
                for (SuggestionEntry suggestion : COMPLEMENTARY_DEPS.get(key)) {
                    String suggestedArtifact = suggestion.coordinate().split(":")[1];
                    if (!existingArtifacts.contains(suggestedArtifact)) {
                        recommendations.add(new Recommendation(
                            "dep-" + suggestedArtifact,
                            RecommendationType.DEPENDENCY_SUGGESTION,
                            "Add " + suggestedArtifact,
                            suggestion.reason() + " (complements " + key + ")",
                            "dependencies",
                            suggestion.priority(),
                            suggestion.confidence(),
                            List.of("Add dependency: " + suggestion.coordinate())
                        ));
                    }
                }
            }
        }

        return recommendations;
    }

    @Override
    public String getRuleId() {
        return "dependency-complement";
    }

    private record SuggestionEntry(String coordinate, String reason, int priority, double confidence) {}
}
