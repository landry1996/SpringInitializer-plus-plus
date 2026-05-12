import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WizardStateService } from '../wizard-state.service';

@Component({
  selector: 'app-step5-modules',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h2>Modules / Bounded Contexts</h2>
    <p class="subtitle">Define the modules for your project</p>
    <div class="module-input">
      <input type="text" [(ngModel)]="newModule" placeholder="Module name (e.g. user, order, payment)" (keyup.enter)="addModule()">
      <button (click)="addModule()">Add</button>
    </div>
    <div class="module-list">
      @for (mod of wizardState.state().architecture.modules; track mod) {
        <div class="module-chip">
          {{ mod }}
          <span class="remove" (click)="removeModule(mod)">&times;</span>
        </div>
      }
    </div>
    @if (wizardState.state().architecture.modules.length === 0) {
      <p class="hint">No modules defined. A default module structure will be used.</p>
    }
  `,
  styles: [`
    h2 { margin-bottom: 0.5rem; color: #333; }
    .subtitle { color: #666; margin-bottom: 1.5rem; }
    .module-input { display: flex; gap: 0.5rem; margin-bottom: 1rem; }
    .module-input input { flex: 1; padding: 0.75rem; border: 1px solid #ddd; border-radius: 4px; }
    .module-input button { padding: 0.75rem 1.5rem; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; }
    .module-list { display: flex; flex-wrap: wrap; gap: 0.5rem; }
    .module-chip { display: inline-flex; align-items: center; gap: 0.5rem; padding: 0.5rem 1rem; background: #e3f2fd; border-radius: 20px; font-size: 0.9rem; }
    .remove { cursor: pointer; color: #c62828; font-size: 1.2rem; }
    .hint { color: #999; font-style: italic; margin-top: 1rem; }
  `]
})
export class Step5ModulesComponent {
  newModule = '';
  constructor(public wizardState: WizardStateService) {}

  addModule(): void {
    const name = this.newModule.trim().toLowerCase();
    if (name && !this.wizardState.state().architecture.modules.includes(name)) {
      const arch = this.wizardState.state().architecture;
      this.wizardState.updateState({ architecture: { ...arch, modules: [...arch.modules, name] } });
      this.newModule = '';
    }
  }

  removeModule(mod: string): void {
    const arch = this.wizardState.state().architecture;
    this.wizardState.updateState({ architecture: { ...arch, modules: arch.modules.filter(m => m !== mod) } });
  }
}
