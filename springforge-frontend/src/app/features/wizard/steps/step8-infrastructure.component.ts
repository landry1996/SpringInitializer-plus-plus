import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WizardStateService } from '../wizard-state.service';

@Component({
  selector: 'app-step8-infrastructure',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h2>Infrastructure</h2>
    <div class="toggles">
      <label class="toggle-item">
        <input type="checkbox" [(ngModel)]="wizardState.state().infrastructure.docker">
        <div><strong>Dockerfile</strong><span>Multi-stage Docker build</span></div>
      </label>
      <label class="toggle-item">
        <input type="checkbox" [(ngModel)]="wizardState.state().infrastructure.dockerCompose">
        <div><strong>Docker Compose</strong><span>Local development services</span></div>
      </label>
      <label class="toggle-item">
        <input type="checkbox" [(ngModel)]="wizardState.state().infrastructure.kubernetes">
        <div><strong>Kubernetes</strong><span>K8s deployment manifests</span></div>
      </label>
    </div>
    <div class="ci-section">
      <h3>CI/CD Pipeline</h3>
      <select [(ngModel)]="wizardState.state().infrastructure.ci">
        <option value="NONE">None</option>
        <option value="GITHUB_ACTIONS">GitHub Actions</option>
        <option value="GITLAB_CI">GitLab CI</option>
      </select>
    </div>
  `,
  styles: [`
    h2 { margin-bottom: 1.5rem; }
    .toggles { display: flex; flex-direction: column; gap: 0.75rem; margin-bottom: 2rem; }
    .toggle-item { display: flex; align-items: center; gap: 1rem; padding: 1rem; border: 1px solid #e0e0e0; border-radius: 8px; cursor: pointer; }
    .toggle-item input { width: 20px; height: 20px; }
    .toggle-item strong { display: block; }
    .toggle-item span { color: #666; font-size: 0.85rem; }
    .ci-section h3 { margin-bottom: 0.75rem; }
    select { padding: 0.75rem; border: 1px solid #ddd; border-radius: 4px; width: 100%; max-width: 300px; }
  `]
})
export class Step8InfrastructureComponent {
  constructor(public wizardState: WizardStateService) {}
}
