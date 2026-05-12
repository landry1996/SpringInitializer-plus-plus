import { Injectable, signal, computed } from '@angular/core';

export interface WizardState {
  metadata: { groupId: string; artifactId: string; name: string; description: string; packageName: string; };
  versions: { javaVersion: string; springBootVersion: string; };
  buildTool: string;
  architecture: { type: string; modules: string[]; enableCQRS: boolean; enableEventSourcing: boolean; };
  dependencies: string[];
  security: { type: string; roles: string[]; } | null;
  infrastructure: { docker: boolean; dockerCompose: boolean; kubernetes: boolean; ci: string; };
  options: { includeExamples: boolean; formatCode: boolean; runCompileCheck: boolean; outputFormat: string; };
}

@Injectable({ providedIn: 'root' })
export class WizardStateService {
  private readonly _currentStep = signal(0);
  private readonly _state = signal<WizardState>({
    metadata: { groupId: 'com.example', artifactId: '', name: '', description: '', packageName: '' },
    versions: { javaVersion: '21', springBootVersion: '3.3.5' },
    buildTool: 'MAVEN',
    architecture: { type: 'HEXAGONAL', modules: [], enableCQRS: false, enableEventSourcing: false },
    dependencies: ['spring-boot-starter-web'],
    security: null,
    infrastructure: { docker: true, dockerCompose: true, kubernetes: false, ci: 'GITHUB_ACTIONS' },
    options: { includeExamples: true, formatCode: true, runCompileCheck: false, outputFormat: 'ZIP' }
  });

  readonly currentStep = this._currentStep.asReadonly();
  readonly state = this._state.asReadonly();
  readonly totalSteps = 10;

  readonly stepLabels = [
    'Metadata', 'Versions', 'Build Tool', 'Architecture',
    'Modules', 'Dependencies', 'Security', 'Infrastructure',
    'Options', 'Review'
  ];

  next(): void {
    if (this._currentStep() < this.totalSteps - 1) {
      this._currentStep.update(s => s + 1);
    }
  }

  previous(): void {
    if (this._currentStep() > 0) {
      this._currentStep.update(s => s - 1);
    }
  }

  goToStep(step: number): void {
    if (step >= 0 && step < this.totalSteps) {
      this._currentStep.set(step);
    }
  }

  updateState(partial: Partial<WizardState>): void {
    this._state.update(s => ({ ...s, ...partial }));
  }

  getConfiguration(): any {
    const s = this._state();
    return {
      metadata: {
        ...s.metadata,
        javaVersion: s.versions.javaVersion,
        springBootVersion: s.versions.springBootVersion,
        buildTool: s.buildTool,
        packageName: s.metadata.packageName || (s.metadata.groupId + '.' + s.metadata.artifactId.replace(/-/g, ''))
      },
      architecture: s.architecture,
      dependencies: s.dependencies,
      security: s.security,
      infrastructure: s.infrastructure,
      options: s.options
    };
  }

  reset(): void {
    this._currentStep.set(0);
  }

  importConfiguration(config: any): void {
    if (!config) return;
    const newState: Partial<WizardState> = {};
    if (config.metadata) {
      newState.metadata = {
        groupId: config.metadata.groupId || 'com.example',
        artifactId: config.metadata.artifactId || '',
        name: config.metadata.name || '',
        description: config.metadata.description || '',
        packageName: config.metadata.packageName || ''
      };
      newState.versions = {
        javaVersion: config.metadata.javaVersion || '21',
        springBootVersion: config.metadata.springBootVersion || '3.3.5'
      };
      if (config.metadata.buildTool) {
        newState.buildTool = config.metadata.buildTool;
      }
    }
    if (config.architecture) {
      newState.architecture = {
        type: config.architecture.type || 'HEXAGONAL',
        modules: config.architecture.modules || [],
        enableCQRS: config.architecture.enableCQRS || false,
        enableEventSourcing: config.architecture.enableEventSourcing || false
      };
    }
    if (config.dependencies) {
      newState.dependencies = config.dependencies;
    }
    if (config.security !== undefined) {
      newState.security = config.security;
    }
    if (config.infrastructure) {
      newState.infrastructure = {
        docker: config.infrastructure.docker ?? true,
        dockerCompose: config.infrastructure.dockerCompose ?? true,
        kubernetes: config.infrastructure.kubernetes ?? false,
        ci: config.infrastructure.ci || 'GITHUB_ACTIONS'
      };
    }
    if (config.options) {
      newState.options = {
        includeExamples: config.options.includeExamples ?? true,
        formatCode: config.options.formatCode ?? true,
        runCompileCheck: config.options.runCompileCheck ?? false,
        outputFormat: config.options.outputFormat || 'ZIP'
      };
    }
    this._state.update(s => ({ ...s, ...newState }));
  }
}
