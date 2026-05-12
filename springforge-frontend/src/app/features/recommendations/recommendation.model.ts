export interface Recommendation {
  id: string;
  type: RecommendationType;
  title: string;
  description: string;
  category: string;
  priority: number;
  confidence: number;
  suggestedActions: string[];
}

export type RecommendationType =
  | 'DEPENDENCY_SUGGESTION'
  | 'PATTERN_SUGGESTION'
  | 'ANTI_PATTERN_WARNING'
  | 'BEST_PRACTICE'
  | 'PERFORMANCE_TIP'
  | 'SECURITY_ADVISORY';

export interface CompatibilityScore {
  overallScore: number;
  categoryScores: Record<string, number>;
  strengths: string[];
  improvements: string[];
}
