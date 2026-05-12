package com.springforge.recommendation;

import java.util.List;

public record Recommendation(
    String id,
    RecommendationType type,
    String title,
    String description,
    String category,
    int priority,
    double confidence,
    List<String> suggestedActions
) {}
