import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { WizardStateService } from '../wizard-state.service';

@Component({
  selector: 'app-step2-versions',
  standalone: true,
  imports: [FormsModule],
  template: `
    <h2>Java & Spring Boot Version</h2>
    <div class="form-grid">
      <div class="form-group">
        <label>Java Version</label>
        <select [(ngModel)]="wizardState.state().versions.javaVersion">
          <option value="11">Java 11</option>
          <option value="17">Java 17</option>
          <option value="21">Java 21 (Recommended)</option>
          <option value="25">Java 25</option>
        </select>
      </div>
      <div class="form-group">
        <label>Spring Boot Version</label>
        <select [(ngModel)]="wizardState.state().versions.springBootVersion">
          <option value="3.3.5">3.3.5 (Latest Stable)</option>
          <option value="3.2.12">3.2.12</option>
          <option value="3.1.12">3.1.12</option>
          <option value="3.0.13">3.0.13</option>
          <option value="2.7.18">2.7.18 (Legacy)</option>
        </select>
      </div>
    </div>
  `,
  styles: [`
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; }
    .form-group { display: flex; flex-direction: column; }
    label { margin-bottom: 0.5rem; font-weight: 500; color: #555; }
    select { padding: 0.75rem; border: 1px solid #ddd; border-radius: 4px; font-size: 0.95rem; }
    h2 { margin-bottom: 1.5rem; color: #333; }
  `]
})
export class Step2VersionsComponent {
  constructor(public wizardState: WizardStateService) {}
}
