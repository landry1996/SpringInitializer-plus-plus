import { Component, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WizardStateService } from '../wizard-state.service';
import { VersionService } from '../../../core/services/version.service';

@Component({
  selector: 'app-step2-versions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h2>Java & Spring Boot Version</h2>
    <div class="form-grid">
      <div class="form-group">
        <label>Java Version</label>
        <select [ngModel]="wizardState.state().versions.javaVersion" (ngModelChange)="onJavaChange($event)">
          @for (v of versionService.javaVersions; track v.id) {
            <option [value]="v.id">{{ v.label }}</option>
          }
        </select>
      </div>
      <div class="form-group">
        <label>
          Spring Boot Version
          @if (versionService.loading()) {
            <span class="loading">(fetching...)</span>
          }
        </label>
        <select [ngModel]="wizardState.state().versions.springBootVersion" (ngModelChange)="onSpringBootChange($event)">
          @for (v of versionService.springBootVersions(); track v.id) {
            <option [value]="v.id">{{ v.name }}</option>
          }
        </select>
      </div>
    </div>
    @if (compatibilityError()) {
      <div class="error-banner">
        {{ compatibilityError() }}
      </div>
    }
    <div class="info">
      <p><strong>Compatibility:</strong> Spring Boot 3.x requires Java 17+, Spring Boot 4.x requires Java 21+</p>
      <p class="hint">Versions fetched from start.spring.io. LTS versions recommended for production.</p>
    </div>
  `,
  styles: [`
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; }
    .form-group { display: flex; flex-direction: column; }
    label { margin-bottom: 0.5rem; font-weight: 500; color: #555; }
    select { padding: 0.75rem; border: 1px solid #ddd; border-radius: 4px; font-size: 0.95rem; }
    h2 { margin-bottom: 1.5rem; color: #333; }
    .error-banner { margin-top: 1rem; padding: 0.75rem 1rem; background: #ffebee; color: #c62828; border-radius: 6px; font-size: 0.9rem; }
    .info { margin-top: 1.5rem; padding: 1rem; background: #f5f5f5; border-radius: 6px; }
    .info p { margin: 0.25rem 0; font-size: 0.85rem; color: #666; }
    .hint { font-style: italic; }
    .loading { font-size: 0.8rem; color: #1976d2; font-weight: normal; }
  `]
})
export class Step2VersionsComponent {
  compatibilityError = computed(() => {
    const s = this.wizardState.state();
    return this.versionService.getCompatibilityMessage(s.versions.javaVersion, s.versions.springBootVersion);
  });

  constructor(public wizardState: WizardStateService, public versionService: VersionService) {}

  onJavaChange(javaVersion: string): void {
    const versions = this.wizardState.state().versions;
    this.wizardState.updateState({ versions: { ...versions, javaVersion } });
  }

  onSpringBootChange(springBootVersion: string): void {
    const versions = this.wizardState.state().versions;
    this.wizardState.updateState({ versions: { ...versions, springBootVersion } });
  }
}
