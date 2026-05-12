import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from './admin.service';
import { DashboardStats } from './admin.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="admin-dashboard" *ngIf="stats">
      <h2>Admin Dashboard</h2>

      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-value">{{ stats.totalProjects }}</div>
          <div class="stat-label">Total Projects</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.totalUsers }}</div>
          <div class="stat-label">Total Users</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.activeUsers }}</div>
          <div class="stat-label">Active Users</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.totalBlueprints }}</div>
          <div class="stat-label">Blueprints</div>
        </div>
      </div>

      <div class="period-stats">
        <div class="period-card">
          <span class="period-value">{{ stats.projectsToday }}</span>
          <span class="period-label">Today</span>
        </div>
        <div class="period-card">
          <span class="period-value">{{ stats.projectsThisWeek }}</span>
          <span class="period-label">This Week</span>
        </div>
        <div class="period-card">
          <span class="period-value">{{ stats.projectsThisMonth }}</span>
          <span class="period-label">This Month</span>
        </div>
      </div>

      <div class="charts-row">
        <div class="chart-section">
          <h4>By Architecture</h4>
          <div *ngFor="let entry of archEntries" class="bar-item">
            <span class="bar-label">{{ entry[0] }}</span>
            <div class="bar-track"><div class="bar-fill" [style.width.%]="getPercent(entry[1], stats.totalProjects)"></div></div>
            <span class="bar-value">{{ entry[1] }}</span>
          </div>
        </div>

        <div class="chart-section">
          <h4>By Java Version</h4>
          <div *ngFor="let entry of javaEntries" class="bar-item">
            <span class="bar-label">Java {{ entry[0] }}</span>
            <div class="bar-track"><div class="bar-fill" [style.width.%]="getPercent(entry[1], stats.totalProjects)"></div></div>
            <span class="bar-value">{{ entry[1] }}</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .admin-dashboard { padding: 2rem; }
    .stats-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; margin-bottom: 2rem; }
    .stat-card { background: white; border: 1px solid #e0e0e0; border-radius: 8px; padding: 1.5rem; text-align: center; }
    .stat-value { font-size: 2rem; font-weight: bold; color: #1976d2; }
    .stat-label { color: #757575; margin-top: 0.5rem; }
    .period-stats { display: flex; gap: 1rem; margin-bottom: 2rem; }
    .period-card { background: #f5f5f5; padding: 1rem 2rem; border-radius: 8px; text-align: center; }
    .period-value { display: block; font-size: 1.5rem; font-weight: bold; }
    .period-label { color: #757575; }
    .charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 2rem; }
    .chart-section { background: white; border: 1px solid #e0e0e0; border-radius: 8px; padding: 1.5rem; }
    .bar-item { display: flex; align-items: center; gap: 0.5rem; margin: 0.5rem 0; }
    .bar-label { min-width: 120px; font-size: 0.85rem; }
    .bar-track { flex: 1; height: 20px; background: #e0e0e0; border-radius: 4px; overflow: hidden; }
    .bar-fill { height: 100%; background: #4CAF50; border-radius: 4px; transition: width 0.3s; }
    .bar-value { min-width: 30px; text-align: right; font-size: 0.85rem; font-weight: bold; }
  `]
})
export class AdminDashboardComponent implements OnInit {
  stats: DashboardStats | null = null;
  archEntries: [string, number][] = [];
  javaEntries: [string, number][] = [];

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.getDashboard().subscribe(stats => {
      this.stats = stats;
      this.archEntries = Object.entries(stats.projectsByArchitecture);
      this.javaEntries = Object.entries(stats.projectsByJavaVersion);
    });
  }

  getPercent(value: number, total: number): number {
    return total > 0 ? (value / total) * 100 : 0;
  }
}
