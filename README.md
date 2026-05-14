# SpringForge

> Intelligent enterprise-grade Spring Boot project generator with AI-powered recommendations.

Generate production-ready Spring Boot projects in under 60 seconds via a 10-step wizard, enforcing architectural blueprints and organizational standards.

## Features

- **8 Architecture Blueprints**: Monolithic, Layered, Hexagonal, DDD, CQRS, Event-Driven, Microservices, Modulith
- **Advanced Microservices Configuration**: Per-service database selection (PostgreSQL, MySQL, MongoDB, Redis, Cassandra, Neo4j), inter-service communication mapping (REST/gRPC sync, Kafka/RabbitMQ async), resilience patterns (Circuit Breaker, Retry, Timeout, Bulkhead, Rate Limit), service discovery (Eureka/Consul), API Gateway with rate limiting, centralized configuration, distributed tracing
- **Architecture-Specific Options**: DDD bounded contexts with context mapping, CQRS with separate read/write stores, Event-Driven with schema registry, Modulith with ArchUnit enforcement
- **Architecture Diagram**: Auto-generated SVG visualization showing services, connections, and databases
- **AI Recommendations**: Intelligent dependency suggestions, anti-pattern detection, compatibility scoring
- **Blueprint Marketplace**: Community-driven templates with search, ratings, and versioning
- **Multi-tenant SaaS**: Organization management, API keys, subscription plans (Free/Pro/Enterprise)
- **Async Generation Pipeline**: Validate → Resolve → Generate → Post-Process (ZIP)
- **Real-time Progress**: WebSocket-based generation tracking
- **Admin Panel**: User management, audit logs, analytics dashboard
- **Internationalization**: 4 languages (EN, FR, DE, ES)
- **IntelliJ Plugin**: Generate projects directly from your IDE
- **VS Code Extension**: Architecture visualization and project generation
- **CLI Tool**: Go-based CLI for terminal workflows

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Frontend | Angular 18 (Standalone Components, Signals) |
| CLI | Go + Cobra |
| IDE | IntelliJ Platform Plugin, VS Code Extension |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |
| Messaging | Apache Kafka |
| Auth | Keycloak + JWT |
| Migration | Flyway |
| Templates | Freemarker 2.3.33 |
| Monitoring | Prometheus + Grafana |
| Tests | JUnit 5, Mockito, Playwright |

## Quick Start

### Prerequisites
- Java 21+
- Node.js 20+
- Docker & Docker Compose
- Maven 3.9+

### Development (Full Stack)

```bash
# Start all services
docker compose up -d

# Backend only (with local DB)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Frontend only
cd springforge-frontend
npm install
npm start
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:4200 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Keycloak | http://localhost:8180 |
| Grafana | http://localhost:3000 |
| Prometheus | http://localhost:9090 |

### Production (VPS)

```bash
git clone https://github.com/landry1996/SpringInitializer-plus-plus.git /opt/springforge
cd /opt/springforge
cp .env.example .env && nano .env
chmod +x deploy.sh && ./deploy.sh init
```

See [GUIDE_DEPLOIEMENT.md](GUIDE_DEPLOIEMENT.md) for complete VPS deployment instructions.

## Wizard Steps

The project creation wizard guides users through 10 steps:

| Step | Name | Description |
|------|------|-------------|
| 1 | Metadata | Group ID, Artifact ID, project name, description |
| 2 | Versions | Java version, Spring Boot version |
| 3 | Build Tool | Maven, Gradle Groovy, Gradle Kotlin |
| 4 | Architecture | Choose from 8 architecture types |
| 5 | **Architecture Config** | Dynamic configuration per architecture type |
| 6 | Dependencies | Spring Boot starters with AI suggestions |
| 7 | Security | JWT, OAuth2, Keycloak with roles |
| 8 | Infrastructure | Docker, Kubernetes, CI/CD |
| 9 | Options | Examples, formatting, compilation check |
| 10 | Review | Summary, architecture diagram, generate |

### Microservices Configuration (Step 5)

When selecting Microservices architecture, users can configure:

- **Services**: Define each microservice (name, description, port)
- **Databases per service**: PostgreSQL, MySQL, MongoDB, Redis, Cassandra, Neo4j — with purpose (Primary Store, Cache, Search, Event Store)
- **Synchronous communication**: REST or gRPC connections between services
- **Asynchronous communication**: Kafka or RabbitMQ with topics, event types, serialization format (JSON/Avro/Protobuf)
- **Resilience patterns**: Circuit Breaker, Retry, Timeout, Bulkhead, Rate Limit per connection
- **Service Discovery**: Eureka or Consul
- **API Gateway**: Rate limiting, CORS, auth per route
- **Centralized Config**: Spring Cloud Config Server with profiles
- **Secret Management**: HashiCorp Vault, Environment Variables
- **Orchestration**: Saga pattern (Choreography/Orchestration)
- **Observability**: Zipkin/Jaeger tracing, Prometheus metrics, ELK/Loki logging

## API Endpoints

### Project Generation
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/v1/projects/generate | Start generation (returns 202) |
| POST | /api/v1/projects/validate | Validate configuration |
| POST | /api/v1/projects/preview | Preview project structure |
| GET | /api/v1/generations/{id}/status | Check generation status |
| GET | /api/v1/generations/{id}/download | Download generated ZIP |

### Recommendations
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/v1/recommendations | Get AI recommendations |
| POST | /api/v1/recommendations/score | Get compatibility score |

### Marketplace
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/marketplace/blueprints | List/search blueprints |
| GET | /api/v1/marketplace/blueprints/{id} | Blueprint details |
| POST | /api/v1/marketplace/blueprints | Publish blueprint |
| POST | /api/v1/marketplace/blueprints/{id}/rate | Rate blueprint |

### Organizations (Multi-tenant)
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/v1/organizations | Create organization |
| GET | /api/v1/organizations/{id} | Get organization |
| POST | /api/v1/organizations/{id}/api-keys | Generate API key |
| GET | /api/v1/organizations/{id}/usage | Get usage/quotas |

### Admin
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/admin/dashboard | Dashboard stats |
| GET | /api/v1/admin/users | List users |
| GET | /api/v1/admin/audit | Audit logs |

### i18n
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/i18n/locales | Supported locales |
| GET | /api/v1/i18n/messages/{locale} | Messages for locale |

## Architecture

```
SpringForge/
├── springforge-backend/
│   └── src/main/java/com/springforge/
│       ├── config/          # Cache, Async, Database, OpenAPI configs
│       ├── security/        # Rate limiting, CORS, CSP, input validation
│       ├── recommendation/  # AI engine with pluggable rules
│       ├── marketplace/     # Blueprint store with search/rating
│       ├── admin/           # Dashboard, users, audit
│       ├── tenant/          # Multi-tenant, API keys, quotas
│       ├── i18n/            # Internationalization
│       ├── generator/
│       │   ├── domain/      # ProjectConfiguration (40+ DTOs for 8 architectures)
│       │   ├── application/ # Pipeline steps, validators
│       │   └── api/         # REST controllers
│       └── shared/          # Common utilities
├── springforge-frontend/    # Angular 18 SPA
│   └── src/app/features/wizard/
│       ├── wizard-state.service.ts  # Reactive state with architecture configs
│       └── steps/
│           ├── step5-modules.component.ts  # Dynamic architecture config UI
│           └── architecture-diagram.component.ts  # SVG visualization
├── springforge-cli/         # Go CLI (Cobra)
├── springforge-intellij-plugin/  # IntelliJ IDE plugin
├── springforge-vscode/      # VS Code extension
├── infra/
│   ├── k8s/             # Kubernetes manifests
│   ├── monitoring/      # Grafana dashboards, alerts
│   ├── nginx/           # Reverse proxy config
│   └── prometheus/      # Prometheus config
└── .github/workflows/   # CI/CD pipelines
```

## Deployment

### Docker Compose (Development/Staging)
```bash
docker compose up -d
```

### Docker Compose (Production VPS)
```bash
./deploy.sh init          # First deployment
./deploy.sh ssl domain.com email@example.com  # Enable HTTPS
./deploy.sh update        # Update to latest
./deploy.sh backup        # Backup database
./deploy.sh status        # Check health
```

### Kubernetes (Production)
```bash
kubectl apply -f infra/k8s/namespace.yml
kubectl apply -f infra/k8s/
```

### CI/CD
- **CI**: Automated on every push (build, test, security scan)
- **Release**: Tag-based Docker image build + push to GHCR
- **Deploy**: Manual production deployment with confirmation gate

## Monitoring

- **Prometheus**: Scrapes Spring Boot Actuator metrics every 15s
- **Grafana**: Pre-configured dashboards (request rate, latency, JVM, DB pool, Kafka)
- **Alerts**: Automated alerting on high error rate, response time, memory, connection pool exhaustion

## Testing

```bash
# Backend unit tests
cd springforge-backend && ./mvnw test

# Frontend unit tests
cd springforge-frontend && npm test

# E2E tests
cd springforge-frontend && npx playwright test
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines.

## License

MIT
