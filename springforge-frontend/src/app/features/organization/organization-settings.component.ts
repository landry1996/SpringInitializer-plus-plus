import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrganizationService } from './organization.service';
import { Organization, OrganizationMember, UsageReport } from './organization.model';

@Component({
  selector: 'app-organization-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="org-settings" *ngIf="org">
      <h2>{{ org.name }}</h2>
      <span class="plan-badge" [class]="org.plan.toLowerCase()">{{ org.plan }}</span>

      <div class="usage-section" *ngIf="usage">
        <h4>Usage</h4>
        <div class="usage-bars">
          <div class="usage-item">
            <label>Projects</label>
            <div class="bar-track">
              <div class="bar-fill" [style.width.%]="getPercent(usage.projectsUsed, usage.projectsLimit)"></div>
            </div>
            <span>{{ usage.projectsUsed }}/{{ usage.projectsLimit === -1 ? 'Unlimited' : usage.projectsLimit }}</span>
          </div>
          <div class="usage-item">
            <label>Users</label>
            <div class="bar-track">
              <div class="bar-fill" [style.width.%]="getPercent(usage.usersUsed, usage.usersLimit)"></div>
            </div>
            <span>{{ usage.usersUsed }}/{{ usage.usersLimit === -1 ? 'Unlimited' : usage.usersLimit }}</span>
          </div>
          <div class="usage-item">
            <label>Generations Today</label>
            <div class="bar-track">
              <div class="bar-fill" [style.width.%]="getPercent(usage.generationsToday, usage.generationsLimit)"></div>
            </div>
            <span>{{ usage.generationsToday }}/{{ usage.generationsLimit === -1 ? 'Unlimited' : usage.generationsLimit }}</span>
          </div>
        </div>
      </div>

      <div class="members-section">
        <h4>Members ({{ members.length }})</h4>
        <div class="member-list">
          <div *ngFor="let m of members" class="member-item">
            <span class="member-email">{{ m.email }}</span>
            <select [ngModel]="m.role" (ngModelChange)="changeRole(m.id, $event)">
              <option value="OWNER">Owner</option>
              <option value="ADMIN">Admin</option>
              <option value="MEMBER">Member</option>
              <option value="VIEWER">Viewer</option>
            </select>
            <button class="btn-remove" (click)="removeMember(m.id)">Remove</button>
          </div>
        </div>
        <div class="add-member">
          <input [(ngModel)]="newMemberEmail" placeholder="Email address">
          <button (click)="addMember()">Invite</button>
        </div>
      </div>

      <div class="upgrade-section" *ngIf="org.plan !== 'ENTERPRISE'">
        <h4>Upgrade Plan</h4>
        <div class="plan-options">
          <button *ngIf="org.plan === 'FREE'" (click)="upgrade('PRO')">Upgrade to PRO</button>
          <button (click)="upgrade('ENTERPRISE')">Upgrade to ENTERPRISE</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .org-settings { padding: 2rem; max-width: 800px; }
    .plan-badge { padding: 4px 12px; border-radius: 4px; font-weight: bold; font-size: 0.85rem; }
    .plan-badge.free { background: #e0e0e0; }
    .plan-badge.pro { background: #e3f2fd; color: #1565c0; }
    .plan-badge.enterprise { background: #fce4ec; color: #c62828; }
    .usage-section, .members-section, .upgrade-section { margin-top: 2rem; }
    .usage-bars { display: grid; gap: 1rem; }
    .usage-item { display: flex; align-items: center; gap: 1rem; }
    .usage-item label { min-width: 150px; }
    .bar-track { flex: 1; height: 12px; background: #e0e0e0; border-radius: 6px; overflow: hidden; }
    .bar-fill { height: 100%; background: #4CAF50; border-radius: 6px; }
    .member-list { margin: 1rem 0; }
    .member-item { display: flex; align-items: center; gap: 1rem; padding: 0.5rem 0; border-bottom: 1px solid #f0f0f0; }
    .member-email { flex: 1; }
    .btn-remove { background: #dc3545; color: white; border: none; padding: 4px 8px; border-radius: 4px; cursor: pointer; font-size: 0.8rem; }
    .add-member { display: flex; gap: 0.5rem; margin-top: 1rem; }
    .add-member input { flex: 1; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; }
    .add-member button { background: #4CAF50; color: white; border: none; padding: 0.5rem 1rem; border-radius: 4px; cursor: pointer; }
    .plan-options { display: flex; gap: 1rem; }
    .plan-options button { padding: 0.75rem 1.5rem; background: #1976d2; color: white; border: none; border-radius: 6px; cursor: pointer; }
    select { padding: 4px 8px; border: 1px solid #ddd; border-radius: 4px; }
  `]
})
export class OrganizationSettingsComponent implements OnInit {
  org: Organization | null = null;
  members: OrganizationMember[] = [];
  usage: UsageReport | null = null;
  newMemberEmail = '';
  orgId = '';

  constructor(private orgService: OrganizationService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    if (!this.orgId) return;
    this.orgService.getById(this.orgId).subscribe(o => this.org = o);
    this.orgService.getMembers(this.orgId).subscribe(m => this.members = m);
    this.orgService.getUsage(this.orgId).subscribe(u => this.usage = u);
  }

  addMember(): void {
    if (!this.newMemberEmail) return;
    this.orgService.addMember(this.orgId, { email: this.newMemberEmail, role: 'MEMBER' }).subscribe(() => {
      this.loadData();
      this.newMemberEmail = '';
    });
  }

  removeMember(memberId: string): void {
    this.orgService.removeMember(this.orgId, memberId).subscribe(() => this.loadData());
  }

  changeRole(memberId: string, role: string): void {
    this.orgService.changeMemberRole(this.orgId, memberId, role).subscribe();
  }

  upgrade(plan: string): void {
    this.orgService.upgrade(this.orgId, plan).subscribe(o => this.org = o);
  }

  getPercent(used: number, limit: number): number {
    if (limit <= 0) return 0;
    return Math.min(100, (used / limit) * 100);
  }
}
