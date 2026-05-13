package com.springforge.recommendation;

import com.springforge.generator.domain.ProjectConfiguration;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public List<Recommendation> getRecommendations(@RequestBody ProjectConfiguration config) {
        return recommendationService.getRecommendations(config);
    }

    @PostMapping("/score")
    public CompatibilityScore getCompatibilityScore(@RequestBody ProjectConfiguration config) {
        return recommendationService.getCompatibilityScore(config);
    }
}
