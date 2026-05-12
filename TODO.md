# SpringForge — TODO

> Travail bloc par bloc. Chaque bloc est coché une fois terminé et poussé.

---

## Phase 1 — Foundations

- [x] **BLOC 1** — Project Scaffolding
  - Spring Boot 3.3.5 + Spring Modulith 1.2.6 + Maven
  - Module structure (shared, user, template, blueprint, generator)
  - PostgreSQL 16 + Flyway configuration
  - Docker Compose (PostgreSQL dev)
  - application.yml profiles (dev, test, prod)
  - ~~Angular 18 project initialization~~ (déplacé Phase 3)

- [x] **BLOC 2** — Shared Kernel & Security
  - Base domain classes (BaseEntity with UUID, DomainEvent interface)
  - Global exception handler (ApiError record, no stacktrace)
  - Security config (JWT HS512, BCrypt 12, CORS strict)
  - Auth DTOs + Bean Validation (@NotBlank, @Email, @Size)
  - Security headers (HSTS, X-Frame-Options DENY, Content-Type-Options)
  - Freemarker Configuration bean (templates loading from classpath)
  - Async thread pool (generationExecutor: 3 core, 5 max, queue 25)

- [x] **BLOC 3** — User Module
  - User entity + Role enum (ADMIN, USER)
  - Register / Login / Refresh / Logout use cases
  - JWT service (access 15min + refresh 7 days)
  - Refresh token rotation (revoke old, issue new)
  - AuthController (/api/v1/auth/**)
  - Flyway migration V1 (users, refresh_tokens + indexes)
  - Tests unitaires + intégration (AuthControllerTest)

---

## Phase 2 — Core Engine

- [x] **BLOC 4** — Blueprint Module
  - Blueprint domain (entity, ArchitectureType enum, constraints/defaults/structure JSON)
  - BlueprintDataInitializer (ApplicationRunner, charge JSON au startup, idempotent)
  - Built-in blueprints JSON: Hexagonal, DDD, Layered, Microservices
  - BlueprintController (GET /api/v1/blueprints, GET /api/v1/blueprints/{id}, filtre par type)
  - Flyway migration V2 (blueprints table + index on type)

- [x] **BLOC 5** — Template Module
  - Template domain (entity, TemplateScope enum CORE/CUSTOM, version, blueprintType)
  - Freemarker integration (Configuration bean + renderTemplate dans GenerateStep)
  - Template CRUD use cases (CreateTemplate, ListTemplates, GetTemplate)
  - Core templates FTL: pom.xml, Application.java, application.yml, Dockerfile, .gitignore, docker-compose.yml
  - Architecture templates: hexagonal (DomainEntity, Repository, Controller), layered (Controller, Service, Repository)
  - TemplateController (GET list, GET by id, POST create @PreAuthorize ADMIN)
  - Flyway migration V4 (templates table + indexes)

- [x] **BLOC 6** — Generator Module (Pipeline)
  - GenerateRequest record / ProjectConfiguration schema (Metadata, Architecture, SecurityConfig, InfrastructureConfig, GenerationOptions)
  - Pipeline complet: ValidateStep → ResolveStep → GenerateStep → PostProcessStep
  - Async execution (@Async("generationExecutor"))
  - Generation entity avec status tracking (QUEUED → IN_PROGRESS → COMPLETED/FAILED)
  - ZIP packaging (PostProcessStep: zip + cleanup source dir)
  - GeneratorController (POST /projects/generate → 202, GET /generations/{id}/status, GET /generations/{id}/download)
  - Flyway migration V3 (generations table + indexes)
  - Tests (GenerationPipelineTest: validate + resolve steps)

---

## Phase 3 — Frontend Wizard

- [x] **BLOC 7** — Angular App Shell
  - Routing (lazy-loaded feature modules)
  - Core module (auth service, HTTP interceptor, guards)
  - Shared components (stepper, form controls)
  - Login / Register pages
  - Auth state management (signals)

- [x] **BLOC 8** — Wizard (Steps 1-5)
  - Step 1: Project Metadata form
  - Step 2: Java & Spring Boot version selector
  - Step 3: Build tool choice
  - Step 4: Architecture / Blueprint selector (cards)
  - Step 5: Module definition (dynamic list)
  - Wizard state service (signals)

- [x] **BLOC 9** — Wizard (Steps 6-10) + Generation
  - Step 6: Dependency picker (categorized, searchable)
  - Step 7: Security configuration
  - Step 8: Infrastructure toggles
  - Step 9: Options
  - Step 10: Review summary + Generate button
  - Generation progress indicator + download

---

## Phase 4 — Polish & Production

- [x] **BLOC 10** — Architecture Templates (compléter)
  - Hexagonal architecture templates (full set: UseCase, UseCaseImpl, JpaAdapter, package-info)
  - DDD architecture templates (Aggregate, ValueObject, DomainEvent, Repository, Command, Query, CommandHandler, package-info)
  - Microservices scaffold templates (service-registry, api-gateway, service template, docker-compose)
  - Template variables documentation (TEMPLATE_VARIABLES.md)

- [x] **BLOC 11** — Security Hardening
  - Rate limiting (RateLimitingFilter: 20 req/min sur /auth endpoints)
  - Brute force protection (LoginAttemptService: 5 tentatives, lock 15min)
  - Correlation ID filter (X-Request-ID header + MDC)
  - Audit logging aspect (AOP sur POST/PUT/DELETE)
  - Payload size limits (10MB max via multipart config)
  - Security headers (HSTS, X-Frame-Options DENY, Content-Type-Options)

- [x] **BLOC 12** — Testing & Documentation
  - ArchUnit rules (domain→infra, domain→api, application→infra, application→api)
  - Integration tests (AuthControllerTest, BlueprintControllerTest, TemplateControllerTest, GeneratorControllerTest)
  - API documentation (SpringDoc OpenAPI 2.6 + Swagger UI)
  - README.md (Quick Start, API endpoints, Architecture)
  - Docker multi-stage build (Dockerfile + docker-compose.prod.yml)
  - .env.example pour les variables de production

---

## Status

| Bloc | Status | Date |
|------|--------|------|
| 1 | DONE | 2026-05-08 |
| 2 | DONE | 2026-05-12 |
| 3 | DONE | 2026-05-08 |
| 4 | DONE | 2026-05-12 |
| 5 | DONE | 2026-05-12 |
| 6 | DONE | 2026-05-12 |
| 7 | DONE | 2026-05-12 |
| 8 | DONE | 2026-05-12 |
| 9 | DONE | 2026-05-12 |
| 10 | DONE | 2026-05-12 |
| 11 | DONE | 2026-05-12 |
| 12 | DONE | 2026-05-12 |

---

## Notes d'implémentation (2026-05-12)

### Backend complété — Production Ready :
- **Bloc 4** : BlueprintDataInitializer + 4 blueprints JSON
- **Bloc 5** : Module Template complet + migration V4 + templates FTL
- **Bloc 6** : Pipeline complet (GenerateStep + PostProcessStep + UseCase + Controller)
- **Bloc 11** : RateLimitingFilter (20 req/min auth), LoginAttemptService (5 tentatives, lock 15min), AuditLoggingAspect, CorrelationIdFilter
- **Bloc 12** : ArchUnit tests, Integration tests (Template, Generator, Blueprint controllers), OpenAPI/Swagger, README.md, Dockerfile multi-stage, docker-compose.prod.yml

### Frontend complété (2026-05-12) :
- **Bloc 7** : Angular 18 app shell — routing (lazy-loaded), auth service (signals), HTTP interceptor, auth guard, stepper component, login/register pages
- **Bloc 8** : Wizard Steps 1-5 — metadata form, version selector, build tool, architecture/blueprint cards, module definition
- **Bloc 9** : Wizard Steps 6-10 — dependency picker, security config, infrastructure toggles, options, review + generate button with async polling + ZIP download

### Bloc 10 complété (2026-05-12) :
- **Hexagonal** : UseCase, UseCaseImpl, JpaAdapter, package-info (full port & adapter pattern)
- **DDD** : Aggregate, ValueObject (ID), DomainEvent, Repository, Command, Query, CommandHandler, package-info
- **Microservices** : Service Registry (Eureka), API Gateway (Spring Cloud Gateway + Resilience4j), service scaffold, docker-compose multi-service
- **GenerateStep** : Updated to render all architecture templates per module
- **Documentation** : TEMPLATE_VARIABLES.md with all variables and structure

### Projet complété — Tous les blocs sont DONE
