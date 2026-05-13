package com.springforge.recommendation;

import com.springforge.generator.domain.ProjectConfiguration;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final List<RecommendationRule> rules;

    public RecommendationService(List<RecommendationRule> rules) {
        this.rules = rules;
    }

    public List<Recommendation> getRecommendations(ProjectConfiguration config) {
        return rules.stream()
            .flatMap(rule -> rule.evaluate(config).stream())
            .sorted(Comparator.comparingInt(Recommendation::priority).reversed())
            .collect(Collectors.toList());
    }

    public CompatibilityScore getCompatibilityScore(ProjectConfiguration config) {
        List<Recommendation> recommendations = getRecommendations(config);

        Map<String, Double> categoryScores = new HashMap<>();
        Map<String, List<Recommendation>> byCategory = recommendations.stream()
            .collect(Collectors.groupingBy(Recommendation::category));

        List<String> strengths = new ArrayList<>();
        List<String> improvements = new ArrayList<>();

        double totalDeductions = 0;

        for (var entry : byCategory.entrySet()) {
            double categoryDeduction = 0;
            for (Recommendation rec : entry.getValue()) {
                if (rec.type() == RecommendationType.ANTI_PATTERN_WARNING) {
                    categoryDeduction += 20 * rec.confidence();
                    improvements.add(rec.title());
                } else if (rec.type() == RecommendationType.SECURITY_ADVISORY) {
                    categoryDeduction += 15 * rec.confidence();
                    improvements.add(rec.title());
                } else if (rec.type() == RecommendationType.BEST_PRACTICE) {
                    categoryDeduction += 5 * rec.confidence();
                }
            }
            double score = Math.max(0, 100 - categoryDeduction);
            categoryScores.put(entry.getKey(), score);
            totalDeductions += categoryDeduction;
        }

        if (config.security() != null && config.security().enabled()) {
            strengths.add("Security configured");
        }
        if (config.observability() != null && config.observability().enabled()) {
            strengths.add("Observability enabled");
        }
        if (config.testing() != null && config.testing().enabled()) {
            strengths.add("Testing infrastructure configured");
        }
        if (config.infrastructure() != null && config.infrastructure().helm()) {
            strengths.add("Kubernetes-ready with Helm charts");
        }

        double overallScore = Math.max(0, Math.min(100, 100 - (totalDeductions / Math.max(1, byCategory.size()))));

        return new CompatibilityScore(overallScore, categoryScores, strengths, improvements);
    }
}
