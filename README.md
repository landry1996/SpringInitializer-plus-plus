# SpringForge

> Intelligent enterprise-grade Spring Boot project generator with AI-powered recommendations.

Generate production-ready Spring Boot projects in under 60 seconds via a 10-step wizard, enforcing architectural blueprints and organizational standards.

## Features

- **4 Architecture Blueprints**: Hexagonal, Layered, DDD, Microservices
- **AI Recommendations**: Intelligent dependency suggestions, anti-pattern detection, compatibility scoring
- **Blueprint Marketplace**: Community-driven templates with search, ratings, and versioning
- **Multi-tenant SaaS**: Organization management, API keys, subscription plans (Free/Pro/Enterprise)
- **Async Generation Pipeline**: Validate → Resolve → Generate → Post-Process (ZIP)
- **Real-time Progress**: WebSocket-based generation tracking
- **Admin Panel**: User management, audit logs, analytics dashboard
- **Internationalization**: 4 languages (EN, FR, DE, ES)
- **IntelliJ Plugin**: Generate projects directly from your IDE
- **CLI Tool**: Go-based CLI for terminal workflows

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Frontend | Angular 18 (Standalone Components) |
| CLI | Go + Cobra |
| IDE | IntelliJ Platform Plugin |
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

### Production

```bash
docker compose up -d --build
```

## API Endpoints

### Project Generation
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/v1/projects/generate | Start generation (returns 202) |
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
├── src/main/java/com/springforge/
│   ├── config/          # Cache, Async, Database, OpenAPI configs
│   ├── security/        # Rate limiting, CORS, CSP, input validation
│   ├── recommendation/  # AI engine with pluggable rules
│   ├── marketplace/     # Blueprint store with search/rating
│   ├── admin/           # Dashboard, users, audit
│   ├── tenant/          # Multi-tenant, API keys, quotas
│   ├── i18n/            # Internationalization
│   ├── generator/       # Generation pipeline
│   └── shared/          # Common utilities
├── springforge-frontend/    # Angular 18 SPA
├── springforge-cli/         # Go CLI (Cobra)
├── springforge-intellij-plugin/  # IntelliJ IDE plugin
├── infra/
│   ├── k8s/             # Kubernetes manifests
│   ├── monitoring/      # Grafana dashboards, alerts
│   └── prometheus/      # Prometheus config
└── .github/workflows/   # CI/CD pipelines
```

## Deployment

### Docker Compose (Development/Staging)
```bash
docker compose up -d
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
./mvnw test

# Frontend unit tests
cd springforge-frontend && npm test

# E2E tests
cd springforge-frontend && npx playwright test
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines.

## License

MIT
