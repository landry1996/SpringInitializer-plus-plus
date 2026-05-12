package com.springforge.recommendation.rules;

import com.springforge.core.model.ProjectConfiguration;
import com.springforge.recommendation.Recommendation;
import com.springforge.recommendation.RecommendationRule;
import com.springforge.recommendation.RecommendationType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ArchitectureRecommendationRule implements RecommendationRule {

    private static final Map<String, List<PatternSuggestion>> ARCHITECTURE_PATTERNS = Map.of(
        "HEXAGONAL", List.of(
            new PatternSuggestion("MapStruct for port/adapter mapping", "Use MapStruct to map between domain and infrastructure layers", 4, 0.9,
                List.of("Add org.mapstruct:mapstruct dependency", "Create mapper interfaces in adapter layer")),
            new PatternSuggestion("ArchUnit for architecture enforcement", "Enforce hexagonal boundaries with ArchUnit tests", 4, 0.85,
                List.of("Add com.tngtech.archunit:archunit-junit5", "Create architecture test class"))
        ),
        "DDD", List.of(
            new PatternSuggestion("Domain Events pattern", "Use Spring Application Events for aggregate communication", 5, 0.9,
                List.of("Create domain event base class", "Use ApplicationEventPublisher in aggregates")),
            new PatternSuggestion("Aggregate Root pattern", "Enforce invariants through aggregate roots", 4, 0.85,
                List.of("Create AbstractAggregateRoot base class", "Route all mutations through aggregate"))
        ),
        "CQRS", List.of(
            new PatternSuggestion("Event Store", "Persist domain events for event sourcing", 4, 0.8,
                List.of("Create EventStore interface", "Implement JPA-based event store")),
            new PatternSuggestion("Read/Write model separation", "Separate read projections from write models", 5, 0.9,
                List.of("Create separate read model DTOs", "Implement projection handlers"))
        ),
        "MICROSERVICES", List.of(
            new PatternSuggestion("Service Discovery", "Register services with Eureka or Consul", 5, 0.9,
                List.of("Add spring-cloud-starter-netflix-eureka-client", "Configure service registration")),
            new PatternSuggestion("Circuit Breaker", "Add resilience with Resilience4j", 4, 0.85,
                List.of("Add spring-cloud-starter-circuitbreaker-resilience4j", "Annotate remote calls with @CircuitBreaker")),
            new PatternSuggestion("API Gateway", "Centralize routing with Spring Cloud Gateway", 4, 0.8,
                List.of("Add spring-cloud-starter-gateway module", "Configure route predicates"))
        ),
        "MODULITH", List.of(
            new PatternSuggestion("Spring Modulith Events", "Use application events for module communication", 5, 0.9,
                List.of("Add spring-modulith-events dependency", "Use @ApplicationModuleListener")),
            new PatternSuggestion("Module integration testing", "Verify module interactions with @ApplicationModuleTest", 4, 0.85,
                List.of("Add spring-modulith-starter-test", "Create @ApplicationModuleTest classes"))
        ),
        "EVENT_DRIVEN", List.of(
            new PatternSuggestion("Outbox Pattern", "Ensure reliable event publishing with transactional outbox", 5, 0.9,
                List.of("Create outbox table and entity", "Implement outbox event processor")),
            new PatternSuggestion("Dead Letter Queue", "Handle failed messages gracefully", 4, 0.85,
                List.of("Configure DLQ topic", "Implement DLQ consumer for retry"))
        )
    );

    @Override
    public List<Recommendation> evaluate(ProjectConfiguration config) {
        List<Recommendation> recommendations = new ArrayList<>();

        if (config.architecture() == null) return recommendations;

        String archType = config.architecture().type();
        if (ARCHITECTURE_PATTERNS.containsKey(archType)) {
            for (PatternSuggestion pattern : ARCHITECTURE_PATTERNS.get(archType)) {
                recommendations.add(new Recommendation(
                    "arch-" + archType.toLowerCase() + "-" + pattern.title().hashCode(),
                    RecommendationType.PATTERN_SUGGESTION,
                    pattern.title(),
                    pattern.description(),
                    "architecture",
                    pattern.priority(),
                    pattern.confidence(),
                    pattern.actions()
                ));
            }
        }

        return recommendations;
    }

    @Override
    public String getRuleId() {
        return "architecture-pattern";
    }

    private record PatternSuggestion(String title, String description, int priority, double confidence, List<String> actions) {}
}
