package com.springforge.generator.application;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DependencyConflictDetector {

    private static final Map<String, Set<String>> CONFLICTS = Map.ofEntries(
        Map.entry("spring-boot-starter-data-jpa", Set.of("spring-boot-starter-data-jdbc")),
        Map.entry("spring-boot-starter-webflux", Set.of("spring-boot-starter-web")),
        Map.entry("spring-boot-starter-undertow", Set.of("spring-boot-starter-tomcat", "spring-boot-starter-jetty")),
        Map.entry("spring-boot-starter-jetty", Set.of("spring-boot-starter-tomcat", "spring-boot-starter-undertow")),
        Map.entry("spring-boot-starter-log4j2", Set.of("spring-boot-starter-logging")),
        Map.entry("spring-boot-starter-data-mongodb", Set.of("spring-boot-starter-data-jpa")),
        Map.entry("spring-boot-starter-data-r2dbc", Set.of("spring-boot-starter-data-jpa", "spring-boot-starter-data-jdbc"))
    );

    private static final Map<String, Set<String>> REQUIRES = Map.of(
        "spring-boot-starter-data-jpa", Set.of("spring-boot-starter-jdbc"),
        "spring-boot-starter-security", Set.of(),
        "spring-boot-starter-oauth2-resource-server", Set.of("spring-boot-starter-security"),
        "spring-boot-starter-oauth2-client", Set.of("spring-boot-starter-security")
    );

    public List<String> detectConflicts(List<String> dependencies) {
        List<String> issues = new ArrayList<>();

        for (String dep : dependencies) {
            Set<String> conflicts = CONFLICTS.get(dep);
            if (conflicts != null) {
                for (String conflict : conflicts) {
                    if (dependencies.contains(conflict)) {
                        issues.add("Conflict: '" + dep + "' is incompatible with '" + conflict + "'");
                    }
                }
            }
        }

        for (Map.Entry<String, Set<String>> entry : REQUIRES.entrySet()) {
            if (dependencies.contains(entry.getKey())) {
                for (String required : entry.getValue()) {
                    if (!dependencies.contains(required)) {
                        issues.add("Missing dependency: '" + entry.getKey() + "' requires '" + required + "'");
                    }
                }
            }
        }

        return issues;
    }

    public List<String> suggestAdditions(List<String> dependencies) {
        Set<String> suggestions = new LinkedHashSet<>();

        if (dependencies.contains("spring-boot-starter-data-jpa") &&
            !dependencies.contains("postgresql") && !dependencies.contains("mysql-connector-j") && !dependencies.contains("h2")) {
            suggestions.add("Consider adding a database driver (postgresql, mysql-connector-j, or h2)");
        }

        if (dependencies.contains("spring-boot-starter-data-mongodb") &&
            !dependencies.contains("mongock-springboot-v3")) {
            suggestions.add("Consider adding 'mongock-springboot-v3' for MongoDB schema migrations");
        }

        if (dependencies.contains("spring-boot-starter-web") &&
            !dependencies.contains("spring-boot-starter-validation")) {
            suggestions.add("Consider adding 'spring-boot-starter-validation' for request validation");
        }

        if (dependencies.contains("spring-boot-starter-actuator") &&
            !dependencies.contains("micrometer-registry-prometheus")) {
            suggestions.add("Consider adding 'micrometer-registry-prometheus' for metrics export");
        }

        return new ArrayList<>(suggestions);
    }
}
