# SpringForge — Document Technique

## 1. Vue d'ensemble de l'architecture

SpringForge est un générateur de projets Spring Boot enterprise-grade, construit en architecture **modular monolith** avec Spring Modulith. Le système se compose de 4 composants principaux :

- **Backend** : Java 21 + Spring Boot 3.3.5 (API REST)
- **Frontend** : Angular 18 (SPA standalone components)
- **CLI** : Go + Cobra (outil en ligne de commande)
- **Plugin IntelliJ** : Java + IntelliJ Platform SDK
- **Extension VS Code** : TypeScript + Webview API
- **Module IA** : Intégration LLM (Claude, OpenAI) avec streaming SSE

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
| Object Storage | MinIO (S3-compatible) | 8.5.13 |
| Paiement | Stripe Java SDK | 26.1.0 |
| LLM Client | Spring WebFlux (WebClient) | intégré |
| MongoDB (optionnel) | Spring Data MongoDB | intégré |
| Migrations MongoDB | Mongock | 5.x |
| MySQL (optionnel) | mysql-connector-j | 8.x |
| Email | Spring Boot Starter Mail | intégré |

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

### 2.3 Extension VS Code

| Couche | Technologie | Version |
|--------|-------------|---------|
| Runtime | Node.js | 20+ |
| Langage | TypeScript | 5.x |
| Framework | VS Code Extension API | 1.85+ |
| UI | Webview (HTML/CSS/JS) | — |
| HTTP Client | fetch API | — |
| Build | tsc | — |

### 2.4 Infrastructure

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
| MinIO | minio/minio:latest | 9000/9001 |
| Prometheus | prom/prometheus:latest | 9090 |
| Grafana | grafana/grafana:latest | 3000 |

---

## 3. Architecture applicative

### 3.1 Structure hexagonale des modules

```
springforge-backend/src/main/java/com/springforge/
├── shared/              # Module noyau partagé
│   ├── config/
│   │   ├── AsyncConfig.java            # Thread pool génération (3 core, 5 max)
│   │   ├── CorsConfig.java            # CORS configurable via env
│   │   ├── FreemarkerConfig.java       # Configuration templates
│   │   ├── OpenApiConfig.java          # Swagger/OpenAPI
│   │   └── WebSocketConfig.java        # WebSocket STOMP
│   └── security/
│       ├── SecurityConfig.java         # Spring Security, CORS, CSP, HSTS
│       ├── JwtAuthenticationFilter.java # Filtre JWT
│       ├── JwtService.java             # Génération/validation tokens
│       ├── RateLimitingFilter.java     # 60 req/min par client
│       └── LoginAttemptService.java    # Protection brute-force
├── config/              # Configurations additionnelles (P5)
│   ├── CacheConfig.java            # Redis cache manager multi-TTL
│   ├── DatabaseConfig.java         # HikariCP tuning (pool 5-20)
│   └── CompressionConfig.java      # Gzip HTTP (seuil 1Ko)
├── security/            # Sécurité additionnelle (P5)
│   ├── SecurityHeadersFilter.java  # Headers OWASP complets
│   └── InputSanitizer.java         # Validation noms, packages, versions
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
├── generator/           # Pipeline de génération (25 fichiers)
│   ├── api/             # Controller REST
│   ├── application/     # Use cases, validators
│   ├── domain/          # Entités, statuts, pipeline
│   └── infrastructure/  # Persistance JPA
├── storage/             # Stockage objets (MinIO / FileSystem)
│   ├── StorageService.java          # Interface abstraite
│   ├── FileSystemStorageService.java # Implémentation locale (dev)
│   ├── MinioStorageService.java      # Implémentation MinIO (prod)
│   └── StorageCleanupScheduler.java  # Rétention 30 jours
├── billing/             # Facturation Stripe
│   ├── domain/          # Subscription, Invoice, Plans
│   ├── infrastructure/  # Repositories JPA
│   ├── application/     # BillingService (checkout, portal, webhooks)
│   └── api/             # BillingController, StripeWebhookController
├── ai/                  # Intégration LLM (Claude, OpenAI)
│   ├── domain/          # LlmProvider, LlmRequest, LlmResponse, LlmService
│   ├── infrastructure/  # ClaudeLlmService, OpenAiLlmService
│   ├── application/     # AiAssistantService (review, suggest, generate)
│   └── api/             # AiController (REST + SSE streaming)
├── notification/        # Webhooks & Notifications
│   ├── domain/          # WebhookConfig, DeliveryLog, EventType, Channel
│   ├── infrastructure/  # WebhookConfigRepository, DeliveryLogRepository
│   ├── application/     # NotificationService (dispatch, retry, HMAC)
│   └── api/             # WebhookController (CRUD, test, deliveries)
├── user/                # Authentification (JWT, refresh tokens)
├── blueprint/           # Définitions d'architecture
├── template/            # Templates Freemarker
├── project/             # Projets utilisateurs
└── preset/              # Presets de configuration
```

### 3.2 Pipeline de génération (4 phases)

```
POST /api/v1/projects/generate → 202 Accepted + generationId

Phase 1: VALIDATE    → JSON Schema, permissions, quotas
Phase 2: RESOLVE     → Résolution dépendances, détection conflits
Phase 3: GENERATE    → Rendu Freemarker, assemblage arborescence
Phase 4: POST-PROCESS → Formatage code, vérification compilation, ZIP → Upload MinIO
```

Exécution asynchrone via `@Async("generationExecutor")` avec ThreadPool (3 core, 5 max).

### 3.3bis Stockage des artefacts (MinIO)

Le ZIP généré est uploadé dans MinIO (S3-compatible) plutôt que conservé sur le filesystem :

```
POST-PROCESS → ZIP créé localement → Upload vers bucket `springforge-generations`
                                    → Suppression fichier local
                                    → objectKey stocké en base (generations.object_key)

Download    → GET /api/v1/generations/{id}/download
            → StorageService.download(objectKey)
            → StreamingResponseBody vers le client
```

Configuration conditionnelle via `@ConditionalOnProperty(name = "storage.type")` :
- `filesystem` (défaut, dev) : stockage dans `./generated-projects/`
- `minio` (prod) : stockage S3-compatible avec URLs pré-signées

Rétention : `StorageCleanupScheduler` supprime les fichiers > 30 jours (cron quotidien 3h).

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
| subscriptions, invoices | V10 | Billing Stripe |
| webhook_configs, webhook_events, delivery_logs | V11 | Notifications |

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
| generationExecutor | 3 | 5 | 25 | Génération projets |

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

### 7.1 Modes de déploiement

| Mode | Fichier | Usage |
|------|---------|-------|
| Local (dev) | `docker-compose.yml` | Développement, 9 services complets |
| VPS (prod) | `docker-compose.prod.yml` | Production, 5 services + Nginx HTTPS |
| Kubernetes | `infra/k8s/*.yml` | Production haute disponibilité |

### 7.2 Docker Compose Local (développement)

10 services orchestrés : backend, frontend, PostgreSQL, Redis, MinIO, Kafka, Zookeeper, Schema Registry, Keycloak, Prometheus, Grafana.

```bash
docker compose up -d
# Frontend: http://localhost:4200 | API: http://localhost:8080
```

### 7.3 Docker Compose VPS (production)

5 services : Nginx (reverse proxy HTTPS), backend, frontend, PostgreSQL, Redis.
Kafka et Keycloak sont optionnels (désactivés par défaut pour économiser la RAM).

```bash
cp .env.example .env  # Configurer les variables
./deploy.sh init      # Premier déploiement
./deploy.sh ssl domaine.com email@ex.com  # Activer HTTPS
```

| Paramètre | Configuration |
|-----------|--------------|
| Reverse proxy | Nginx avec TLS Let's Encrypt (auto-renew) |
| Backend | 1 Go RAM max, healthcheck Actuator |
| Frontend | 128 Mo RAM max, Nginx SPA routing |
| PostgreSQL | 512 Mo RAM max, healthcheck pg_isready |
| Redis | 192 Mo RAM max, LRU eviction 128 Mo |
| Backups | pg_dump quotidien, rotation 7 jours |

### 7.4 Kubernetes (production haute disponibilité)

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

### 7.5 CI/CD (GitHub Actions)

| Workflow | Trigger | Actions |
|----------|---------|---------|
| ci.yml | Push/PR | Build Maven, tests, build frontend, security scan |
| release.yml | Tag v* | Build Docker images, push GHCR, deploy staging |
| deploy-prod.yml | Manual | Validation + déploiement production |
| publish-extensions.yml | Tag ext-v* / Manual | Build + publish VS Code & IntelliJ extensions |

### 7.6 Configuration conditionnelle

Les services Kafka et Keycloak sont optionnels en production :

| Variable | Défaut | Effet |
|----------|--------|-------|
| `KAFKA_ENABLED` | false | Désactive l'auto-configuration Kafka |
| `KEYCLOAK_ENABLED` | false | Désactive la validation JWT via Keycloak |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | (vide) | Pas de connexion Kafka |
| `KEYCLOAK_ISSUER_URI` | (vide) | Auth JWT locale uniquement |

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
| Billing | 1 fichier (BillingServiceTest) | Checkout, webhook handlers, trial |
| AI | 1 fichier (AiAssistantServiceTest) | Review, suggest, generate, stream |
| Notification | 1 fichier (NotificationServiceTest) | Dispatch, retry, test webhook |
| Storage | 1 fichier (StorageCleanupSchedulerTest) | Cleanup, skip null, continue on error |

### 8.2 Tests E2E (Playwright)

| Suite | Scénarios |
|-------|-----------|
| wizard.spec.ts | Flux complet step 1→6→génération |
| marketplace.spec.ts | Recherche, filtrage, notation |
| admin.spec.ts | Dashboard, users, audit |
| i18n.spec.ts | Changement locale, traductions |
| organization.spec.ts | Settings, membres, API keys |
| billing.spec.ts | Plans, upgrade, invoices |
| webhooks.spec.ts | Create, form validation, test, list |
| ai-chat.spec.ts | Chat interface, send message, streaming |

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

### 9.7 Billing (Stripe)
- `GET /api/v1/billing/subscription` — Abonnement courant de l'organisation
- `POST /api/v1/billing/checkout` — Créer session Stripe Checkout
- `POST /api/v1/billing/portal` — Ouvrir portail client Stripe
- `GET /api/v1/billing/invoices` — Historique factures
- `POST /api/v1/webhooks/stripe` — Réception événements Stripe (permitAll)

### 9.8 Intelligence Artificielle (LLM)
- `POST /api/v1/ai/review` — Code review IA du projet généré
- `POST /api/v1/ai/suggest` — Suggestions d'amélioration architecture
- `POST /api/v1/ai/generate` — Génération de code boilerplate
- `POST /api/v1/ai/chat` — Chat assistant avec streaming SSE

### 9.9 Webhooks & Notifications
- `GET /api/v1/organizations/{orgId}/webhooks` — Lister webhooks configurés
- `POST /api/v1/organizations/{orgId}/webhooks` — Créer webhook
- `PUT /api/v1/organizations/{orgId}/webhooks/{id}` — Modifier webhook
- `DELETE /api/v1/organizations/{orgId}/webhooks/{id}` — Supprimer webhook
- `POST /api/v1/organizations/{orgId}/webhooks/{id}/test` — Tester un webhook
- `GET /api/v1/organizations/{orgId}/webhooks/{id}/deliveries` — Historique envois (paginé)

---

## 10. Diagramme des dépendances inter-modules

```
┌──────────────────────────────────────────────────────────────────┐
│                       shared/security                             │
│    (JWT, RateLimit, CORS, CSP, HSTS, LoginAttempt)               │
└───────────────────────────┬──────────────────────────────────────┘
                            │ protège
┌───────────────────────────▼──────────────────────────────────────┐
│                   security/ + config/                             │
│    (SecurityHeaders, InputSanitizer, Cache, DB, Gzip)            │
└───────────────────────────┬──────────────────────────────────────┘
                            │
┌───────────────────────────▼──────────────────────────────────────┐
│                        API Layer                                  │
│  (Controllers : Recommendation, Marketplace, Admin, Organization,│
│   I18n, Generator, Auth, Billing, AI, Webhook)                   │
└──┬──────┬───────┬───────┬──────┬──────┬──────┬──────┬───────────┘
   │      │       │       │      │      │      │      │
┌──▼──┐ ┌─▼───┐ ┌▼────┐ ┌▼───┐ ┌▼───┐ ┌▼───┐ ┌▼──┐ ┌▼──────────┐
│reco │ │mark │ │admin│ │ten │ │gen │ │bill│ │ai │ │notification│
│mend │ │etpl │ │     │ │ant │ │era │ │ing │ │   │ │            │
└──┬──┘ └──┬──┘ └──┬──┘ └─┬──┘ └─┬──┘ └─┬──┘ └┬──┘ └─────┬─────┘
   │       │       │      │      │      │     │         │
┌──▼───────▼───────▼──────▼──────▼──────▼─────▼─────────▼─────────┐
│  PostgreSQL + Redis + MinIO (+ Kafka optionnel)                   │
│  + Stripe API + Claude/OpenAI API (externes)                     │
└──────────────────────────────────────────────────────────────────┘
```

---

## 11. Fichiers de déploiement

| Fichier | Rôle |
|---------|------|
| `Dockerfile` | Image backend multi-stage (build Maven → JRE Alpine) |
| `springforge-frontend/Dockerfile` | Image frontend multi-stage (Node build → Nginx) |
| `springforge-frontend/nginx.conf` | Proxy API + WebSocket + SPA routing |
| `docker-compose.yml` | Stack dev complète (9 services) |
| `docker-compose.prod.yml` | Stack prod VPS (5 services + HTTPS) |
| `deploy.sh` | Script déploiement VPS (init, update, ssl, backup) |
| `.env.example` | Template variables d'environnement |
| `.dockerignore` | Exclusions build Docker |
| `infra/nginx/nginx.conf` | Config Nginx reverse proxy prod |
| `infra/nginx/conf.d/default.conf` | Vhost HTTPS + routing |
| `infra/nginx/conf.d/default-nossl.conf.example` | Vhost HTTP (premier démarrage) |
| `infra/prometheus/prometheus.yml` | Scraping métriques Actuator |
| `infra/k8s/*.yml` | 12 manifestes Kubernetes |
| `infra/monitoring/*.yml` | ServiceMonitor, alertes, dashboards |
| `springforge-vscode/` | Extension VS Code (TypeScript) |
| `springforge-vscode/package.json` | Manifest extension, commandes, settings |
| `springforge-vscode/src/extension.ts` | Point d'entrée, activation, status bar |
| `springforge-vscode/src/client.ts` | Client HTTP vers l'API SpringForge |
| `springforge-vscode/src/panel.ts` | Wizard multi-step webview |

---

## 12. Intégrations externes (P6)

### 12.1 MinIO (Object Storage S3-compatible)

| Paramètre | Valeur |
|-----------|--------|
| Bucket | `springforge-generations` |
| Port API | 9000 |
| Port Console | 9001 |
| Rétention | 30 jours |
| Implémentation | `MinioStorageService` (@ConditionalOnProperty) |
| Fallback dev | `FileSystemStorageService` (matchIfMissing=true) |

### 12.2 Stripe (Paiement)

| Composant | Description |
|-----------|-------------|
| SDK | com.stripe:stripe-java:26.1.0 |
| Checkout | Session de paiement hébergée Stripe |
| Portal | Portail client pour gérer l'abonnement |
| Webhooks | `checkout.session.completed`, `invoice.paid`, `invoice.payment_failed`, `customer.subscription.deleted` |
| Plans | FREE (0€), PRO (29€/mois), ENTERPRISE (99€/mois) |
| Sécurité | Signature webhook vérifiée via Stripe SDK |

### 12.3 LLM (Intelligence Artificielle)

| Paramètre | Claude | OpenAI |
|-----------|--------|--------|
| Provider | `@ConditionalOnProperty(ai.provider=claude)` | `@ConditionalOnProperty(ai.provider=openai)` |
| Client HTTP | Spring WebClient (non-bloquant) | Spring WebClient (non-bloquant) |
| Streaming | SSE via Flux<String> | SSE via Flux<String> |
| Modèle défaut | claude-sonnet-4-20250514 | gpt-4o |
| Endpoints | /api/v1/ai/review, /suggest, /generate, /chat | Idem |

### 12.4 Webhooks & Notifications

| Composant | Description |
|-----------|-------------|
| Canaux | WEBHOOK (HTTP POST), SLACK (Incoming Webhook), EMAIL |
| Événements | GENERATION_COMPLETED, GENERATION_FAILED, QUOTA_WARNING, QUOTA_EXCEEDED, SUBSCRIPTION_CHANGED, MEMBER_JOINED |
| Retry | 3 tentatives, backoff exponentiel (2^attempt minutes) |
| Signature | HMAC-SHA256 (`X-Webhook-Signature: sha256=...`) |
| Dispatch | @Async (non-bloquant) |
| Scheduler | Retry toutes les 60s (@Scheduled) |

### 12.5 MongoDB (Support générateur)

| Composant | Description |
|-----------|-------------|
| Templates | MongoConfig, MongoDocument, MongoRepository, MongockConfig, InitialMigration |
| Dépendances auto | spring-boot-starter-data-mongodb, mongock, embedded-mongo (test) |
| Docker | MongoDB 7 avec healthcheck |
| Tests | Testcontainers MongoDB |
| Détection | `hasMongoDB()` dans GenerateStep |

### 12.6 MySQL (Support générateur)

| Composant | Description |
|-----------|-------------|
| Templates | `application-mysql.yml.ftl` (datasource, JPA dialect, Flyway), `docker-compose-mysql.yml.ftl` |
| Dépendances auto | mysql-connector-j, spring-boot-starter-data-jpa |
| Docker | MySQL 8.0 avec healthcheck (`mysqladmin ping`) |
| Config | Port 3306, dialect `MySQLDialect`, Flyway migrations |
| Détection | `hasMySQL()` dans GenerateStep |

### 12.7 Période d'essai PRO (Trial)

| Composant | Description |
|-----------|-------------|
| Entité | `Subscription` : champs `trial` (boolean), `trialEndsAt` (LocalDateTime) |
| Création | `Subscription.createProTrial(userId)` : 14 jours PRO à l'inscription |
| Expiration | `BillingService.expireTrials()` : cron horaire, downgrade vers FREE |
| Conversion | `Subscription.convertFromTrial()` : retire le flag trial lors du paiement Stripe |
| Frontend | Bannière trial avec compte à rebours + bouton "Subscribe Now" |
| Repository | `findByTrialTrueAndTrialEndsAtBefore(LocalDateTime)` |

### 12.8 Email Notifications (SMTP)

| Composant | Description |
|-----------|-------------|
| Classe | `EmailNotificationSender` (@ConditionalOnProperty `notification.email.enabled=true`) |
| Dépendance | `spring-boot-starter-mail` (JavaMailSender) |
| Format | HTML MimeMessage (template inline avec formatSubject + buildHtmlBody) |
| Config | SMTP host/port/username/password via variables d'environnement |
| Intégration | Canal `EMAIL` dans `NotificationService.deliver()` |
| Null-safe | `@Autowired(required = false)` — service absent si email désactivé |

### 12.9 Publication Extensions (Marketplace)

| Composant | Description |
|-----------|-------------|
| Workflow | `.github/workflows/publish-extensions.yml` |
| Trigger | Tag `ext-v*` ou `workflow_dispatch` (choix vscode/intellij/all) |
| VS Code | `vsce package` + `vsce publish` + Open VSX Registry |
| IntelliJ | `gradlew buildPlugin` + `gradlew verifyPlugin` + `gradlew publishPlugin` |
| Secrets | `VSCE_PAT`, `OVSX_PAT`, `JETBRAINS_MARKETPLACE_TOKEN` |
| Artefacts | Upload .vsix et .zip comme artifacts GitHub |
