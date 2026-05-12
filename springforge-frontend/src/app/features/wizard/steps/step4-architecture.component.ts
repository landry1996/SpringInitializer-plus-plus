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
          @if (arch.recommended) {
            <span class="badge">Recommended</span>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    h2 { margin-bottom: 1.5rem; color: #333; }
    .cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; }
    .card { padding: 1.25rem; border: 2px solid #e0e0e0; border-radius: 8px; cursor: pointer; transition: all 0.2s; position: relative; }
    .card:hover { border-color: #90caf9; }
    .card.selected { border-color: #1976d2; background: #e3f2fd; }
    .card h3 { margin: 0 0 0.5rem; font-size: 0.95rem; }
    .card p { margin: 0; color: #666; font-size: 0.82rem; line-height: 1.4; }
    .badge { position: absolute; top: 8px; right: 8px; background: #4caf50; color: white; font-size: 0.7rem; padding: 2px 6px; border-radius: 10px; }
  `]
})
export class Step4ArchitectureComponent {
  architectures = [
    { type: 'HEXAGONAL', name: 'Hexagonal', description: 'Ports & Adapters. Domain isolated from infrastructure. Ideal for complex business logic.', recommended: true },
    { type: 'LAYERED', name: 'Layered (3-tier)', description: 'Traditional Controller → Service → Repository. Ideal for CRUD and medium apps.' },
    { type: 'DDD', name: 'DDD', description: 'Domain-Driven Design with bounded contexts, aggregates and domain events.' },
    { type: 'CQRS', name: 'CQRS', description: 'Command/Query separation. Recommended when read/write models differ significantly.' },
    { type: 'EVENT_DRIVEN', name: 'Event-Driven', description: 'Reactive architecture based on events. Ideal for async workflows and inter-service integration.' },
    { type: 'MICROSERVICES', name: 'Microservices', description: 'Distributed services with Gateway, Discovery, Circuit Breaker and Config Server.' },
    { type: 'MODULITH', name: 'Modulith', description: 'Modular monolith with explicit boundaries. Spring Modulith native. Best of both worlds.' },
    { type: 'MONOLITHIC', name: 'Monolithic', description: 'Simple single deployable. Ideal for prototypes, MVPs and small applications.' }
  ];

  constructor(public wizardState: WizardStateService) {}

  select(type: string): void {
    const arch = this.wizardState.state().architecture;
    this.wizardState.updateState({ architecture: { ...arch, type } });
  }
}
