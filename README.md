# SpringForge

> Intelligent enterprise-grade Spring Boot project generator.

Generate production-ready Spring Boot projects in under 60 seconds via a 10-step wizard, enforcing architectural blueprints and organizational standards.

## Features

- **4 Architecture Blueprints**: Hexagonal, Layered, DDD, Microservices
- **Async Generation Pipeline**: Validate → Resolve → Generate → Post-Process (ZIP)
- **JWT Authentication** with refresh token rotation
- **Template Engine**: Freemarker-based project generation
- **Modular Monolith**: Spring Modulith with enforced module boundaries

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Modules | Spring Modulith 1.2.6 |
| Database | PostgreSQL 16 |
| Migration | Flyway |
| Templates | Freemarker 2.3.33 |
| Auth | JWT (jjwt 0.12.6) + BCrypt |
| Tests | JUnit 5, Testcontainers, ArchUnit |

## Quick Start

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Maven 3.9+

### Development

```bash
# Start PostgreSQL
docker compose up -d

# Run the application
cd springforge-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API will be available at http://localhost:8080

### Production

```bash
# Copy and configure environment
cp .env.example .env
# Edit .env with production values

# Build and run
docker compose -f docker-compose.prod.yml up -d
```

## API Endpoints

### Authentication
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/v1/auth/register | Register new user |
| POST | /api/v1/auth/login | Login |
| POST | /api/v1/auth/refresh | Refresh tokens |
| POST | /api/v1/auth/logout | Logout (revoke tokens) |

### Blueprints (public)
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/blueprints | List all blueprints |
| GET | /api/v1/blueprints/{id} | Get blueprint by ID |

### Templates (authenticated)
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/templates | List templates |
| GET | /api/v1/templates/{id} | Get template |
| POST | /api/v1/templates | Create template (ADMIN) |

### Generation (authenticated)
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/v1/projects/generate | Start generation (returns 202) |
| GET | /api/v1/generations/{id}/status | Check generation status |
| GET | /api/v1/generations/{id}/download | Download generated ZIP |

### Swagger UI
Available at: http://localhost:8080/swagger-ui.html

## Architecture

```
springforge-backend/
  src/main/java/com/springforge/
    shared/       (Security, Config, Exceptions)
    user/         (Auth module - hexagonal)
    blueprint/    (Blueprint definitions - hexagonal)
    template/     (Freemarker templates - hexagonal)
    generator/    (Generation pipeline - hexagonal)
```

## Testing

```bash
cd springforge-backend
./mvnw test
```

## License

MIT
