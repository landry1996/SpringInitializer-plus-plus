# Template Variables Documentation

## Common Variables (all templates)

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| `groupId` | String | Maven group ID | `com.example` |
| `artifactId` | String | Maven artifact ID | `my-project` |
| `projectName` | String | Human-readable project name | `My Project` |
| `projectDescription` | String | Project description | `A Spring Boot app` |
| `javaVersion` | String | Java version | `21` |
| `springBootVersion` | String | Spring Boot version | `3.3.5` |
| `packageName` | String | Base Java package | `com.example.myproject` |
| `applicationClassName` | String | Main class name | `MyProjectApplication` |
| `dependencies` | List<Map> | Resolved dependencies | `[{groupId, artifactId}]` |

## Module-Specific Variables (Hexagonal / DDD)

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| `moduleName` | String | Module name (lowercase) | `order` |
| `entityName` | String | Entity name (PascalCase) | `Order` |

## Microservices Variables

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| `services` | List<Map> | List of services | `[{name: "order-service"}]` |
| `serviceName` | String | Individual service name | `order-service` |

## Template Structure

```
templates/core/
├── common/           # Generated for all architectures
│   ├── pom.xml.ftl
│   ├── Application.java.ftl
│   ├── application.yml.ftl
│   ├── Dockerfile.ftl
│   ├── gitignore.ftl
│   └── docker-compose.yml.ftl
├── hexagonal/        # Hexagonal architecture (per module)
│   ├── DomainEntity.java.ftl
│   ├── Repository.java.ftl
│   ├── Controller.java.ftl
│   ├── UseCase.java.ftl
│   ├── UseCaseImpl.java.ftl
│   ├── JpaAdapter.java.ftl
│   └── package-info.java.ftl
├── ddd/              # DDD architecture (per module)
│   ├── Aggregate.java.ftl
│   ├── ValueObject.java.ftl
│   ├── DomainEvent.java.ftl
│   ├── Repository.java.ftl
│   ├── Command.java.ftl
│   ├── Query.java.ftl
│   ├── CommandHandler.java.ftl
│   └── package-info.java.ftl
├── layered/          # Layered architecture (per module)
│   ├── Controller.java.ftl
│   ├── Service.java.ftl
│   └── Repository.java.ftl
└── microservices/    # Microservices scaffold
    ├── service-registry-pom.xml.ftl
    ├── ServiceRegistryApplication.java.ftl
    ├── service-registry-application.yml.ftl
    ├── api-gateway-pom.xml.ftl
    ├── ApiGatewayApplication.java.ftl
    ├── api-gateway-application.yml.ftl
    ├── service-pom.xml.ftl
    ├── service-application.yml.ftl
    └── docker-compose.yml.ftl
```

## Architecture Behavior

### Hexagonal
Each module generates: domain entity, repository port, use case interface + impl, JPA adapter, REST controller, package-info.

### DDD
Each module generates: aggregate root, value object (ID), domain events, repository port, command/query records, command handler, package-info.

### Layered
Generates shared directories (controller/, service/, repository/, model/) with one controller, service, and repository per module.

### Microservices
Generates multi-project structure: service-registry (Eureka), api-gateway (Spring Cloud Gateway + Resilience4j), individual service scaffolds with Eureka client, plus a docker-compose.yml orchestrating all services.
