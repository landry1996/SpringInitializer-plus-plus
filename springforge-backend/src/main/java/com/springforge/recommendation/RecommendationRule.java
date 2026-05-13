package com.springforge.recommendation;

import com.springforge.generator.domain.ProjectConfiguration;

import java.util.List;

public interface RecommendationRule {
    List<Recommendation> evaluate(ProjectConfiguration config);
    String getRuleId();
}
