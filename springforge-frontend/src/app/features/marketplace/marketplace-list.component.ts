import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MarketplaceService } from './marketplace.service';
import { Blueprint, BlueprintCategory } from './blueprint.model';

@Component({
  selector: 'app-marketplace-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="marketplace">
      <h2>Blueprint Marketplace</h2>

      <div class="search-bar">
        <input type="text" [(ngModel)]="searchQuery" placeholder="Search blueprints..." (keyup.enter)="search()">
        <select [(ngModel)]="selectedCategory" (change)="search()">
          <option value="">All Categories</option>
          <option *ngFor="let cat of categories" [value]="cat">{{ cat }}</option>
        </select>
        <select [(ngModel)]="sortBy" (change)="search()">
          <option value="downloads">Most Downloaded</option>
          <option value="rating">Top Rated</option>
          <option value="createdAt">Newest</option>
          <option value="name">Name</option>
        </select>
        <button (click)="search()">Search</button>
      </div>

      <div class="blueprint-grid">
        <div *ngFor="let bp of blueprints" class="blueprint-card" (click)="selectBlueprint(bp)">
          <div class="card-header">
            <span class="category-badge">{{ bp.category }}</span>
            <span class="version">v{{ bp.version }}</span>
          </div>
          <h3>{{ bp.name }}</h3>
          <p>{{ bp.description }}</p>
          <div class="card-footer">
            <span class="author">by {{ bp.author }}</span>
            <div class="stats">
              <span class="downloads">{{ bp.downloads }} downloads</span>
              <span class="rating">{{ bp.rating | number:'1.1-1' }} ({{ bp.ratingCount }})</span>
            </div>
          </div>
          <div class="tags">
            <span *ngFor="let tag of bp.tags" class="tag">{{ tag }}</span>
          </div>
        </div>
      </div>

      <div class="pagination" *ngIf="totalPages > 1">
        <button [disabled]="currentPage === 0" (click)="goToPage(currentPage - 1)">Previous</button>
        <span>Page {{ currentPage + 1 }} of {{ totalPages }}</span>
        <button [disabled]="currentPage >= totalPages - 1" (click)="goToPage(currentPage + 1)">Next</button>
      </div>
    </div>
  `,
  styles: [`
    .marketplace { padding: 2rem; }
    .search-bar { display: flex; gap: 0.5rem; margin-bottom: 1.5rem; flex-wrap: wrap; }
    .search-bar input { flex: 1; min-width: 200px; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; }
    .search-bar select { padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; }
    .search-bar button { padding: 0.5rem 1rem; background: #4CAF50; color: white; border: none; border-radius: 4px; cursor: pointer; }
    .blueprint-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 1rem; }
    .blueprint-card { border: 1px solid #e0e0e0; border-radius: 8px; padding: 1rem; cursor: pointer; transition: box-shadow 0.2s; }
    .blueprint-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
    .card-header { display: flex; justify-content: space-between; margin-bottom: 0.5rem; }
    .category-badge { background: #e3f2fd; color: #1565c0; padding: 2px 8px; border-radius: 4px; font-size: 0.75rem; }
    .version { color: #757575; font-size: 0.85rem; }
    .card-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 0.75rem; font-size: 0.85rem; }
    .author { color: #616161; }
    .stats { display: flex; gap: 0.75rem; }
    .downloads, .rating { color: #757575; }
    .tags { margin-top: 0.5rem; display: flex; flex-wrap: wrap; gap: 4px; }
    .tag { background: #f5f5f5; padding: 2px 6px; border-radius: 3px; font-size: 0.75rem; }
    .pagination { display: flex; justify-content: center; align-items: center; gap: 1rem; margin-top: 2rem; }
    .pagination button { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 4px; cursor: pointer; }
    .pagination button:disabled { opacity: 0.5; cursor: not-allowed; }
  `]
})
export class MarketplaceListComponent implements OnInit {
  blueprints: Blueprint[] = [];
  searchQuery = '';
  selectedCategory = '';
  sortBy = 'downloads';
  currentPage = 0;
  totalPages = 0;
  categories: BlueprintCategory[] = ['MICROSERVICE', 'REST_API', 'BATCH', 'MESSAGING', 'FULLSTACK', 'LIBRARY', 'GATEWAY', 'EVENT_DRIVEN'];

  constructor(private marketplaceService: MarketplaceService) {}

  ngOnInit(): void {
    this.search();
  }

  search(): void {
    this.marketplaceService.search({
      query: this.searchQuery || undefined,
      category: (this.selectedCategory as BlueprintCategory) || undefined,
      sortBy: this.sortBy
    }, this.currentPage).subscribe(page => {
      this.blueprints = page.content;
      this.totalPages = page.totalPages;
    });
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.search();
  }

  selectBlueprint(bp: Blueprint): void {
    // Navigate to detail view
  }
}
