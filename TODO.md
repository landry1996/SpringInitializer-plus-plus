# SpringForge — TODO

> Travail bloc par bloc. Chaque bloc est coché une fois terminé et poussé.

---

## Phase 1 — Foundations

- [ ] **BLOC 1** — Project Scaffolding
  - Spring Boot 3.3.x + Spring Modulith + Maven
  - Module structure (shared, user, template, blueprint, generator)
  - PostgreSQL + Flyway configuration
  - Docker Compose (PostgreSQL dev)
  - application.yml profiles (dev, test, prod)
  - Angular 18 project initialization (standalone components)

- [ ] **BLOC 2** — Shared Kernel & Security
  - Base domain classes (BaseEntity, DomainEvent)
  - Global exception handler (ApiError, no stacktrace)
  - Security config (JWT HS512, BCrypt 12, CORS)
  - Auth DTOs + Bean Validation
  - Security headers
  - Correlation ID filter

- [ ] **BLOC 3** — User Module
  - User entity + Role enum (ADMIN, USER)
  - Register / Login / Refresh / Logout use cases
  - JWT service (access + refresh token)
  - Refresh token rotation
  - UserController + @PreAuthorize
  - Flyway migration V1 (users, refresh_tokens)
  - Tests unitaires + intégration

---

## Phase 2 — Core Engine

- [ ] **BLOC 4** — Blueprint Module
  - Blueprint domain (entity, constraints, defaults)
  - YAML/JSON blueprint loader
  - Built-in blueprints: Hexagonal, DDD, Layered, Microservices
  - BlueprintController (GET list, GET by id)
  - Flyway migration V2 (blueprints)
  - Tests

- [ ] **BLOC 5** — Template Module
  - Template domain (entity, version, scope, hierarchy)
  - Freemarker integration (TemplateEngine adapter)
  - Template CRUD use cases
  - Core templates: pom.xml, Application.java, SecurityConfig, Dockerfile, .gitignore
  - TemplateController
  - Flyway migration V3 (templates)
  - Tests

- [ ] **BLOC 6** — Generator Module (Pipeline)
  - GenerationRequest / Project configuration schema
  - Pipeline: ValidateStep -> ResolveStep -> GenerateStep -> PostProcessStep
  - Async execution (@Async + CompletableFuture)
  - Generation status tracking (QUEUED, IN_PROGRESS, COMPLETED, FAILED)
  - ZIP packaging
  - GeneratorController (POST generate, GET status, GET download)
  - Flyway migration V4 (projects, generations)
  - Tests (unitaires + intégration pipeline)

---

## Phase 3 — Frontend Wizard

- [ ] **BLOC 7** — Angular App Shell
  - Routing (lazy-loaded feature modules)
  - Core module (auth service, HTTP interceptor, guards)
  - Shared components (stepper, form controls)
  - Login / Register pages
  - Auth state management (signals)

- [ ] **BLOC 8** — Wizard (Steps 1-5)
  - Step 1: Project Metadata form
  - Step 2: Java & Spring Boot version selector
  - Step 3: Build tool choice
  - Step 4: Architecture / Blueprint selector (cards)
  - Step 5: Module definition (dynamic list)
  - Wizard state service (signals)

- [ ] **BLOC 9** — Wizard (Steps 6-10) + Generation
  - Step 6: Dependency picker (categorized, searchable)
  - Step 7: Security configuration
  - Step 8: Infrastructure toggles
  - Step 9: Options
  - Step 10: Review summary + Generate button
  - Generation progress indicator + download

---

## Phase 4 — Polish & Production

- [ ] **BLOC 10** — Architecture Templates
  - Hexagonal architecture templates (full set)
  - DDD architecture templates
  - Layered architecture templates
  - Microservices scaffold templates
  - Template variables documentation

- [ ] **BLOC 11** — Security Hardening
  - Rate limiting (Bucket4j) on auth endpoints
  - Brute force protection (LoginAttemptService)
  - Input sanitization
  - IDOR protection (ResourceOwnershipChecker)
  - Audit logging aspect
  - Payload size limits

- [ ] **BLOC 12** — Testing & Documentation
  - ArchUnit rules (module boundaries, no domain->infra dependency)
  - Integration tests (Testcontainers)
  - Generated project compile verification tests
  - API documentation (OpenAPI/Swagger)
  - README.md
  - Docker multi-stage build (prod)

---

## Status

| Bloc | Status | Date |
|------|--------|------|
| 1 | TODO | - |
| 2 | TODO | - |
| 3 | TODO | - |
| 4 | TODO | - |
| 5 | TODO | - |
| 6 | TODO | - |
| 7 | TODO | - |
| 8 | TODO | - |
| 9 | TODO | - |
| 10 | TODO | - |
| 11 | TODO | - |
| 12 | TODO | - |
