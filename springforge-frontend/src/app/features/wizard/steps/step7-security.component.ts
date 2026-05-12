import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WizardStateService } from '../wizard-state.service';

@Component({
  selector: 'app-step7-security',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h2>Security Configuration</h2>
    <div class="cards">
      <div class="card" [class.selected]="secType === 'NONE'" (click)="selectType('NONE')">
        <h3>None</h3>
        <p>No security configuration</p>
      </div>
      <div class="card" [class.selected]="secType === 'JWT'" (click)="selectType('JWT')">
        <h3>JWT</h3>
        <p>Stateless JWT-based authentication</p>
      </div>
      <div class="card" [class.selected]="secType === 'OAUTH2'" (click)="selectType('OAUTH2')">
        <h3>OAuth2 / OIDC</h3>
        <p>OAuth2 with external identity provider</p>
      </div>
    </div>
    @if (secType !== 'NONE') {
      <div class="roles-section">
        <h3>Roles</h3>
        <div class="role-input">
          <input type="text" [(ngModel)]="newRole" placeholder="Role name" (keyup.enter)="addRole()">
          <button (click)="addRole()">Add Role</button>
        </div>
        <div class="role-chips">
          @for (role of roles; track role) {
            <span class="chip">{{ role }} <span class="remove" (click)="removeRole(role)">&times;</span></span>
          }
        </div>
      </div>
    }
  `,
  styles: [`
    h2 { margin-bottom: 1.5rem; }
    .cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1rem; margin-bottom: 2rem; }
    .card { padding: 1.5rem; border: 2px solid #e0e0e0; border-radius: 8px; cursor: pointer; text-align: center; }
    .card:hover { border-color: #90caf9; }
    .card.selected { border-color: #1976d2; background: #e3f2fd; }
    .card h3 { margin: 0 0 0.5rem; }
    .card p { margin: 0; color: #666; font-size: 0.85rem; }
    .roles-section h3 { margin-bottom: 0.75rem; }
    .role-input { display: flex; gap: 0.5rem; margin-bottom: 1rem; }
    .role-input input { flex: 1; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; }
    .role-input button { padding: 0.5rem 1rem; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; }
    .role-chips { display: flex; gap: 0.5rem; flex-wrap: wrap; }
    .chip { padding: 0.25rem 0.75rem; background: #e3f2fd; border-radius: 12px; font-size: 0.85rem; }
    .remove { cursor: pointer; color: #c62828; margin-left: 0.25rem; }
  `]
})
export class Step7SecurityComponent {
  newRole = '';
  constructor(public wizardState: WizardStateService) {}

  get secType(): string { return this.wizardState.state().security?.type || 'NONE'; }
  get roles(): string[] { return this.wizardState.state().security?.roles || []; }

  selectType(type: string): void {
    if (type === 'NONE') {
      this.wizardState.updateState({ security: null });
    } else {
      this.wizardState.updateState({ security: { type, roles: this.roles.length ? this.roles : ['ADMIN', 'USER'] } });
    }
  }

  addRole(): void {
    const role = this.newRole.trim().toUpperCase();
    if (role && !this.roles.includes(role)) {
      this.wizardState.updateState({ security: { type: this.secType, roles: [...this.roles, role] } });
      this.newRole = '';
    }
  }

  removeRole(role: string): void {
    this.wizardState.updateState({ security: { type: this.secType, roles: this.roles.filter(r => r !== role) } });
  }
}
