import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-stepper',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="stepper">
      <div class="stepper-header">
        @for (step of steps; track step; let i = $index) {
          <div class="step" [class.active]="i === currentStep" [class.completed]="i < currentStep">
            <div class="step-number">{{ i < currentStep ? '\u2713' : i + 1 }}</div>
            <div class="step-label">{{ step }}</div>
          </div>
          @if (i < steps.length - 1) {
            <div class="step-connector" [class.active]="i < currentStep"></div>
          }
        }
      </div>
    </div>
  `,
  styles: [`
    .stepper { padding: 1rem 0; }
    .stepper-header { display: flex; align-items: center; overflow-x: auto; padding: 0.5rem; }
    .step { display: flex; flex-direction: column; align-items: center; min-width: 80px; }
    .step-number {
      width: 32px; height: 32px; border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      background: #e0e0e0; color: #666; font-weight: 600; font-size: 0.85rem;
      transition: all 0.3s;
    }
    .step.active .step-number { background: #1976d2; color: white; }
    .step.completed .step-number { background: #4caf50; color: white; }
    .step-label { font-size: 0.7rem; margin-top: 4px; text-align: center; color: #666; }
    .step.active .step-label { color: #1976d2; font-weight: 600; }
    .step-connector { flex: 1; height: 2px; background: #e0e0e0; min-width: 20px; margin: 0 4px; }
    .step-connector.active { background: #4caf50; }
  `]
})
export class StepperComponent {
  @Input() steps: string[] = [];
  @Input() currentStep = 0;
}
