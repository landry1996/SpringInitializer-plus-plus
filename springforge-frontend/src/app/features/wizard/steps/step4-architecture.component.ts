import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WizardStateService } from '../wizard-state.service';

@Component({
  selector: 'app-step4-architecture',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h2>Architecture Blueprint</h2>
    <div class="cards">
      @for (arch of architectures; track arch.type) {
        <div class="card" [class.selected]="wizardState.state().architecture.type === arch.type" (click)="select(arch.type)">
          <h3>{{ arch.name }}</h3>
          <p>{{ arch.description }}</p>
        </div>
      }
    </div>
  `,
  styles: [`
    h2 { margin-bottom: 1.5rem; color: #333; }
    .cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 1rem; }
    .card { padding: 1.5rem; border: 2px solid #e0e0e0; border-radius: 8px; cursor: pointer; transition: all 0.2s; }
    .card:hover { border-color: #90caf9; }
    .card.selected { border-color: #1976d2; background: #e3f2fd; }
    .card h3 { margin: 0 0 0.5rem; }
    .card p { margin: 0; color: #666; font-size: 0.9rem; }
  `]
})
export class Step4ArchitectureComponent {
  architectures = [
    { type: 'HEXAGONAL', name: 'Hexagonal', description: 'Ports & Adapters. Domain isolated from infrastructure.' },
    { type: 'LAYERED', name: 'Layered', description: 'Traditional Controller/Service/Repository layers.' },
    { type: 'DDD', name: 'DDD', description: 'Bounded contexts with aggregates and domain events.' },
    { type: 'MICROSERVICES', name: 'Microservices', description: 'Multi-service scaffold with API gateway.' },
  ];

  constructor(public wizardState: WizardStateService) {}

  select(type: string): void {
    const arch = this.wizardState.state().architecture;
    this.wizardState.updateState({ architecture: { ...arch, type } });
  }
}
