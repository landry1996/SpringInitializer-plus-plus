# Changelog

All notable changes to SpringForge are documented in this file.

## [Unreleased]

### Added
- **Recommendation Engine**: AI-powered dependency suggestions, architecture patterns, anti-pattern detection, security advisories
- **Blueprint Marketplace**: Community blueprint store with search, rating, versioning, and download tracking
- **IntelliJ Plugin**: Full IDE integration for project generation with settings and progress dialogs
- **Admin Panel**: Dashboard with analytics, user management, audit logs, blueprint approval
- **Multi-tenant SaaS**: Organization management, API key authentication, subscription plans (Free/Pro/Enterprise), quota enforcement
- **Internationalization**: Support for EN, FR, DE, ES with locale switcher
- **Unit Tests**: Comprehensive test suite for recommendation, marketplace, admin, tenant, and i18n services
- **E2E Tests**: Playwright tests for wizard, marketplace, admin, i18n, and organization flows
- **API Documentation**: OpenAPI/Swagger configuration with Postman collection
- **CI/CD Pipelines**: GitHub Actions for CI (build/test/security), release (Docker/GHCR), and production deployment
- **Docker Compose**: Full-stack orchestration (9 services) with frontend Dockerfile and nginx proxy
- **Kubernetes Manifests**: Production-ready K8s configs (deployments, services, ingress, HPA, PVC)
- **Monitoring & Alerting**: Prometheus ServiceMonitor, Grafana dashboards, alerting rules, AlertManager config
- **Security Hardening**: Rate limiting (60 req/min), CORS, CSP, HSTS, input sanitization, security headers
- **Performance**: Redis cache with per-domain TTL, HikariCP optimization, async thread pools, HTTP compression

## [1.0.0] - 2026-05-01

### Added
- Initial project generator with 4 architecture blueprints
- 10-step wizard with real-time validation
- Async generation pipeline (Validate → Resolve → Generate → Post-Process)
- JWT authentication with refresh token rotation
- Freemarker template engine
- PostgreSQL database with Flyway migrations
- Angular 18 frontend with standalone components
- Go CLI tool with Cobra
- WebSocket real-time progress tracking
