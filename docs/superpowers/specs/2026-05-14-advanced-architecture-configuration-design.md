# Advanced Architecture Configuration — Design Spec

**Date**: 2026-05-14  
**Status**: Approved  
**Scope**: Full configuration of microservices and all architecture types in the project creation wizard

---

## Overview

Enable users to fully configure their chosen architecture during project creation. Each architecture type exposes specific configuration options relevant to its patterns. The microservices architecture gets the richest configuration with per-service database selection, inter-service communication mapping, and resilience patterns.

---

## Architecture-Specific Configuration

### 1. Microservices

#### 1.1 Service Definition

Each microservice is configured individually:

| Field | Type | Description |
|-------|------|-------------|
| name | string | Service name (e.g., "user-service") |
| description | string | Purpose of the service |
| port | number | Server port (auto-assigned, overridable) |
| databases | DatabaseConfig[] | One or more databases per service |

#### 1.2 Database Per Service

Each service can have **multiple databases** for different purposes:

| Database | Use Cases |
|----------|-----------|
| PostgreSQL | Relational data, transactions |
| MySQL | Relational data, legacy compatibility |
| MongoDB | Document storage, flexible schemas |
| Redis | Caching, session store, pub/sub |
| Cassandra | Time-series, high write throughput |
| Neo4j | Graph relationships |

**Configuration per database**:
- `type`: Database engine
- `purpose`: PRIMARY_STORE, CACHE, SEARCH, EVENT_STORE
- `generateEntity`: boolean — scaffold sample entities for this DB

Example: A `user-service` could have PostgreSQL (PRIMARY_STORE) + Redis (CACHE).

#### 1.3 Synchronous Communication

| Field | Type | Description |
|-------|------|-------------|
| protocol | REST / gRPC | Communication protocol |
| source | string | Calling service |
| target | string | Target service |
| endpoints | string[] | Exposed endpoint paths on target |
| loadBalancing | boolean | Client-side load balancing |

#### 1.4 Asynchronous Communication

| Field | Type | Description |
|-------|------|-------------|
| broker | KAFKA / RABBITMQ | Message broker |
| source | string | Producer service |
| target | string | Consumer service |
| topic/exchange | string | Topic or exchange name |
| eventType | string | Event class name |
| serialization | JSON / AVRO / PROTOBUF | Message format |

#### 1.5 Resilience Patterns (per connection)

| Pattern | Options |
|---------|---------|
| Circuit Breaker | enabled, failureThreshold, waitDuration, slidingWindowSize |
| Retry | enabled, maxAttempts, backoffDelay |
| Timeout | enabled, duration |
| Bulkhead | enabled, maxConcurrentCalls |
| Rate Limiting | enabled, limitForPeriod, periodDuration |

#### 1.6 Service Discovery & API Gateway

| Field | Type | Description |
|-------|------|-------------|
| discoveryType | EUREKA / CONSUL | Service registry |
| gateway.routes | GatewayRoute[] | Per-service routing rules |
| gateway.rateLimiting | RateLimitConfig | Global rate limiting |
| gateway.authPerRoute | Map<route, authType> | Auth requirements per route |
| gateway.cors | CorsConfig | CORS configuration |

#### 1.7 Orchestration Patterns

| Field | Type | Description |
|-------|------|-------------|
| sagaPattern | CHOREOGRAPHY / ORCHESTRATION / NONE | Saga type |
| sagas | SagaDefinition[] | Steps, compensations, participants |

#### 1.8 Centralized Configuration

| Field | Type | Description |
|-------|------|-------------|
| configServer | boolean | Enable Spring Cloud Config |
| profiles | string[] | Environment profiles (dev, staging, prod) |
| secretManagement | VAULT / ENV / NONE | Secret storage strategy |

#### 1.9 Shared Libraries

| Field | Type | Description |
|-------|------|-------------|
| sharedModules | SharedModule[] | Name, type (DTOs, utils, events, exceptions) |
| generateCommonLib | boolean | Generate a common library module |

#### 1.10 Observability

| Field | Type | Description |
|-------|------|-------------|
| distributedTracing | ZIPKIN / JAEGER / NONE | Tracing backend |
| metricsExporter | PROMETHEUS / NONE | Metrics collection |
| centralizedLogging | ELK / LOKI / NONE | Log aggregation |
| correlationHeaders | boolean | Auto-propagate correlation IDs |

---

### 2. Monolithic

| Category | Configuration |
|----------|--------------|
| **Modules** | names[], descriptions[], dependencies between modules |
| **Packaging** | JAR / WAR |
| **Profiles** | Spring profiles (dev, staging, prod) with per-profile properties |
| **Caching** | strategy (REDIS / CAFFEINE / EHCACHE / NONE), cached entities |
| **Scheduling** | enabled, jobs[] with cron expressions |
| **Session** | STATELESS_JWT / STATEFUL_REDIS / IN_MEMORY |
| **Database** | type, connection pooling (HikariCP config) |

---

### 3. Layered

| Category | Configuration |
|----------|--------------|
| **Layers** | Standard (Controller/Service/Repository) + custom layers (names[]) |
| **Cross-cutting** | AOP aspects: LOGGING / CACHING / TRANSACTION / AUDIT / PERFORMANCE |
| **DTO strategy** | MAPSTRUCT / MODELMAPPER / MANUAL, dedicated mapping layer |
| **Validation** | Separate validation layer, custom validators per entity |
| **Exception handling** | Global handler, custom error codes, error response format |
| **Modules** | names[], which layers each module uses |

---

### 4. Hexagonal

| Category | Configuration |
|----------|--------------|
| **Inbound Ports** | REST / GRPC / GRAPHQL / CLI / MESSAGING |
| **Outbound Ports** | DATABASE / EXTERNAL_API / MESSAGING / FILE_STORAGE / CACHE |
| **Use Cases** | name, description, inbound port, outbound ports used |
| **Adapters** | Per port: adapter type, specific configuration |
| **Domain Model** | entities[], valueObjects[], domainServices[] |
| **Anti-corruption layer** | enabled per outbound port, transformer definitions |

---

### 5. DDD (Domain-Driven Design)

| Category | Configuration |
|----------|--------------|
| **Bounded Contexts** | name, description, responsibility |
| **Context Mapping** | relationships: SHARED_KERNEL / CUSTOMER_SUPPLIER / ACL / CONFORMIST / OPEN_HOST / PUBLISHED_LANGUAGE |
| **Aggregates** | Per context: name, root entity, invariants description |
| **Domain Events** | name, publisher context, subscriber contexts, payload fields |
| **Value Objects** | name, fields[], owning aggregate |
| **Repositories** | Per aggregate root: interface methods |
| **Domain Services** | name, contexts involved, operations |

---

### 6. CQRS

| Category | Configuration |
|----------|--------------|
| **Command Side** | commands[] (name, fields, handler, target aggregate) |
| **Query Side** | queries[] (name, fields, handler, read model) |
| **Write Store** | database type, schema strategy |
| **Read Store** | database type (can differ from write), denormalization strategy |
| **Event Store** | type (AXON / EVENTSTOREDB / CUSTOM), retention policy |
| **Projections** | readModels[] (name, source events, rebuild strategy: REPLAY / SNAPSHOT) |
| **Separation** | SAME_DB / SEPARATE_DB (independent read/write databases) |

---

### 7. Event-Driven

| Category | Configuration |
|----------|--------------|
| **Events** | name, schema format (AVRO / JSON / PROTOBUF), version, fields[] |
| **Topology** | producers[] (service, events), consumers[] (service, events, group) |
| **Broker** | KAFKA / RABBITMQ, cluster config |
| **Sagas** | name, steps[] (action, compensation, participant), timeout |
| **Reliability** | deadLetterQueue (enabled, retryCount), idempotency (enabled, strategy) |
| **Schema Registry** | type (CONFLUENT / APICURIO / NONE), compatibility (BACKWARD / FORWARD / FULL) |
| **Stream Processing** | enabled, framework (KAFKA_STREAMS / SPRING_CLOUD_STREAM) |

---

### 8. Modulith

| Category | Configuration |
|----------|--------------|
| **Modules** | name, responsibility, publicAPI (exposed packages) |
| **Dependencies** | allowedDependencies matrix (module A can depend on B but not C) |
| **Internal Events** | name, publisher module, subscriber modules, async (boolean) |
| **Shared Kernel** | modules[] (shared DTOs, utils, exceptions) |
| **Architecture Tests** | ArchUnit rules auto-generated, custom rules[] |
| **Module Boundaries** | enforcement level: WARN / FAIL |

---

## Frontend Changes

### New Wizard Step: "Architecture Configuration" (Step 5, replacing current Modules step)

This step dynamically renders configuration forms based on the selected architecture (Step 4). The step uses sub-tabs or accordion sections to organize the configuration by category.

**For Microservices:**
1. **Services Tab** — Add/remove services, set name/description/port
2. **Databases Tab** — Per-service database selection (multi-select with purpose)
3. **Communication Tab** — Define sync/async connections between services
4. **Resilience Tab** — Configure patterns per connection
5. **Infrastructure Tab** — Discovery, gateway, config server
6. **Observability Tab** — Tracing, metrics, logging

**For other architectures:** Relevant tabs/sections based on their configuration categories.

### Architecture Diagram (Step 10 — Review)

A read-only SVG/Canvas diagram showing:
- Services/modules as boxes
- Solid arrows = synchronous communication (labeled with protocol)
- Dashed arrows = asynchronous communication (labeled with broker/topic)
- Database icons attached to each service
- Color coding by module/bounded context

---

## Backend Changes

### New/Updated DTOs

```
ProjectConfiguration.architecture expanded:
  - MicroservicesConfig (services, communications, resilience, discovery, gateway, config, observability)
  - MonolithicConfig (modules, packaging, profiles, caching, scheduling, session)
  - LayeredConfig (layers, crossCutting, dtoStrategy, validation, exceptionHandling)
  - HexagonalConfig (inboundPorts, outboundPorts, useCases, adapters, domainModel)
  - DddConfig (boundedContexts, contextMapping, aggregates, domainEvents, valueObjects)
  - CqrsConfig (commands, queries, writeStore, readStore, eventStore, projections)
  - EventDrivenConfig (events, topology, broker, sagas, reliability, schemaRegistry)
  - ModulithConfig (modules, dependencies, internalEvents, sharedKernel, archTests)
```

### Template Expansion

For each architecture type, Freemarker templates must generate:
- Configuration files (application.yml) reflecting all user choices
- Source code scaffolding matching the configured structure
- Docker Compose services for selected databases/brokers
- Test scaffolding for architecture enforcement (ArchUnit)

### Validation Rules

- Service names must be unique within a project
- Communication references must point to existing services
- Database purpose PRIMARY_STORE required at least once per service
- Bounded context names unique, context mapping references valid contexts
- Saga steps must reference existing services/events

---

## Implementation Phases

### Phase 1: Backend DTOs & Models
- Create configuration DTOs for all 8 architectures
- Validation logic for each

### Phase 2: Frontend — Microservices Configuration
- Dynamic step with sub-tabs
- Service definition with per-service database selection
- Communication mapping (sync + async)
- Resilience pattern configuration

### Phase 3: Frontend — Other Architectures Configuration
- Monolithic, Layered, Hexagonal forms
- DDD, CQRS, Event-Driven, Modulith forms

### Phase 4: Backend — Template Generation
- Expand Freemarker templates for all new configuration options
- Generate proper Docker Compose with selected databases/brokers

### Phase 5: Architecture Diagram
- Read-only visualization in Review step
- Auto-generated from configuration state

### Phase 6: Integration & Testing
- End-to-end testing
- Validation that generated projects compile
