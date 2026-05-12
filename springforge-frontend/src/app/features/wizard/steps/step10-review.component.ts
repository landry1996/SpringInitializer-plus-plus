import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WizardStateService } from '../wizard-state.service';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-step10-review',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h2>Review & Generate</h2>
    <div class="review-grid">
      <div class="review-section">
        <h3>Project</h3>
        <p><strong>{{ state().metadata.groupId }}:{{ state().metadata.artifactId }}</strong></p>
        <p>{{ state().metadata.name || state().metadata.artifactId }}</p>
      </div>
      <div class="review-section">
        <h3>Stack</h3>
        <p>Java {{ state().versions.javaVersion }} / Spring Boot {{ state().versions.springBootVersion }}</p>
        <p>{{ state().buildTool }} / {{ state().architecture.type }}</p>
      </div>
      <div class="review-section">
        <h3>Modules</h3>
        <p>{{ state().architecture.modules.length > 0 ? state().architecture.modules.join(', ') : 'Default structure' }}</p>
      </div>
      <div class="review-section">
        <h3>Dependencies ({{ state().dependencies.length }})</h3>
        <p>{{ state().dependencies.join(', ') || 'None' }}</p>
      </div>
      <div class="review-section">
        <h3>Security</h3>
        <p>{{ state().security ? state().security!.type + ' - Roles: ' + state().security!.roles.join(', ') : 'None' }}</p>
      </div>
      <div class="review-section">
        <h3>Infrastructure</h3>
        <p>Docker: {{ state().infrastructure.docker }} | Compose: {{ state().infrastructure.dockerCompose }} | CI: {{ state().infrastructure.ci }}</p>
      </div>
    </div>

    @if (generationStatus() === 'idle') {
      <button class="generate-btn" (click)="generate()">Generate Project</button>
    } @else if (generationStatus() === 'generating') {
      <div class="progress">
        <div class="spinner"></div>
        <p>Generating your project...</p>
      </div>
    } @else if (generationStatus() === 'completed') {
      <div class="success">
        <p>Project generated successfully!</p>
        <button class="download-btn" (click)="download()">Download ZIP</button>
      </div>
    } @else if (generationStatus() === 'failed') {
      <div class="error-msg">
        <p>Generation failed: {{ errorMessage() }}</p>
        <button class="generate-btn" (click)="generate()">Retry</button>
      </div>
    }
  `,
  styles: [`
    h2 { margin-bottom: 1.5rem; }
    .review-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 2rem; }
    .review-section { padding: 1rem; background: #f9f9f9; border-radius: 8px; }
    .review-section h3 { margin: 0 0 0.5rem; font-size: 0.85rem; text-transform: uppercase; color: #888; }
    .review-section p { margin: 0.25rem 0; font-size: 0.9rem; word-break: break-all; }
    .generate-btn { width: 100%; padding: 1rem; background: #4caf50; color: white; border: none; border-radius: 8px; font-size: 1.1rem; cursor: pointer; }
    .generate-btn:hover { background: #43a047; }
    .download-btn { width: 100%; padding: 1rem; background: #1976d2; color: white; border: none; border-radius: 8px; font-size: 1.1rem; cursor: pointer; margin-top: 1rem; }
    .progress { text-align: center; padding: 2rem; }
    .spinner { width: 40px; height: 40px; border: 4px solid #e0e0e0; border-top: 4px solid #1976d2; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 1rem; }
    @keyframes spin { to { transform: rotate(360deg); } }
    .success { text-align: center; padding: 1rem; background: #e8f5e9; border-radius: 8px; }
    .error-msg { text-align: center; padding: 1rem; background: #ffebee; border-radius: 8px; color: #c62828; }
  `]
})
export class Step10ReviewComponent {
  generationStatus = signal<'idle' | 'generating' | 'completed' | 'failed'>('idle');
  generationId = signal('');
  errorMessage = signal('');

  constructor(public wizardState: WizardStateService, private apiService: ApiService) {}
  state() { return this.wizardState.state(); }

  generate(): void {
    this.generationStatus.set('generating');
    const config = this.wizardState.getConfiguration();
    this.apiService.generateProject(config).subscribe({
      next: (res) => {
        this.generationId.set(res.generationId);
        this.pollStatus(res.generationId);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || 'Unknown error');
        this.generationStatus.set('failed');
      }
    });
  }

  private pollStatus(id: string): void {
    const interval = setInterval(() => {
      this.apiService.getGenerationStatus(id).subscribe({
        next: (status) => {
          if (status.status === 'COMPLETED') {
            clearInterval(interval);
            this.generationStatus.set('completed');
          } else if (status.status === 'FAILED') {
            clearInterval(interval);
            this.errorMessage.set(status.errorMessage || 'Generation failed');
            this.generationStatus.set('failed');
          }
        },
        error: () => { clearInterval(interval); this.generationStatus.set('failed'); }
      });
    }, 2000);
  }

  download(): void {
    this.apiService.downloadGeneration(this.generationId()).subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = this.state().metadata.artifactId + '.zip';
      a.click();
      URL.revokeObjectURL(url);
    });
  }
}
