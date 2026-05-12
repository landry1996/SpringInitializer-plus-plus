import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WizardStateService } from '../wizard-state.service';

@Component({
  selector: 'app-step3-buildtool',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h2>Build Tool</h2>
    <div class="cards">
      <div class="card" [class.selected]="wizardState.state().buildTool === 'MAVEN'" (click)="select('MAVEN')">
        <h3>Maven</h3>
        <p>Industry standard, XML-based build tool with excellent IDE support.</p>
      </div>
      <div class="card" [class.selected]="wizardState.state().buildTool === 'GRADLE_GROOVY'" (click)="select('GRADLE_GROOVY')">
        <h3>Gradle (Groovy)</h3>
        <p>Flexible build tool with Groovy DSL. Faster builds with daemon.</p>
      </div>
      <div class="card" [class.selected]="wizardState.state().buildTool === 'GRADLE_KOTLIN'" (click)="select('GRADLE_KOTLIN')">
        <h3>Gradle (Kotlin)</h3>
        <p>Type-safe Kotlin DSL with better IDE autocompletion.</p>
      </div>
    </div>
  `,
  styles: [`
    h2 { margin-bottom: 1.5rem; color: #333; }
    .cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; }
    .card { padding: 1.5rem; border: 2px solid #e0e0e0; border-radius: 8px; cursor: pointer; transition: all 0.2s; }
    .card:hover { border-color: #90caf9; background: #f8fbff; }
    .card.selected { border-color: #1976d2; background: #e3f2fd; }
    .card h3 { margin: 0 0 0.5rem; color: #333; }
    .card p { margin: 0; color: #666; font-size: 0.9rem; }
  `]
})
export class Step3BuildToolComponent {
  constructor(public wizardState: WizardStateService) {}
  select(tool: string): void {
    this.wizardState.updateState({ buildTool: tool });
  }
}
