# SpringForge - Design Specification

> Intelligent enterprise-grade Spring Boot project generator with Angular frontend.

## 1. Problem Statement

Setting up a new Spring Boot microservice with proper architecture, security, messaging,
Docker, CI/CD takes 2-5 days per project. SpringForge generates production-ready projects
in < 60 seconds via a 10-step wizard, enforcing architectural blueprints.

## 2. Architecture: Modular Monolith (Spring Modulith)

Single deployable with enforced module boundaries. Same hexagonal/DDD internal structure
as microservices. Can be split later by extracting modules.

Modules: shared (kernel), user, template, blueprint, generator.
Each module: domain/ application/ infrastructure/ api/ (hexagonal).

## 3. Generation Pipeline (async, 4 phases)

1. VALIDATE - JSON Schema validation, permission check
2. RESOLVE - Dependency resolution, conflict detection, version compatibility
3. GENERATE - Freemarker template rendering, file tree assembly
4. POST-PROCESS - Code formatting, compile check, ZIP packaging

POST returns 202 + generationId. Poll status. Download when COMPLETED.

## 4. Tech Stack

Backend: Java 21, Spring Boot 3.3.x, Spring Modulith 1.2.x, PostgreSQL 16, Flyway,
Freemarker 2.3.33, Spring Security 6 + JWT (jjwt 0.12.x), MapStruct 1.6.x, Maven.
Tests: JUnit 5, Mockito, Testcontainers, ArchUnit.
Frontend: Angular 18, Angular Material, pnpm. Docker multi-stage.

## 5. Output Matrix

Java: 11/17/21/25. Spring Boot: 2.7.x-3.3.x. Build: Maven/Gradle(Groovy/Kotlin).
Architecture: Monolithic/Layered/Hexagonal/DDD/CQRS/Microservices/Modulith.
DB: PostgreSQL/MySQL/MongoDB/H2. Messaging: None/Kafka/RabbitMQ.
Security: None/JWT/OAuth2. CI: None/GitHub Actions/GitLab CI.
Container: None/Dockerfile/Compose/K8s.

## 6. Blueprint System

YAML presets with architectural constraints and defaults.
MVP: Hexagonal, DDD, Layered, Microservices.

## 7. Template Hierarchy

Core > Organization > Team > Personal (lower overrides higher).
Engine: Freemarker only (handles simple + complex cases).

## 8. Angular Wizard (10 steps)

1-Metadata, 2-Versions, 3-Build Tool, 4-Architecture, 5-Modules,
6-Dependencies, 7-Security, 8-Infrastructure, 9-Options, 10-Review+Generate.
State: Angular Signals.

## 9. API (REST v1)

Auth: register/login/refresh/logout.
Blueprints: list/get. Templates: CRUD(ADMIN).
Projects: CRUD + generate(async 202). Generations: status/download.
Versions: java/spring-boot. Dependencies: catalog.

## 10. Out of Scope (MVP)

Go CLI, multi-tenant, Kafka bus, MongoDB/Redis/MinIO, Keycloak,
Grafana stack, K8s for SpringForge, SaaS/billing, AI suggestions.

## 11. Success Criteria

100% generated projects compile. 4+ architecture types. <60s generation.
Full wizard. JWT+RBAC. >80% coverage on generator. Docker Compose dev.
