import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WizardStateService } from '../wizard-state.service';

@Component({
  selector: 'app-step9-options',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h2>Generation Options</h2>
    <div class="toggles">
      <label class="toggle-item">
        <input type="checkbox" [(ngModel)]="wizardState.state().options.includeExamples">
        <div><strong>Include Examples</strong><span>Generate example controllers, services, and tests</span></div>
      </label>
      <label class="toggle-item">
        <input type="checkbox" [(ngModel)]="wizardState.state().options.formatCode">
        <div><strong>Format Code</strong><span>Apply code formatting to generated files</span></div>
      </label>
      <label class="toggle-item">
        <input type="checkbox" [(ngModel)]="wizardState.state().options.runCompileCheck">
        <div><strong>Compile Check</strong><span>Verify generated project compiles (slower)</span></div>
      </label>
    </div>
  `,
  styles: [`
    h2 { margin-bottom: 1.5rem; }
    .toggles { display: flex; flex-direction: column; gap: 0.75rem; }
    .toggle-item { display: flex; align-items: center; gap: 1rem; padding: 1rem; border: 1px solid #e0e0e0; border-radius: 8px; cursor: pointer; }
    .toggle-item input { width: 20px; height: 20px; }
    .toggle-item strong { display: block; }
    .toggle-item span { color: #666; font-size: 0.85rem; }
  `]
})
export class Step9OptionsComponent {
  constructor(public wizardState: WizardStateService) {}
}
