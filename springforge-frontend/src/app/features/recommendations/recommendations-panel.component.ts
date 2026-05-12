import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RecommendationService } from './recommendation.service';
import { Recommendation, CompatibilityScore } from './recommendation.model';

@Component({
  selector: 'app-recommendations-panel',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="recommendations-panel" *ngIf="recommendations.length > 0 || score">
      <h3>Recommendations</h3>

      <div class="score-section" *ngIf="score">
        <div class="score-badge" [class.good]="score.overallScore >= 80"
             [class.warning]="score.overallScore >= 50 && score.overallScore < 80"
             [class.danger]="score.overallScore < 50">
          {{ score.overallScore | number:'1.0-0' }}/100
        </div>
        <div class="score-details">
          <div class="strengths" *ngIf="score.strengths.length">
            <strong>Strengths:</strong>
            <span *ngFor="let s of score.strengths" class="badge badge-success">{{ s }}</span>
          </div>
          <div class="improvements" *ngIf="score.improvements.length">
            <strong>To improve:</strong>
            <span *ngFor="let i of score.improvements" class="badge badge-warning">{{ i }}</span>
          </div>
        </div>
      </div>

      <div class="recommendation-cards">
        <div *ngFor="let rec of recommendations" class="rec-card" [class]="'priority-' + rec.priority">
          <div class="rec-header">
            <span class="rec-type" [class]="rec.type.toLowerCase()">{{ getTypeLabel(rec.type) }}</span>
            <span class="rec-priority">P{{ rec.priority }}</span>
          </div>
          <h4>{{ rec.title }}</h4>
          <p>{{ rec.description }}</p>
          <div class="rec-actions">
            <span *ngFor="let action of rec.suggestedActions" class="action-item">{{ action }}</span>
          </div>
          <div class="confidence">Confidence: {{ rec.confidence * 100 | number:'1.0-0' }}%</div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .recommendations-panel { padding: 1rem; border: 1px solid #e0e0e0; border-radius: 8px; margin: 1rem 0; }
    .score-section { display: flex; align-items: center; gap: 1rem; margin-bottom: 1rem; padding: 1rem; background: #f8f9fa; border-radius: 8px; }
    .score-badge { font-size: 2rem; font-weight: bold; padding: 0.5rem 1rem; border-radius: 50%; }
    .score-badge.good { color: #28a745; }
    .score-badge.warning { color: #ffc107; }
    .score-badge.danger { color: #dc3545; }
    .badge { display: inline-block; padding: 2px 8px; margin: 2px; border-radius: 4px; font-size: 0.85rem; }
    .badge-success { background: #d4edda; color: #155724; }
    .badge-warning { background: #fff3cd; color: #856404; }
    .recommendation-cards { display: grid; gap: 0.75rem; }
    .rec-card { padding: 1rem; border: 1px solid #dee2e6; border-radius: 6px; border-left: 4px solid #6c757d; }
    .rec-card.priority-5 { border-left-color: #dc3545; }
    .rec-card.priority-4 { border-left-color: #fd7e14; }
    .rec-card.priority-3 { border-left-color: #ffc107; }
    .rec-header { display: flex; justify-content: space-between; margin-bottom: 0.5rem; }
    .rec-type { font-size: 0.75rem; text-transform: uppercase; padding: 2px 6px; border-radius: 3px; background: #e9ecef; }
    .rec-type.anti_pattern_warning { background: #f8d7da; color: #721c24; }
    .rec-type.security_advisory { background: #fff3cd; color: #856404; }
    .rec-type.dependency_suggestion { background: #d1ecf1; color: #0c5460; }
    .rec-type.pattern_suggestion { background: #d4edda; color: #155724; }
    .rec-priority { font-weight: bold; font-size: 0.85rem; }
    .rec-actions { margin-top: 0.5rem; }
    .action-item { display: block; font-size: 0.85rem; color: #495057; padding: 2px 0; }
    .action-item::before { content: "→ "; color: #6c757d; }
    .confidence { font-size: 0.75rem; color: #6c757d; margin-top: 0.5rem; }
  `]
})
export class RecommendationsPanelComponent implements OnChanges {
  @Input() config: any;
  recommendations: Recommendation[] = [];
  score: CompatibilityScore | null = null;

  constructor(private recommendationService: RecommendationService) {}

  ngOnChanges(): void {
    if (this.config) {
      this.loadRecommendations();
    }
  }

  loadRecommendations(): void {
    this.recommendationService.getRecommendations(this.config).subscribe(recs => this.recommendations = recs);
    this.recommendationService.getCompatibilityScore(this.config).subscribe(s => this.score = s);
  }

  getTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      'DEPENDENCY_SUGGESTION': 'Dependency',
      'PATTERN_SUGGESTION': 'Pattern',
      'ANTI_PATTERN_WARNING': 'Warning',
      'BEST_PRACTICE': 'Best Practice',
      'PERFORMANCE_TIP': 'Performance',
      'SECURITY_ADVISORY': 'Security'
    };
    return labels[type] || type;
  }
}
