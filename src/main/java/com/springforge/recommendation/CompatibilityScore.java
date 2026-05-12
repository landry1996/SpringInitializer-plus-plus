package com.springforge.recommendation;

import java.util.List;
import java.util.Map;

public record CompatibilityScore(
    double overallScore,
    Map<String, Double> categoryScores,
    List<String> strengths,
    List<String> improvements
) {}
