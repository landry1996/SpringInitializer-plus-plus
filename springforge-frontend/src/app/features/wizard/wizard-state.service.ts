import { Injectable, signal, computed } from '@angular/core';

export interface DatabaseConfig {
  type: string;
  purpose: string;
  generateEntity: boolean;
}

export interface ServiceDefinition {
  name: string;
  description: string;
  port: number;
  databases: DatabaseConfig[];
}

export interface SyncCommunication {
  protocol: string;
  source: string;
  target: string;
  endpoints: string[];
  loadBalancing: boolean;
}

export interface AsyncCommunication {
  broker: string;
  source: string;
  target: string;
  topic: string;
  eventType: string;
  serialization: string;
}

export interface ResilienceConfig {
  source: string;
  target: string;
  circuitBreaker: { enabled: boolean; failureThreshold: number; waitDurationSeconds: number; slidingWindowSize: number; };
  retry: { enabled: boolean; maxAttempts: number; backoffDelayMs: number; };
  timeout: { enabled: boolean; durationMs: number; };
  bulkhead: { enabled: boolean; maxConcurrentCalls: number; };
  rateLimit: { enabled: boolean; limitForPeriod: number; periodDurationSeconds: number; };
}

export interface GatewayRoute {
  id: string;
  path: string;
  serviceId: string;
  stripPrefix: boolean;
}

export interface SagaStep {
  service: string;
  action: string;
  compensation: string;
}

export interface SagaDefinition {
  name: string;
  steps: SagaStep[];
}

export interface MicroservicesConfig {
  services: ServiceDefinition[];
  syncCommunications: SyncCommunication[];
  asyncCommunications: AsyncCommunication[];
  resilience: ResilienceConfig[];
  discovery: { type: string; };
  gateway: {
    routes: GatewayRoute[];
    rateLimiting: { enabled: boolean; replenishRate: number; burstCapacity: number; };
    authPerRoute: { [key: string]: string };
    cors: { allowedOrigins: string[]; allowedMethods: string[]; allowedHeaders: string[]; };
  };
  orchestration: { sagaPattern: string; sagas: SagaDefinition[]; };
  centralizedConfig: { configServer: boolean; profiles: string[]; secretManagement: string; };
  sharedModules: { name: string; type: string; }[];
  observability: { distributedTracing: string; metricsExporter: string; centralizedLogging: string; correlationHeaders: boolean; };
}

export interface MonolithicConfig {
  modules: { name: string; description: string; dependsOn: string[]; }[];
  packaging: string;
  profiles: string[];
  caching: { strategy: string; cachedEntities: string[]; };
  scheduling: { enabled: boolean; jobs: { name: string; cronExpression: string; description: string; }[]; };
  sessionStrategy: string;
  database: { type: string; poolSize: number; connectionTimeout: number; };
}

export interface LayeredConfig {
  layers: string[];
  crossCuttingConcerns: string[];
  dtoStrategy: string;
  separateValidationLayer: boolean;
  exceptionHandling: { globalHandler: boolean; customErrorCodes: string[]; errorResponseFormat: string; };
  modules: { name: string; layers: string[]; }[];
}

export interface HexagonalConfig {
  inboundPorts: string[];
  outboundPorts: string[];
  useCases: { name: string; description: string; inboundPort: string; outboundPorts: string[]; }[];
  adapters: { portName: string; adapterType: string; configuration: { [key: string]: string }; }[];
  domainModel: { entities: string[]; valueObjects: string[]; domainServices: string[]; };
}

export interface DddConfig {
  boundedContexts: { name: string; description: string; responsibility: string; }[];
  contextMapping: { source: string; target: string; relationType: string; }[];
  aggregates: { contextName: string; name: string; rootEntity: string; invariants: string; }[];
  domainEvents: { name: string; publisherContext: string; subscriberContexts: string[]; payloadFields: string[]; }[];
  valueObjects: { name: string; fields: string[]; owningAggregate: string; }[];
  domainServices: { name: string; contexts: string[]; operations: string[]; }[];
}

export interface CqrsConfig {
  commands: { name: string; fields: string[]; handler: string; targetAggregate: string; }[];
  queries: { name: string; fields: string[]; handler: string; readModel: string; }[];
  writeStore: { databaseType: string; schemaStrategy: string; };
  readStore: { databaseType: string; schemaStrategy: string; };
  eventStore: { type: string; retentionPolicy: string; };
  projections: { name: string; sourceEvents: string[]; rebuildStrategy: string; }[];
  separationStrategy: string;
}

export interface EventDrivenConfig {
  events: { name: string; schemaFormat: string; version: string; fields: string[]; }[];
  producers: { service: string; events: string[]; }[];
  consumers: { service: string; events: string[]; consumerGroup: string; }[];
  broker: { type: string; clusterConfig: { [key: string]: string }; };
  sagas: SagaDefinition[];
  reliability: { deadLetterQueue: boolean; retryCount: number; idempotency: boolean; idempotencyStrategy: string; };
  schemaRegistry: { type: string; compatibility: string; };
  streamProcessing: { enabled: boolean; framework: string; };
}

export interface ModulithConfig {
  modules: { name: string; responsibility: string; publicApi: string[]; }[];
  allowedDependencies: { from: string; to: string; allowed: boolean; }[];
  internalEvents: { name: string; publisherModule: string; subscriberModules: string[]; async: boolean; }[];
  sharedKernelModules: string[];
  archTests: { enabled: boolean; enforcementLevel: string; customRules: string[]; };
}

export interface WizardState {
  metadata: { groupId: string; artifactId: string; name: string; description: string; packageName: string; };
  versions: { javaVersion: string; springBootVersion: string; };
  buildTool: string;
  architecture: {
    type: string;
    modules: string[];
    enableCQRS: boolean;
    enableEventSourcing: boolean;
    microservices: MicroservicesConfig | null;
    monolithic: MonolithicConfig | null;
    layered: LayeredConfig | null;
    hexagonal: HexagonalConfig | null;
    ddd: DddConfig | null;
    cqrs: CqrsConfig | null;
    eventDriven: EventDrivenConfig | null;
    modulith: ModulithConfig | null;
  };
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
    architecture: {
      type: 'HEXAGONAL', modules: [], enableCQRS: false, enableEventSourcing: false,
      microservices: null, monolithic: null, layered: null, hexagonal: null,
      ddd: null, cqrs: null, eventDriven: null, modulith: null
    },
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
    'Architecture Config', 'Dependencies', 'Security', 'Infrastructure',
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
        enableEventSourcing: config.architecture.enableEventSourcing || false,
        microservices: config.architecture.microservices || null,
        monolithic: config.architecture.monolithic || null,
        layered: config.architecture.layered || null,
        hexagonal: config.architecture.hexagonal || null,
        ddd: config.architecture.ddd || null,
        cqrs: config.architecture.cqrs || null,
        eventDriven: config.architecture.eventDriven || null,
        modulith: config.architecture.modulith || null
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
