package com.springforge.generator.domain;

import java.util.List;
import java.util.Map;

public record ProjectConfiguration(
        Metadata metadata,
        Architecture architecture,
        List<String> dependencies,
        SecurityConfig security,
        InfrastructureConfig infrastructure,
        MessagingConfig messaging,
        ObservabilityConfig observability,
        TestingConfig testing,
        MultiTenantConfig multiTenant,
        GenerationOptions options
) {
    public record Metadata(
            String groupId,
            String artifactId,
            String name,
            String description,
            String packageName,
            String javaVersion,
            String springBootVersion,
            BuildTool buildTool
    ) {}

    public record Architecture(
            String type,
            List<String> modules,
            boolean enableCQRS,
            boolean enableEventSourcing,
            MicroservicesConfig microservices,
            MonolithicConfig monolithic,
            LayeredConfig layered,
            HexagonalConfig hexagonal,
            DddConfig ddd,
            CqrsConfig cqrs,
            EventDrivenConfig eventDriven,
            ModulithConfig modulith
    ) {}

    // ==================== MICROSERVICES ====================

    public record MicroservicesConfig(
            List<ServiceDefinition> services,
            List<SyncCommunication> syncCommunications,
            List<AsyncCommunication> asyncCommunications,
            List<ResilienceConfig> resilience,
            DiscoveryConfig discovery,
            GatewayConfig gateway,
            OrchestrationConfig orchestration,
            CentralizedConfigConfig centralizedConfig,
            List<SharedModule> sharedModules,
            MicroservicesObservabilityConfig observability
    ) {}

    public record ServiceDefinition(
            String name,
            String description,
            int port,
            List<DatabaseConfig> databases
    ) {}

    public record DatabaseConfig(
            String type,
            String purpose,
            boolean generateEntity
    ) {}

    public record SyncCommunication(
            String protocol,
            String source,
            String target,
            List<String> endpoints,
            boolean loadBalancing
    ) {}

    public record AsyncCommunication(
            String broker,
            String source,
            String target,
            String topic,
            String eventType,
            String serialization
    ) {}

    public record ResilienceConfig(
            String source,
            String target,
            CircuitBreakerConfig circuitBreaker,
            RetryConfig retry,
            TimeoutConfig timeout,
            BulkheadConfig bulkhead,
            RateLimitConfig rateLimit
    ) {}

    public record CircuitBreakerConfig(
            boolean enabled,
            int failureThreshold,
            int waitDurationSeconds,
            int slidingWindowSize
    ) {}

    public record RetryConfig(
            boolean enabled,
            int maxAttempts,
            int backoffDelayMs
    ) {}

    public record TimeoutConfig(
            boolean enabled,
            int durationMs
    ) {}

    public record BulkheadConfig(
            boolean enabled,
            int maxConcurrentCalls
    ) {}

    public record RateLimitConfig(
            boolean enabled,
            int limitForPeriod,
            int periodDurationSeconds
    ) {}

    public record DiscoveryConfig(
            String type
    ) {}

    public record GatewayConfig(
            List<GatewayRoute> routes,
            GatewayRateLimitConfig rateLimiting,
            Map<String, String> authPerRoute,
            CorsConfig cors
    ) {}

    public record GatewayRoute(
            String id,
            String path,
            String serviceId,
            boolean stripPrefix
    ) {}

    public record GatewayRateLimitConfig(
            boolean enabled,
            int replenishRate,
            int burstCapacity
    ) {}

    public record CorsConfig(
            List<String> allowedOrigins,
            List<String> allowedMethods,
            List<String> allowedHeaders
    ) {}

    public record OrchestrationConfig(
            String sagaPattern,
            List<SagaDefinition> sagas
    ) {}

    public record SagaDefinition(
            String name,
            List<SagaStep> steps
    ) {}

    public record SagaStep(
            String service,
            String action,
            String compensation
    ) {}

    public record CentralizedConfigConfig(
            boolean configServer,
            List<String> profiles,
            String secretManagement
    ) {}

    public record SharedModule(
            String name,
            String type
    ) {}

    public record MicroservicesObservabilityConfig(
            String distributedTracing,
            String metricsExporter,
            String centralizedLogging,
            boolean correlationHeaders
    ) {}

    // ==================== MONOLITHIC ====================

    public record MonolithicConfig(
            List<MonolithModule> modules,
            String packaging,
            List<String> profiles,
            CachingConfig caching,
            SchedulingConfig scheduling,
            String sessionStrategy,
            MonolithDatabaseConfig database
    ) {}

    public record MonolithModule(
            String name,
            String description,
            List<String> dependsOn
    ) {}

    public record CachingConfig(
            String strategy,
            List<String> cachedEntities
    ) {}

    public record SchedulingConfig(
            boolean enabled,
            List<ScheduledJob> jobs
    ) {}

    public record ScheduledJob(
            String name,
            String cronExpression,
            String description
    ) {}

    public record MonolithDatabaseConfig(
            String type,
            int poolSize,
            int connectionTimeout
    ) {}

    // ==================== LAYERED ====================

    public record LayeredConfig(
            List<String> layers,
            List<String> crossCuttingConcerns,
            String dtoStrategy,
            boolean separateValidationLayer,
            ExceptionHandlingConfig exceptionHandling,
            List<LayeredModule> modules
    ) {}

    public record ExceptionHandlingConfig(
            boolean globalHandler,
            List<String> customErrorCodes,
            String errorResponseFormat
    ) {}

    public record LayeredModule(
            String name,
            List<String> layers
    ) {}

    // ==================== HEXAGONAL ====================

    public record HexagonalConfig(
            List<String> inboundPorts,
            List<String> outboundPorts,
            List<UseCaseDefinition> useCases,
            List<AdapterDefinition> adapters,
            DomainModelConfig domainModel
    ) {}

    public record UseCaseDefinition(
            String name,
            String description,
            String inboundPort,
            List<String> outboundPorts
    ) {}

    public record AdapterDefinition(
            String portName,
            String adapterType,
            Map<String, String> configuration
    ) {}

    public record DomainModelConfig(
            List<String> entities,
            List<String> valueObjects,
            List<String> domainServices
    ) {}

    // ==================== DDD ====================

    public record DddConfig(
            List<BoundedContext> boundedContexts,
            List<ContextRelationship> contextMapping,
            List<AggregateDefinition> aggregates,
            List<DomainEventDefinition> domainEvents,
            List<ValueObjectDefinition> valueObjects,
            List<DomainServiceDefinition> domainServices
    ) {}

    public record BoundedContext(
            String name,
            String description,
            String responsibility
    ) {}

    public record ContextRelationship(
            String source,
            String target,
            String relationType
    ) {}

    public record AggregateDefinition(
            String contextName,
            String name,
            String rootEntity,
            String invariants
    ) {}

    public record DomainEventDefinition(
            String name,
            String publisherContext,
            List<String> subscriberContexts,
            List<String> payloadFields
    ) {}

    public record ValueObjectDefinition(
            String name,
            List<String> fields,
            String owningAggregate
    ) {}

    public record DomainServiceDefinition(
            String name,
            List<String> contexts,
            List<String> operations
    ) {}

    // ==================== CQRS ====================

    public record CqrsConfig(
            List<CommandDefinition> commands,
            List<QueryDefinition> queries,
            StoreConfig writeStore,
            StoreConfig readStore,
            EventStoreConfig eventStore,
            List<ProjectionDefinition> projections,
            String separationStrategy
    ) {}

    public record CommandDefinition(
            String name,
            List<String> fields,
            String handler,
            String targetAggregate
    ) {}

    public record QueryDefinition(
            String name,
            List<String> fields,
            String handler,
            String readModel
    ) {}

    public record StoreConfig(
            String databaseType,
            String schemaStrategy
    ) {}

    public record EventStoreConfig(
            String type,
            String retentionPolicy
    ) {}

    public record ProjectionDefinition(
            String name,
            List<String> sourceEvents,
            String rebuildStrategy
    ) {}

    // ==================== EVENT-DRIVEN ====================

    public record EventDrivenConfig(
            List<EventDefinition> events,
            List<ProducerDefinition> producers,
            List<ConsumerDefinition> consumers,
            BrokerConfig broker,
            List<SagaDefinition> sagas,
            ReliabilityConfig reliability,
            SchemaRegistryConfig schemaRegistry,
            StreamProcessingConfig streamProcessing
    ) {}

    public record EventDefinition(
            String name,
            String schemaFormat,
            String version,
            List<String> fields
    ) {}

    public record ProducerDefinition(
            String service,
            List<String> events
    ) {}

    public record ConsumerDefinition(
            String service,
            List<String> events,
            String consumerGroup
    ) {}

    public record BrokerConfig(
            String type,
            Map<String, String> clusterConfig
    ) {}

    public record ReliabilityConfig(
            boolean deadLetterQueue,
            int retryCount,
            boolean idempotency,
            String idempotencyStrategy
    ) {}

    public record SchemaRegistryConfig(
            String type,
            String compatibility
    ) {}

    public record StreamProcessingConfig(
            boolean enabled,
            String framework
    ) {}

    // ==================== MODULITH ====================

    public record ModulithConfig(
            List<ModulithModule> modules,
            List<ModuleDependency> allowedDependencies,
            List<InternalEvent> internalEvents,
            List<String> sharedKernelModules,
            ArchTestConfig archTests
    ) {}

    public record ModulithModule(
            String name,
            String responsibility,
            List<String> publicApi
    ) {}

    public record ModuleDependency(
            String from,
            String to,
            boolean allowed
    ) {}

    public record InternalEvent(
            String name,
            String publisherModule,
            List<String> subscriberModules,
            boolean async
    ) {}

    public record ArchTestConfig(
            boolean enabled,
            String enforcementLevel,
            List<String> customRules
    ) {}

    // ==================== EXISTING CONFIGS ====================

    public record SecurityConfig(
            String type,
            List<String> roles
    ) {}

    public record InfrastructureConfig(
            boolean docker,
            boolean dockerCompose,
            boolean kubernetes,
            boolean helm,
            String ci
    ) {}

    public record MessagingConfig(
            String type,
            List<Map<String, String>> topics
    ) {}

    public record ObservabilityConfig(
            boolean enabled,
            boolean metrics,
            boolean tracing,
            boolean structuredLogging
    ) {}

    public record TestingConfig(
            boolean enabled,
            boolean testcontainers,
            boolean archunit,
            boolean contractTesting
    ) {}

    public record MultiTenantConfig(
            boolean enabled,
            String strategy
    ) {}

    public record GenerationOptions(
            boolean includeExamples,
            boolean formatCode,
            boolean runCompileCheck,
            String outputFormat
    ) {}
}
