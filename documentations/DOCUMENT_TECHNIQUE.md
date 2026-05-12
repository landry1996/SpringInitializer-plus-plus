# SpringForge — Document Technique

## 1. Vue d'ensemble de l'architecture

SpringForge est un générateur de projets Spring Boot enterprise-grade, construit en architecture **modular monolith** avec Spring Modulith. Le système se compose de 4 composants principaux :

- **Backend** : Java 21 + Spring Boot 3.3.5 (API REST)
- **Frontend** : Angular 18 (SPA standalone components)
- **CLI** : Go + Cobra (outil en ligne de commande)
- **Plugin IntelliJ** : Java + IntelliJ Platform SDK

---

## 2. Stack technique complète

### 2.1 Backend

| Couche | Technologie | Version |
|--------|-------------|---------|
| Runtime | Java | 21 (LTS) |
| Framework | Spring Boot | 3.3.5 |
| Modules | Spring Modulith | 1.2.6 |
| Base de données | PostgreSQL | 16 |
| Migrations | Flyway | intégré Spring Boot |
| Cache | Redis | 7 |
| Messaging | Apache Kafka | Confluent 7.5.0 |
| Auth | Keycloak + JWT | 23.0 |
| Templates | Freemarker | 2.3.33 |
| ORM | Hibernate / JPA | 6.x |
| Pool connexions | HikariCP | intégré |
| API docs | Springdoc OpenAPI | 2.x |
| Mapping | MapStruct | 1.6.x |

### 2.2 Frontend

| Couche | Technologie | Version |
|--------|-------------|---------|
| Framework | Angular | 18 |
| Composants | Standalone (sans NgModules) | — |
| State | Angular Signals | — |
| UI | Angular Material | 18.x |
| HTTP | HttpClient | — |
| i18n | Custom TranslateService + Pipe | — |
| Build | esbuild (via Angular CLI) | — |
| Serveur | Nginx Alpine | — |

### 2.3 Infrastructure

| Service | Image | Port |
|---------|-------|------|
| Backend | Multi-stage Java 21 | 8080 |
| Frontend | Multi-stage Node + Nginx | 4200 (→80) |
| PostgreSQL | postgres:16-alpine | 5432 |
| Redis | redis:7-alpine | 6379 |
| Kafka | confluentinc/cp-kafka:7.5.0 | 9092 |
| Zookeeper | confluentinc/cp-zookeeper:7.5.0 | 2181 |
| Schema Registry | confluentinc/cp-schema-registry:7.5.0 | 8081 |
| Keycloak | quay.io/keycloak/keycloak:23.0 | 8180 |
| Prometheus | prom/prometheus:latest | 9090 |
| Grafana | grafana/grafana:latest | 3000 |

---

## 3. Architecture applicative

### 3.1 Structure hexagonale des modules

```
src/main/java/com/springforge/
├── config/              # Configurations transversales
│   ├── OpenApiConfig.java          # Swagger/OpenAPI
│   ├── CacheConfig.java            # Redis cache manager
│   ├── AsyncConfig.java            # Thread pools async
│   ├── DatabaseConfig.java         # HikariCP tuning
│   └── CompressionConfig.java      # Gzip HTTP
├── security/            # Couche sécurité
│   ├── SecurityConfig.java         # Spring Security, CORS, CSP
│   ├── RateLimitingFilter.java     # 60 req/min par client
│   ├── SecurityHeadersFilter.java  # Headers OWASP
│   └── InputSanitizer.java         # Validation entrées
├── recommendation/      # Moteur IA de recommandations
│   ├── RecommendationType.java     # Enum (6 types)
│   ├── Recommendation.java         # Record DTO
│   ├── CompatibilityScore.java     # Score de compatibilité
│   ├── RecommendationRule.java     # Interface pluggable
│   ├── RecommendationService.java  # Agrégation des règles
│   ├── RecommendationController.java
│   └── rules/
│       ├── DependencyRecommendationRule.java
│       ├── ArchitectureRecommendationRule.java
│       ├── AntiPatternRule.java
│       └── SecurityRecommendationRule.java
├── marketplace/         # Catalogue de blueprints
│   ├── Blueprint.java              # Entité JPA
│   ├── BlueprintRepository.java    # Queries custom
│   ├── BlueprintService.java       # Logique métier
│   └── BlueprintController.java    # REST API
├── admin/               # Panel d'administration
│   ├── AdminUser.java              # Entité utilisateur admin
│   ├── AuditLog.java               # Entité audit
│   ├── GenerationStats.java        # Entité statistiques
│   ├── AdminUserRepository.java
│   ├── AuditLogRepository.java
│   ├── GenerationStatsRepository.java
│   ├── AdminUserService.java
│   ├── AuditService.java
│   ├── DashboardService.java       # Agrégation stats
│   └── AdminController.java
├── tenant/              # Multi-tenant SaaS
│   ├── Organization.java           # Entité organisation
│   ├── OrganizationMember.java     # Membres
│   ├── ApiKey.java                 # Clés API (SHA-256)
│   ├── SubscriptionPlan.java       # Enum FREE/PRO/ENTERPRISE
│   ├── TenantContextHolder.java    # ThreadLocal tenant
│   ├── TenantInterceptor.java      # Extraction tenant HTTP
│   ├── QuotaService.java           # Vérification quotas
│   ├── OrganizationService.java    # CRUD organisations
│   └── OrganizationController.java
├── i18n/                # Internationalisation
│   ├── LocaleConfig.java           # Resolver + MessageSource
│   ├── I18nService.java            # Messages par locale
│   └── I18nController.java         # API REST i18n
├── generator/           # Pipeline de génération
└── shared/              # Utilitaires communs
```

### 3.2 Pipeline de génération (4 phases)

```
POST /api/v1/projects/generate → 202 Accepted + generationId

Phase 1: VALIDATE    → JSON Schema, permissions, quotas
Phase 2: RESOLVE     → Résolution dépendances, détection conflits
Phase 3: GENERATE    → Rendu Freemarker, assemblage arborescence
Phase 4: POST-PROCESS → Formatage code, vérification compilation, ZIP
```

Exécution asynchrone via `@Async("generationExecutor")` avec ThreadPool (4 core, 16 max).

### 3.3 Modèle de données

#### Tables principales (Flyway migrations V1-V9)

| Table | Migration | Description |
|-------|-----------|-------------|
| users, roles | V1 | Utilisateurs et authentification |
| blueprints | V2 | Définitions d'architecture |
| templates | V3 | Templates Freemarker |
| generations | V4 | Historique des générations |
| dependencies | V5 | Catalogue de dépendances |
| audit_logs | V6 | Logs d'audit |
| marketplace_blueprints | V7 | Blueprints communautaires |
| admin_users, generation_stats | V8 | Administration |
| organizations, org_members, api_keys | V9 | Multi-tenant |

---

## 4. Sécurité

### 4.1 Authentification & Autorisation

- **Keycloak** comme Identity Provider (realm `springforge`)
- **JWT** stateless avec validation issuer URI
- **API Keys** : préfixe `sf_`, hash SHA-256 en base, rotation possible
- **RBAC** : ADMIN, USER, VIEWER

### 4.2 Protection des endpoints

| Mécanisme | Implémentation |
|-----------|---------------|
| Rate Limiting | 60 req/min par IP ou API key |
| CORS | Whitelist origins (localhost:4200, springforge.io) |
| CSP | `default-src 'self'; script-src 'self'; frame-ancestors 'none'` |
| HSTS | max-age 1 an, includeSubDomains |
| Headers | X-Content-Type-Options, X-Frame-Options, Referrer-Policy |
| Input Validation | Regex sur noms, packages, versions + anti path-traversal |
| XSS | Sanitize HTML, encoding output |

### 4.3 Gestion des secrets

- Secrets Kubernetes (base64, namespace isolé)
- Variables d'environnement (jamais en dur dans le code)
- API keys hashées en SHA-256 (jamais stockées en clair)

---

## 5. Performance

### 5.1 Cache Redis

| Cache | TTL | Contenu |
|-------|-----|---------|
| blueprints | 1h | Liste des blueprints |
| blueprintDetail | 15min | Détail d'un blueprint |
| popularBlueprints | 10min | Top blueprints |
| recommendations | 5min | Résultats recommandations |
| dependencies | 24h | Catalogue dépendances |
| i18nMessages | 12h | Messages par locale |
| organizationQuotas | 2min | Quotas organisation |

### 5.2 Pool de connexions (HikariCP)

| Paramètre | Valeur |
|-----------|--------|
| maximumPoolSize | 20 |
| minimumIdle | 5 |
| connectionTimeout | 30s |
| idleTimeout | 10min |
| maxLifetime | 30min |
| leakDetectionThreshold | 60s |

### 5.3 Async & Thread Pools

| Pool | Core | Max | Queue | Usage |
|------|------|-----|-------|-------|
| generationExecutor | 4 | 16 | 100 | Génération projets |
| notificationExecutor | 2 | 8 | 200 | Notifications async |

### 5.4 Compression HTTP

- Gzip activé pour : JSON, XML, HTML, JS, CSS, ZIP
- Seuil minimum : 1 Ko

---

## 6. Monitoring & Observabilité

### 6.1 Métriques (Prometheus)

- Scraping toutes les 15s sur `/actuator/prometheus`
- Métriques JVM : heap, GC, threads
- Métriques HTTP : request rate, latence, status codes
- Métriques métier : générations/s, erreurs, blueprints populaires
- Métriques infra : HikariCP connections, Kafka consumer lag

### 6.2 Alerting

| Alerte | Condition | Sévérité |
|--------|-----------|----------|
| BackendDown | up == 0 pendant 2min | CRITICAL |
| HighErrorRate | >5% erreurs 5xx | WARNING |
| HighResponseTime | p95 > 2s | WARNING |
| HighMemoryUsage | heap > 85% | WARNING |
| DBPoolExhausted | active/max > 90% | CRITICAL |
| GenerationFailure | >10% échecs | WARNING |
| KafkaConsumerLag | lag > 1000 pendant 10min | WARNING |

### 6.3 Dashboard Grafana

Panels : Request Rate, Response Time p95, Error Rate, Active Requests, JVM Heap, DB Connection Pool, Project Generations, GC Pauses, Kafka Consumer Lag.

---

## 7. Déploiement

### 7.1 Docker Compose (dev/staging)

9 services orchestrés avec dépendances et health checks.
Frontend servi via Nginx avec proxy reverse vers backend.

### 7.2 Kubernetes (production)

| Ressource | Configuration |
|-----------|--------------|
| Namespace | `springforge` |
| Backend Deployment | 2 replicas, probes (startup/readiness/liveness) |
| Frontend Deployment | 2 replicas, probes |
| HPA Backend | 2-10 pods (CPU 70%, Memory 80%) |
| HPA Frontend | 2-5 pods (CPU 75%) |
| Ingress | Nginx, TLS cert-manager, multi-host |
| PostgreSQL | PVC 10Gi, readinessProbe |
| Redis | 256Mo max, LRU eviction |
| Kafka + Zookeeper | Dedicated pods |

### 7.3 CI/CD (GitHub Actions)

| Workflow | Trigger | Actions |
|----------|---------|---------|
| ci.yml | Push/PR | Build Maven, tests, build frontend, security scan |
| release.yml | Tag v* | Build Docker images, push GHCR, deploy staging |
| deploy-prod.yml | Manual | Validation + déploiement production |

---

## 8. Tests

### 8.1 Tests unitaires (JUnit 5 + Mockito + AssertJ)

| Module | Fichiers de test | Couverture |
|--------|-----------------|------------|
| Recommendation | 5 fichiers (Service + 4 Rules) | Logique métier complète |
| Marketplace | 1 fichier (BlueprintService) | CRUD + search + rating |
| Admin | 2 fichiers (AdminUser + Audit) | Gestion users + audit |
| Tenant | 2 fichiers (Quota + Organization) | Quotas + API keys |
| i18n | 1 fichier (I18nService) | Messages + fallback locale |

### 8.2 Tests E2E (Playwright)

| Suite | Scénarios |
|-------|-----------|
| wizard.spec.ts | Flux complet step 1→6→génération |
| marketplace.spec.ts | Recherche, filtrage, notation |
| admin.spec.ts | Dashboard, users, audit |
| i18n.spec.ts | Changement locale, traductions |
| organization.spec.ts | Settings, membres, API keys |

---

## 9. API REST — Endpoints complets

### 9.1 Génération de projets
- `POST /api/v1/projects/generate` — Lancer génération (202)
- `POST /api/v1/projects/preview` — Prévisualisation structure
- `GET /api/v1/generations/{id}/status` — Statut génération
- `GET /api/v1/generations/{id}/download` — Télécharger ZIP

### 9.2 Recommandations
- `POST /api/v1/recommendations` — Obtenir recommandations IA
- `POST /api/v1/recommendations/score` — Score de compatibilité

### 9.3 Marketplace
- `GET /api/v1/marketplace/blueprints` — Lister/rechercher
- `GET /api/v1/marketplace/blueprints/{id}` — Détail blueprint
- `POST /api/v1/marketplace/blueprints` — Publier blueprint
- `POST /api/v1/marketplace/blueprints/{id}/rate` — Noter

### 9.4 Organisations
- `POST /api/v1/organizations` — Créer organisation
- `GET /api/v1/organizations/{id}` — Détails
- `POST /api/v1/organizations/{id}/api-keys` — Générer clé API
- `GET /api/v1/organizations/{id}/usage` — Usage/quotas

### 9.5 Admin
- `GET /api/v1/admin/dashboard` — Statistiques
- `GET /api/v1/admin/users` — Liste utilisateurs
- `GET /api/v1/admin/audit` — Logs d'audit

### 9.6 i18n
- `GET /api/v1/i18n/locales` — Locales supportées
- `GET /api/v1/i18n/messages/{locale}` — Messages par locale

---

## 10. Diagramme des dépendances inter-modules

```
┌─────────────────────────────────────────────────────┐
│                    security                          │
│  (RateLimit, CORS, CSP, Headers, InputSanitizer)    │
└──────────────────────┬──────────────────────────────┘
                       │ protège
┌──────────────────────▼──────────────────────────────┐
│                  API Layer                           │
│  (Controllers : Recommendation, Marketplace,        │
│   Admin, Organization, I18n, Generator)             │
└──────┬───────┬───────┬───────┬───────┬──────────────┘
       │       │       │       │       │
┌──────▼──┐ ┌──▼────┐ ┌▼─────┐ ┌▼────┐ ┌▼─────┐
│recommend│ │market │ │admin │ │tenant│ │i18n  │
│ation    │ │place  │ │      │ │      │ │      │
└────┬────┘ └───┬───┘ └──┬───┘ └──┬───┘ └──┬───┘
     │          │         │        │        │
┌────▼──────────▼─────────▼────────▼────────▼─────┐
│              PostgreSQL + Redis + Kafka           │
└──────────────────────────────────────────────────┘
```
