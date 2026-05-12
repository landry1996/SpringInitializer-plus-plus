import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { WizardStateService } from '../wizard-state.service';

@Component({
  selector: 'app-step1-metadata',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="header-row">
      <h2>Project Metadata</h2>
      <label class="import-btn">
        Import Config
        <input type="file" accept=".json" (change)="importConfig($event)" hidden>
      </label>
    </div>
    <div class="form-grid">
      <div class="form-group">
        <label>Group ID</label>
        <input type="text" [(ngModel)]="wizardState.state().metadata.groupId" placeholder="com.example">
      </div>
      <div class="form-group">
        <label>Artifact ID</label>
        <input type="text" [(ngModel)]="wizardState.state().metadata.artifactId" placeholder="my-service">
      </div>
      <div class="form-group">
        <label>Project Name</label>
        <input type="text" [(ngModel)]="wizardState.state().metadata.name" placeholder="My Service">
      </div>
      <div class="form-group full-width">
        <label>Description</label>
        <textarea [(ngModel)]="wizardState.state().metadata.description" placeholder="A brief description..." rows="3"></textarea>
      </div>
      <div class="form-group">
        <label>Package Name</label>
        <input type="text" [(ngModel)]="wizardState.state().metadata.packageName" [placeholder]="defaultPackage()">
      </div>
    </div>
  `,
  styles: [`
    .header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    .header-row h2 { margin: 0; color: #333; }
    .import-btn { padding: 0.5rem 1rem; background: #1976d2; color: white; border-radius: 6px; font-size: 0.85rem; cursor: pointer; }
    .import-btn:hover { background: #1565c0; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .full-width { grid-column: 1 / -1; }
    .form-group { display: flex; flex-direction: column; }
    label { margin-bottom: 0.25rem; font-weight: 500; color: #555; }
    input, textarea { padding: 0.75rem; border: 1px solid #ddd; border-radius: 4px; font-size: 0.95rem; }
  `]
})
export class Step1MetadataComponent {
  constructor(public wizardState: WizardStateService) {}
  defaultPackage(): string {
    const s = this.wizardState.state().metadata;
    return s.groupId + '.' + (s.artifactId || 'myapp').replace(/-/g, '');
  }

  importConfig(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    const file = input.files[0];
    const reader = new FileReader();
    reader.onload = () => {
      try {
        const config = JSON.parse(reader.result as string);
        this.wizardState.importConfiguration(config);
      } catch {
        alert('Invalid configuration file');
      }
    };
    reader.readAsText(file);
  }
}
