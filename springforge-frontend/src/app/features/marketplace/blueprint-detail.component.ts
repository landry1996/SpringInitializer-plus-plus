import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MarketplaceService } from './marketplace.service';
import { Blueprint } from './blueprint.model';

@Component({
  selector: 'app-blueprint-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="detail-panel" *ngIf="blueprint">
      <div class="detail-header">
        <h2>{{ blueprint.name }}</h2>
        <span class="version">v{{ blueprint.version }}</span>
        <span class="category">{{ blueprint.category }}</span>
      </div>

      <p class="description">{{ blueprint.description }}</p>

      <div class="meta">
        <span>By {{ blueprint.author }}</span>
        <span>{{ blueprint.downloads }} downloads</span>
        <span>Rating: {{ blueprint.rating | number:'1.1-1' }}/5 ({{ blueprint.ratingCount }} votes)</span>
      </div>

      <div class="tags">
        <span *ngFor="let tag of blueprint.tags" class="tag">{{ tag }}</span>
      </div>

      <div class="actions">
        <button class="btn-primary" (click)="downloadBlueprint()">Use this Blueprint</button>
      </div>

      <div class="rating-section">
        <h4>Rate this blueprint</h4>
        <div class="stars">
          <button *ngFor="let star of [1,2,3,4,5]"
                  [class.active]="star <= userRating"
                  (click)="userRating = star">
            &#9733;
          </button>
        </div>
        <textarea [(ngModel)]="userComment" placeholder="Leave a comment (optional)"></textarea>
        <button (click)="submitRating()">Submit Rating</button>
      </div>
    </div>
  `,
  styles: [`
    .detail-panel { padding: 2rem; max-width: 800px; }
    .detail-header { display: flex; align-items: center; gap: 1rem; margin-bottom: 1rem; }
    .detail-header h2 { margin: 0; }
    .version { background: #e8f5e9; color: #2e7d32; padding: 2px 8px; border-radius: 4px; }
    .category { background: #e3f2fd; color: #1565c0; padding: 2px 8px; border-radius: 4px; }
    .description { color: #424242; line-height: 1.6; }
    .meta { display: flex; gap: 1.5rem; color: #757575; margin: 1rem 0; font-size: 0.9rem; }
    .tags { display: flex; flex-wrap: wrap; gap: 0.5rem; margin: 1rem 0; }
    .tag { background: #f5f5f5; padding: 4px 10px; border-radius: 4px; font-size: 0.85rem; }
    .actions { margin: 1.5rem 0; }
    .btn-primary { background: #4CAF50; color: white; border: none; padding: 0.75rem 1.5rem; border-radius: 6px; cursor: pointer; font-size: 1rem; }
    .rating-section { margin-top: 2rem; padding-top: 1.5rem; border-top: 1px solid #e0e0e0; }
    .stars button { background: none; border: none; font-size: 1.5rem; cursor: pointer; color: #ddd; }
    .stars button.active { color: #ffc107; }
    textarea { width: 100%; margin: 0.5rem 0; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; min-height: 80px; }
    .rating-section button:last-child { margin-top: 0.5rem; padding: 0.5rem 1rem; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; }
  `]
})
export class BlueprintDetailComponent implements OnInit {
  @Input() blueprintId!: string;
  blueprint: Blueprint | null = null;
  userRating = 0;
  userComment = '';

  constructor(private marketplaceService: MarketplaceService) {}

  ngOnInit(): void {
    if (this.blueprintId) {
      this.marketplaceService.getById(this.blueprintId).subscribe(bp => this.blueprint = bp);
    }
  }

  downloadBlueprint(): void {
    if (this.blueprint) {
      this.marketplaceService.download(this.blueprint.id).subscribe(bp => {
        this.blueprint = bp;
      });
    }
  }

  submitRating(): void {
    if (this.blueprint && this.userRating > 0) {
      this.marketplaceService.rate(this.blueprint.id, this.userRating, this.userComment).subscribe(bp => {
        this.blueprint = bp;
        this.userRating = 0;
        this.userComment = '';
      });
    }
  }
}
