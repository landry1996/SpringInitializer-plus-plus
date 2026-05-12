# Contributing to ${projectName}

## Getting Started

1. Clone the repository
2. Install prerequisites: JDK ${javaVersion}, Docker
3. Run `docker-compose up -d` to start dependencies
<#if buildTool?? && buildTool?contains("GRADLE")>
4. Run `./gradlew bootRun` to start the application
<#else>
4. Run `./mvnw spring-boot:run` to start the application
</#if>

## Development Workflow

### Branch Naming

- `feature/<ticket-id>-short-description`
- `fix/<ticket-id>-short-description`
- `hotfix/<ticket-id>-short-description`
- `chore/<description>`

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]
[optional footer]
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `perf`

### Pull Request Process

1. Create a feature branch from `develop`
2. Make your changes with tests
3. Ensure all checks pass:
<#if buildTool?? && buildTool?contains("GRADLE")>
   ```bash
   ./gradlew check
   ```
<#else>
   ```bash
   ./mvnw clean verify
   ```
</#if>
4. Open a PR targeting `develop`
5. Request at least 1 reviewer
6. Squash and merge after approval

### Code Standards

- Follow existing code style (see `.editorconfig`)
- Maximum method length: 50 lines
- Maximum file length: 500 lines
- All public APIs must have tests
- Coverage must not decrease

## Architecture

<#if architecture??>
This project follows **${architecture}** architecture.
</#if>

See `docs/adr/` for Architecture Decision Records.

## Testing

```bash
<#if buildTool?? && buildTool?contains("GRADLE")>
# Unit tests
./gradlew test

# Integration tests
./gradlew integrationTest

# All tests
./gradlew check
<#else>
# Unit tests
./mvnw test

# Integration tests
./mvnw verify -P integration-tests

# All tests
./mvnw clean verify
</#if>
```
