import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WizardStateService } from '../wizard-state.service';

interface DepCategory {
  name: string;
  deps: { id: string; label: string; description: string; }[];
}

@Component({
  selector: 'app-step6-dependencies',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h2>Dependencies</h2>
    <input type="text" [(ngModel)]="search" placeholder="Search dependencies..." class="search-input">
    @for (cat of filteredCategories(); track cat.name) {
      <div class="category">
        <h3>{{ cat.name }}</h3>
        <div class="dep-grid">
          @for (dep of cat.deps; track dep.id) {
            <label class="dep-item" [class.selected]="isSelected(dep.id)">
              <input type="checkbox" [checked]="isSelected(dep.id)" (change)="toggle(dep.id)">
              <div>
                <strong>{{ dep.label }}</strong>
                <span>{{ dep.description }}</span>
              </div>
            </label>
          }
        </div>
      </div>
    }
  `,
  styles: [`
    h2 { margin-bottom: 1rem; }
    .search-input { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 4px; margin-bottom: 1.5rem; font-size: 0.95rem; }
    .category { margin-bottom: 1.5rem; }
    .category h3 { color: #555; margin-bottom: 0.5rem; font-size: 0.95rem; text-transform: uppercase; }
    .dep-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 0.5rem; }
    .dep-item { display: flex; gap: 0.5rem; padding: 0.75rem; border: 1px solid #e0e0e0; border-radius: 4px; cursor: pointer; align-items: flex-start; }
    .dep-item.selected { border-color: #1976d2; background: #e3f2fd; }
    .dep-item input { margin-top: 3px; }
    .dep-item strong { display: block; font-size: 0.9rem; }
    .dep-item span { font-size: 0.8rem; color: #666; }
  `]
})
export class Step6DependenciesComponent {
  search = '';
  categories: DepCategory[] = [
    { name: 'Web', deps: [
      { id: 'spring-boot-starter-web', label: 'Spring Web', description: 'Build REST APIs with Spring MVC' },
      { id: 'spring-boot-starter-webflux', label: 'Spring WebFlux', description: 'Reactive web framework' },
      { id: 'spring-boot-starter-websocket', label: 'WebSocket', description: 'WebSocket support' },
    ]},
    { name: 'Data', deps: [
      { id: 'spring-boot-starter-data-jpa', label: 'Spring Data JPA', description: 'Java Persistence API with Hibernate' },
      { id: 'spring-boot-starter-data-mongodb', label: 'MongoDB', description: 'MongoDB NoSQL database' },
      { id: 'spring-boot-starter-data-redis', label: 'Redis', description: 'Redis key-value store' },
    ]},
    { name: 'Security', deps: [
      { id: 'spring-boot-starter-security', label: 'Spring Security', description: 'Authentication and authorization' },
      { id: 'spring-boot-starter-oauth2-client', label: 'OAuth2 Client', description: 'OAuth2/OIDC login' },
    ]},
    { name: 'Messaging', deps: [
      { id: 'spring-kafka', label: 'Apache Kafka', description: 'Kafka messaging' },
      { id: 'spring-boot-starter-amqp', label: 'RabbitMQ', description: 'AMQP messaging with RabbitMQ' },
    ]},
    { name: 'Observability', deps: [
      { id: 'spring-boot-starter-actuator', label: 'Actuator', description: 'Production-ready features' },
      { id: 'micrometer-registry-prometheus', label: 'Prometheus', description: 'Prometheus metrics export' },
    ]},
    { name: 'Validation & Utilities', deps: [
      { id: 'spring-boot-starter-validation', label: 'Validation', description: 'Bean Validation with Hibernate Validator' },
      { id: 'spring-boot-starter-mail', label: 'Mail', description: 'Send emails' },
      { id: 'spring-boot-starter-cache', label: 'Cache', description: 'Spring Cache abstraction' },
    ]},
  ];

  constructor(public wizardState: WizardStateService) {}

  filteredCategories(): DepCategory[] {
    if (!this.search.trim()) return this.categories;
    const q = this.search.toLowerCase();
    return this.categories.map(cat => ({
      ...cat,
      deps: cat.deps.filter(d => d.label.toLowerCase().includes(q) || d.description.toLowerCase().includes(q))
    })).filter(cat => cat.deps.length > 0);
  }

  isSelected(id: string): boolean {
    return this.wizardState.state().dependencies.includes(id);
  }

  toggle(id: string): void {
    const deps = this.wizardState.state().dependencies;
    if (deps.includes(id)) {
      this.wizardState.updateState({ dependencies: deps.filter(d => d !== id) });
    } else {
      this.wizardState.updateState({ dependencies: [...deps, id] });
    }
  }
}
