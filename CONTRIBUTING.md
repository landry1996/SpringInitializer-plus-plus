# Contributing to SpringForge

## Development Setup

### Prerequisites
- Java 21 (Temurin recommended)
- Node.js 20+
- Go 1.21+ (for CLI)
- Docker & Docker Compose
- Maven 3.9+

### Getting Started

```bash
# Clone the repository
git clone https://github.com/landry1996/SpringInitializer-plus-plus.git
cd SpringInitializer-plus-plus

# Start infrastructure
docker compose up -d postgres redis kafka

# Run backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run frontend (in another terminal)
cd springforge-frontend
npm install
npm start
```

## Project Structure

| Directory | Description |
|-----------|-------------|
| `src/` | Spring Boot backend |
| `springforge-frontend/` | Angular 18 frontend |
| `springforge-cli/` | Go CLI tool |
| `springforge-intellij-plugin/` | IntelliJ plugin |
| `infra/` | Infrastructure (K8s, monitoring) |
| `docs/` | API documentation |

## Code Style

### Backend (Java)
- Follow standard Java conventions
- Use constructor injection (no `@Autowired` on fields)
- Records for DTOs, entities with JPA annotations
- Service layer handles business logic, controllers are thin

### Frontend (Angular)
- Standalone components only (no NgModules)
- OnPush change detection where possible
- Reactive forms for user input
- Services for HTTP communication

### Tests
- Unit tests: JUnit 5 + Mockito + AssertJ
- E2E tests: Playwright
- Aim for meaningful coverage, not 100%

## Git Workflow

### Branch Naming
- `feat/description` — New features
- `fix/description` — Bug fixes
- `docs/description` — Documentation
- `refactor/description` — Code refactoring
- `test/description` — Test additions

### Commit Messages
Follow conventional commits:
```
type: short description

Optional longer description.
```

Types: `feat`, `fix`, `docs`, `test`, `refactor`, `perf`, `infra`, `ci`

### Pull Requests
1. Create a feature branch from `main`
2. Make your changes with meaningful commits
3. Ensure tests pass locally
4. Open a PR with description of changes
5. Wait for CI to pass and review

## Database Migrations

Use Flyway with sequential versioning:
```
src/main/resources/db/migration/V{N}__description.sql
```

Never modify existing migrations. Always create new ones.

## API Guidelines

- RESTful endpoints under `/api/v1/`
- Use proper HTTP status codes
- Return JSON responses
- Document with OpenAPI annotations (`@Operation`, `@ApiResponse`)
- Validate inputs at controller level

## Reporting Issues

- Use GitHub Issues
- Include steps to reproduce
- Include expected vs actual behavior
- Include relevant logs or screenshots
