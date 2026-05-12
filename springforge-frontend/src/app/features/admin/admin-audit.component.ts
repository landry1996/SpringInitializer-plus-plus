import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from './admin.service';
import { AuditLog } from './admin.model';

@Component({
  selector: 'app-admin-audit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="audit-logs">
      <h3>Audit Logs</h3>

      <div class="filters">
        <select [(ngModel)]="filterAction" (change)="loadLogs()">
          <option value="">All Actions</option>
          <option value="PROJECT_GENERATED">Project Generated</option>
          <option value="BLUEPRINT_CREATED">Blueprint Created</option>
          <option value="USER_LOGIN">User Login</option>
          <option value="CONFIG_CHANGED">Config Changed</option>
        </select>
        <input [(ngModel)]="filterUserId" placeholder="Filter by User ID" (keyup.enter)="loadLogs()">
        <button (click)="loadLogs()">Filter</button>
      </div>

      <table class="logs-table">
        <thead>
          <tr>
            <th>Timestamp</th>
            <th>Action</th>
            <th>User</th>
            <th>Details</th>
            <th>IP</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let log of logs">
            <td>{{ log.timestamp | date:'short' }}</td>
            <td><span class="action-badge">{{ log.action }}</span></td>
            <td>{{ log.userId }}</td>
            <td class="details-cell">{{ log.details }}</td>
            <td>{{ log.ipAddress }}</td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <button [disabled]="currentPage === 0" (click)="goToPage(currentPage - 1)">Previous</button>
        <span>Page {{ currentPage + 1 }}</span>
        <button (click)="goToPage(currentPage + 1)">Next</button>
      </div>
    </div>
  `,
  styles: [`
    .audit-logs { padding: 1rem; }
    .filters { display: flex; gap: 0.5rem; margin-bottom: 1rem; }
    .filters select, .filters input { padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; }
    .filters button { background: #1976d2; color: white; border: none; padding: 0.5rem 1rem; border-radius: 4px; cursor: pointer; }
    .logs-table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
    .logs-table th, .logs-table td { padding: 0.5rem; text-align: left; border-bottom: 1px solid #e0e0e0; }
    .logs-table th { background: #f5f5f5; }
    .action-badge { background: #e3f2fd; color: #1565c0; padding: 2px 6px; border-radius: 3px; font-size: 0.8rem; }
    .details-cell { max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .pagination { display: flex; justify-content: center; gap: 1rem; margin-top: 1rem; align-items: center; }
    .pagination button { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 4px; cursor: pointer; }
    .pagination button:disabled { opacity: 0.5; }
  `]
})
export class AdminAuditComponent implements OnInit {
  logs: AuditLog[] = [];
  filterAction = '';
  filterUserId = '';
  currentPage = 0;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadLogs();
  }

  loadLogs(): void {
    this.adminService.getAuditLogs(this.currentPage, 50, this.filterAction || undefined, this.filterUserId || undefined)
      .subscribe(page => this.logs = page.content);
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.loadLogs();
  }
}
