import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StepperComponent } from '../../shared/components/stepper/stepper.component';
import { WizardStateService } from './wizard-state.service';
import { Step1MetadataComponent } from './steps/step1-metadata.component';
import { Step2VersionsComponent } from './steps/step2-versions.component';
import { Step3BuildToolComponent } from './steps/step3-buildtool.component';
import { Step4ArchitectureComponent } from './steps/step4-architecture.component';
import { Step5ModulesComponent } from './steps/step5-modules.component';
import { Step6DependenciesComponent } from './steps/step6-dependencies.component';
import { Step7SecurityComponent } from './steps/step7-security.component';
import { Step8InfrastructureComponent } from './steps/step8-infrastructure.component';
import { Step9OptionsComponent } from './steps/step9-options.component';
import { Step10ReviewComponent } from './steps/step10-review.component';

@Component({
  selector: 'app-wizard',
  standalone: true,
  imports: [
    CommonModule, StepperComponent,
    Step1MetadataComponent, Step2VersionsComponent, Step3BuildToolComponent,
    Step4ArchitectureComponent, Step5ModulesComponent, Step6DependenciesComponent,
    Step7SecurityComponent, Step8InfrastructureComponent, Step9OptionsComponent,
    Step10ReviewComponent
  ],
  template: `
    <div class="wizard-container">
      <app-stepper [steps]="wizardState.stepLabels" [currentStep]="wizardState.currentStep()"></app-stepper>
      <div class="wizard-content">
        @switch (wizardState.currentStep()) {
          @case (0) { <app-step1-metadata /> }
          @case (1) { <app-step2-versions /> }
          @case (2) { <app-step3-buildtool /> }
          @case (3) { <app-step4-architecture /> }
          @case (4) { <app-step5-modules /> }
          @case (5) { <app-step6-dependencies /> }
          @case (6) { <app-step7-security /> }
          @case (7) { <app-step8-infrastructure /> }
          @case (8) { <app-step9-options /> }
          @case (9) { <app-step10-review /> }
        }
      </div>
      <div class="wizard-nav">
        <button class="btn-secondary" (click)="wizardState.previous()" [disabled]="wizardState.currentStep() === 0">Previous</button>
        @if (wizardState.currentStep() < 9) {
          <button class="btn-primary" (click)="wizardState.next()">Next</button>
        }
      </div>
    </div>
  `,
  styles: [`
    .wizard-container { max-width: 900px; margin: 2rem auto; padding: 0 1rem; }
    .wizard-content { background: white; border-radius: 8px; padding: 2rem; box-shadow: 0 2px 8px rgba(0,0,0,0.08); min-height: 400px; }
    .wizard-nav { display: flex; justify-content: space-between; margin-top: 1.5rem; }
    .btn-primary { padding: 0.75rem 2rem; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 1rem; }
    .btn-primary:hover { background: #1565c0; }
    .btn-secondary { padding: 0.75rem 2rem; background: white; color: #666; border: 1px solid #ddd; border-radius: 4px; cursor: pointer; font-size: 1rem; }
    .btn-secondary:hover { background: #f5f5f5; }
    .btn-secondary:disabled { opacity: 0.5; cursor: not-allowed; }
  `]
})
export class WizardComponent {
  constructor(public wizardState: WizardStateService) {}
}
