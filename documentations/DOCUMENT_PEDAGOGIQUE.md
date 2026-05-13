# SpringForge — Document Pédagogique

## Introduction

Ce document pédagogique a pour objectif de permettre à tout développeur (junior à senior) de comprendre, utiliser et contribuer au projet SpringForge. Il couvre les concepts architecturaux, les patterns de design utilisés, et fournit des guides pratiques pas à pas.

---

## Partie 1 : Concepts fondamentaux

### 1.1 Qu'est-ce qu'un générateur de projets ?

Un générateur de projets automatise la création de la structure initiale d'une application. Au lieu de copier-coller un projet existant et de le modifier manuellement, SpringForge :

1. **Collecte** les besoins via un wizard interactif
2. **Analyse** la cohérence de la configuration (dépendances, architecture)
3. **Génère** tous les fichiers nécessaires (code, config, tests, Docker, CI)
4. **Livre** un projet prêt à compiler et déployer

**Analogie** : SpringForge est à un projet Spring Boot ce qu'un architecte est à une maison — il produit les plans complets et cohérents à partir des besoins exprimés.

### 1.2 Architecture Modular Monolith

SpringForge utilise un **monolithe modulaire** (Spring Modulith) :

```
┌─────────────────────────────────────────────┐
│           SpringForge Application            │
├──────────┬──────────┬──────────┬────────────┤
│ recommend│ market   │  admin   │  tenant    │
│ ation    │ place    │          │            │
├──────────┴──────────┴──────────┴────────────┤
│              shared / config                  │
└─────────────────────────────────────────────┘
```

**Pourquoi ce choix ?**

| Microservices | Monolithe Modulaire |
|---------------|---------------------|
| Complexité réseau | Appels en mémoire (rapide) |
| Déploiement indépendant | Déploiement unique (simple) |
| Latence inter-services | Zéro latence entre modules |
| Orchestration K8s complexe | Un seul pod suffit |

Le monolithe modulaire donne la **structure** des microservices (isolation, interfaces claires) sans la **complexité opérationnelle**. Si le besoin de scaling indépendant émerge, on peut extraire un module en service dédié.

### 1.3 Architecture Hexagonale (Ports & Adapters)

Chaque module suit l'architecture hexagonale :

```
         ┌───────────────────────────┐
         │      API (Controller)      │ ← Adaptateur entrant
         └─────────────┬─────────────┘
                       │ appelle
         ┌─────────────▼─────────────┐
         │      SERVICE (Logique)     │ ← Domaine métier
         └─────────────┬─────────────┘
                       │ utilise
         ┌─────────────▼─────────────┐
         │    REPOSITORY (JPA/Redis)  │ ← Adaptateur sortant
         └───────────────────────────┘
```

**Principe** : Le domaine métier (service) ne connaît PAS les détails techniques (HTTP, SQL, Redis). Il définit des **interfaces** (ports) que les adaptateurs implémentent.

**Bénéfice** : On peut changer la base de données sans toucher à la logique métier.

### 1.4 Le pattern Rule Engine (Recommandations)

Le moteur de recommandations utilise le pattern **Strategy/Rule** :

```java
// Interface commune (contrat)
public interface RecommendationRule {
    List<Recommendation> evaluate(ProjectConfig config);
    String getRuleId();
}

// Implémentations spécialisées
@Component class DependencyRecommendationRule implements RecommendationRule { ... }
@Component class ArchitectureRecommendationRule implements RecommendationRule { ... }
@Component class AntiPatternRule implements RecommendationRule { ... }
@Component class SecurityRecommendationRule implements RecommendationRule { ... }
```

Le service agrège toutes les règles automatiquement (injection de liste Spring) :

```java
@Service
public class RecommendationService {
    private final List<RecommendationRule> rules; // Auto-injecté par Spring

    public List<Recommendation> getRecommendations(ProjectConfig config) {
        return rules.stream()
            .flatMap(rule -> rule.evaluate(config).stream())
            .sorted(byPriority())
            .toList();
    }
}
```

**Bénéfice** : Pour ajouter une nouvelle règle, il suffit de créer une classe `@Component` implémentant `RecommendationRule`. Aucune modification du service existant (Open/Closed Principle).

### 1.5 Le pattern Provider / ConditionalOnProperty

SpringForge utilise le pattern **Provider** pour basculer entre implémentations au runtime :

```java
// Interface commune
public interface StorageService {
    void upload(String key, InputStream data);
    InputStream download(String key);
}

// Implémentation A : activée par défaut (dev)
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "filesystem", matchIfMissing = true)
public class FileSystemStorageService implements StorageService { ... }

// Implémentation B : activée en prod
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "minio")
public class MinioStorageService implements StorageService { ... }
```

**Utilisé dans** :
- **Storage** : FileSystem vs MinIO (propriété `storage.type`)
- **LLM** : Claude vs OpenAI (propriété `ai.provider`)

**Bénéfice** : On change d'implémentation en modifiant UNE variable d'environnement, sans toucher au code. Le code appelant ne voit que l'interface.

### 1.6 Event-Driven Notifications (Observer pattern distribué)

Le système de webhooks utilise un pattern Observer distribué :

```
[Action métier] ─── dispatch() ──▶ [NotificationService]
                                         │
                    ┌────────────────────┼────────────────────┐
                    │                    │                    │
                    ▼                    ▼                    ▼
            [Webhook HTTP]       [Slack Webhook]       [Email SMTP]
                    │                    │                    │
                    ▼                    ▼                    ▼
             Serveur client        Canal Slack         Boîte mail
```

Points clés :
- **@Async** : le dispatch ne bloque PAS l'action principale
- **Retry avec backoff** : 2^n minutes entre tentatives (1min, 2min, 4min)
- **HMAC-SHA256** : signature du payload pour que le destinataire vérifie l'authenticité
- **@Scheduled** : un scheduler relance les livraisons échouées toutes les 60s

### 1.7 Multi-tenancy

Le multi-tenant isole les données par organisation :

```
Requête HTTP
    │
    ▼
┌─────────────────────┐
│  TenantInterceptor  │ ← Extrait le tenant (API key ou JWT)
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ TenantContextHolder │ ← Stocke dans ThreadLocal
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│   QuotaService      │ ← Vérifie les limites du plan
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│   Business Logic    │ ← Exécution si quota OK
└─────────────────────┘
```

**ThreadLocal** : chaque thread HTTP a sa propre variable `currentTenant`. Pas besoin de le passer en paramètre à travers toute la stack.

---

## Partie 2 : Guide du développeur

### 2.1 Prérequis

| Outil | Version | Vérification |
|-------|---------|--------------|
| Java | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node.js | 20+ | `node -version` |
| Docker | 24+ | `docker --version` |
| Git | 2.40+ | `git --version` |

### 2.2 Lancement rapide

#### Option A : Tout avec Docker (recommandé, le plus simple)

```bash
# 1. Cloner le projet
git clone https://github.com/landry1996/SpringInitializer-plus-plus.git
cd SpringInitializer-plus-plus

# 2. Lancer toute la stack (1 commande)
docker compose up -d

# 3. Attendre ~30 secondes que le backend démarre, puis ouvrir :
# Frontend : http://localhost:4200
# API : http://localhost:8080
# Swagger : http://localhost:8080/swagger-ui.html
# Keycloak : http://localhost:8180 (admin/admin)
# Grafana : http://localhost:3000 (admin/admin)
```

#### Option B : Backend en développement (hot-reload)

```bash
# 1. Cloner le projet
git clone https://github.com/landry1996/SpringInitializer-plus-plus.git
cd SpringInitializer-plus-plus

# 2. Démarrer uniquement l'infrastructure
docker compose up -d postgres redis

# 3. Lancer le backend (avec rechargement automatique)
cd springforge-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 4. Lancer le frontend (autre terminal)
cd springforge-frontend
npm install
npm start

# 5. Ouvrir dans le navigateur
# Frontend : http://localhost:4200
# API : http://localhost:8080
# Swagger : http://localhost:8080/swagger-ui.html
```

#### Option C : Déployer sur un VPS

```bash
# Sur le VPS (voir GUIDE_DEPLOIEMENT.md pour le détail)
git clone https://github.com/landry1996/SpringInitializer-plus-plus.git /opt/springforge
cd /opt/springforge
cp .env.example .env && nano .env  # configurer les secrets
chmod +x deploy.sh && ./deploy.sh init
# → http://votre-ip-vps
```

### 2.3 Structure du projet expliquée

```
SpringInitializer-plus-plus/
│
├── springforge-backend/              ← Code backend Java (tout est ici)
│   ├── src/main/java/com/springforge/
│   │   ├── shared/                   ← Module noyau (security, config, websocket)
│   │   ├── config/                   ← Configs additionnelles (cache, DB, compression)
│   │   ├── security/                 ← Filtres additionnels (headers, sanitizer)
│   │   ├── recommendation/           ← Moteur IA + rules/
│   │   ├── marketplace/              ← Blueprints communautaires
│   │   ├── admin/                    ← Administration
│   │   ├── tenant/                   ← Multi-organisation
│   │   ├── i18n/                     ← Traductions
│   │   ├── generator/                ← Pipeline de génération (25 fichiers)
│   │   ├── storage/                  ← Stockage MinIO / FileSystem
│   │   ├── billing/                  ← Stripe (abonnements, factures)
│   │   ├── ai/                       ← LLM (Claude, OpenAI, streaming)
│   │   ├── notification/             ← Webhooks & Notifications
│   │   ├── user/                     ← Auth JWT
│   │   ├── blueprint/                ← Définitions architecture
│   │   └── template/                 ← Templates Freemarker
│   ├── src/main/resources/
│   │   ├── db/migration/             ← Scripts SQL Flyway (V1 à V9)
│   │   ├── messages*.properties      ← Traductions backend (EN, FR, DE, ES)
│   │   ├── application.yml           ← Config de base
│   │   ├── application-dev.yml       ← Config développement
│   │   ├── application-prod.yml      ← Config production (env vars)
│   │   └── application-test.yml      ← Config tests
│   ├── src/test/java/                ← Tests unitaires JUnit 5
│   └── pom.xml                       ← Dépendances Maven
│
├── springforge-frontend/             ← Application Angular 18
│   ├── src/app/
│   │   ├── features/                 ← Composants par fonctionnalité
│   │   │   ├── wizard/               ← Wizard 10 étapes
│   │   │   ├── marketplace/          ← Catalogue blueprints
│   │   │   ├── admin/                ← Panel admin
│   │   │   ├── recommendations/      ← Panel recommandations
│   │   │   ├── organization/         ← Settings organisation
│   │   │   └── template-editor/      ← Éditeur visuel de blueprints
│   │   └── i18n/                     ← Service + pipe traduction
│   ├── src/assets/i18n/              ← Fichiers JSON traduction
│   ├── e2e/                          ← Tests Playwright
│   ├── Dockerfile                    ← Build multi-stage
│   └── nginx.conf                    ← Proxy reverse + SPA routing
│
├── springforge-cli/                  ← CLI en Go
│   └── cmd/                          ← Commandes Cobra
│
├── springforge-intellij-plugin/      ← Plugin IntelliJ
│   └── src/main/java/.../intellij/
│       ├── settings/                 ← Configuration persistante
│       ├── api/                      ← Client HTTP
│       └── ui/                       ← Dialogues Swing
│
├── springforge-vscode/               ← Extension VS Code
│   ├── package.json                  ← Manifest (commandes, settings)
│   ├── src/extension.ts              ← Point d'entrée + status bar
│   ├── src/client.ts                 ← Client HTTP API SpringForge
│   └── src/panel.ts                  ← Wizard webview multi-step
│
├── infra/
│   ├── k8s/                          ← Manifestes Kubernetes (12 fichiers)
│   ├── monitoring/                   ← Grafana + Alertes
│   ├── prometheus/                   ← Config scraping
│   └── nginx/                        ← Reverse proxy production (HTTPS)
│
├── .github/workflows/                ← CI/CD GitHub Actions
│   ├── ci.yml                        ← Build + Test
│   ├── release.yml                   ← Docker + Deploy staging
│   └── deploy-prod.yml              ← Deploy production
│
├── docker-compose.yml                ← Stack locale dev (9 services)
├── docker-compose.prod.yml           ← Stack production VPS (5 services + HTTPS)
├── deploy.sh                         ← Script déploiement VPS
├── Dockerfile                        ← Image backend multi-stage
├── .env.example                      ← Template variables d'environnement
├── .dockerignore                     ← Exclusions build Docker
└── docs/api/postman-collection.json  ← Collection Postman
```

### 2.4 Comprendre les flux de données

#### Flux : Génération d'un projet

```
[Angular Frontend]
       │
       │ POST /api/v1/projects/generate
       │ Body: { name, java, springBoot, architecture, dependencies... }
       ▼
[SecurityFilter] → vérifie rate limit + headers
       │
       ▼
[TenantInterceptor] → identifie l'organisation
       │
       ▼
[QuotaService] → vérifie limite mensuelle
       │
       ▼
[GeneratorController] → accepte la requête (202)
       │
       │ @Async("generationExecutor")
       ▼
[GeneratorService]
       │
       ├─ Phase 1: VALIDATE → schéma JSON, permissions
       ├─ Phase 2: RESOLVE → dépendances, compatibilité
       ├─ Phase 3: GENERATE → Freemarker templates → fichiers
       └─ Phase 4: POST_PROCESS → format, compile check, ZIP
       │
       ▼
[WebSocket] → notifie le frontend en temps réel
       │
       ▼
[Frontend] → affiche "Terminé" + bouton Download
```

#### Flux : Recommandation IA

```
[Frontend] → POST /api/v1/recommendations
       │       Body: { architecture, dependencies, javaVersion... }
       ▼
[RecommendationController]
       │
       ▼
[RecommendationService]
       │
       ├─ DependencyRecommendationRule.evaluate(config)
       │   → "Ajoutez Flyway" (vous avez JPA)
       │
       ├─ ArchitectureRecommendationRule.evaluate(config)
       │   → "Hexagonal nécessite un module domain"
       │
       ├─ AntiPatternRule.evaluate(config)
       │   → "H2 + production : incompatible"
       │
       └─ SecurityRecommendationRule.evaluate(config)
           → "Activez HTTPS"
       │
       ▼
[Tri par priorité + confiance]
       │
       ▼
[Response JSON] → liste de recommandations triées
```

### 2.5 Comprendre les nouveaux flux de données

#### Flux : Paiement Stripe (Checkout → Webhook)

```
[Frontend]
     │
     │ POST /api/v1/billing/checkout { plan: "PRO" }
     ▼
[BillingController] → crée session Stripe Checkout
     │
     │ Redirige le navigateur vers Stripe
     ▼
[Page Stripe hébergée] → Utilisateur entre sa carte
     │
     │ Paiement réussi
     ▼
[Stripe serveur] ─── webhook ───▶ POST /api/v1/webhooks/stripe
                                        │
                                        ▼
                                 [StripeWebhookController]
                                        │ Vérifie signature
                                        ▼
                                 [BillingService.handleCheckoutCompleted()]
                                        │
                                        ▼
                                 [Subscription] plan = PRO, status = ACTIVE
                                        │
                                        ▼
                                 [NotificationService] → dispatch SUBSCRIPTION_CHANGED
```

#### Flux : Notification webhook sortante

```
[Événement métier] (ex: génération terminée)
     │
     │ notificationService.dispatch(orgId, GENERATION_COMPLETED, data)
     ▼
[NotificationService] @Async (thread séparé)
     │
     │ Cherche les WebhookConfig actifs de l'org avec cet événement
     ▼
[Pour chaque config]
     │
     ├─ Crée DeliveryLog (tentative #1)
     ├─ Construit le payload JSON
     ├─ Calcule signature HMAC-SHA256 (si secret configuré)
     ├─ POST vers l'URL configurée
     │
     ├── Succès (2xx) → deliveryLog.recordSuccess()
     │
     └── Échec → deliveryLog.recordFailure(nextRetry = now + 2^n min)
                    │
                    ▼
              [retryFailedDeliveries()] @Scheduled toutes les 60s
                    │ Récupère les logs échoués dont nextRetryAt < now
                    └── Re-tente la livraison (max 3 fois)
```

#### Flux : Assistance IA (Chat streaming)

```
[Frontend]
     │
     │ POST /api/v1/ai/chat (Accept: text/event-stream)
     │ Body: { prompt, context, model, temperature }
     ▼
[AiController] produces = TEXT_EVENT_STREAM_VALUE
     │
     ▼
[AiAssistantService.streamChat()]
     │
     ▼
[LlmService.stream()] ← ClaudeLlmService OU OpenAiLlmService
     │
     │ WebClient vers API Claude/OpenAI (streaming)
     ▼
[Flux<String>] → chaque token envoyé en SSE au client
     │
     │ data: "Voici"
     │ data: " une"
     │ data: " suggestion"
     │ data: "..."
     ▼
[Frontend] affiche les tokens au fur et à mesure (effet "typing")
```

### 2.6 Ajouter une nouvelle fonctionnalité : guide pas à pas

#### Exemple : Ajouter une nouvelle règle de recommandation

**1. Créer la classe :**

```java
package com.springforge.recommendation.rules;

@Component
public class TestingRecommendationRule implements RecommendationRule {

    @Override
    public String getRuleId() {
        return "testing-recommendations";
    }

    @Override
    public List<Recommendation> evaluate(ProjectConfig config) {
        List<Recommendation> recommendations = new ArrayList<>();

        if (config.getDependencies().contains("spring-boot-starter-web")
            && !config.getDependencies().contains("spring-boot-starter-test")) {

            recommendations.add(new Recommendation(
                UUID.randomUUID().toString(),
                RecommendationType.BEST_PRACTICE,
                "Ajouter Spring Boot Test",
                "Les applications web nécessitent des tests. Ajoutez spring-boot-starter-test.",
                "testing",
                1, // haute priorité
                0.95, // haute confiance
                List.of("Ajouter spring-boot-starter-test", "Configurer MockMvc")
            ));
        }

        return recommendations;
    }
}
```

**2. C'est tout !** Spring détecte automatiquement le `@Component` et l'injecte dans `RecommendationService`.

**3. Ajouter un test :**

```java
@ExtendWith(MockitoExtension.class)
class TestingRecommendationRuleTest {

    private TestingRecommendationRule rule = new TestingRecommendationRule();

    @Test
    void shouldRecommendTestStarter_whenWebWithoutTest() {
        ProjectConfig config = new ProjectConfig();
        config.setDependencies(List.of("spring-boot-starter-web"));

        List<Recommendation> result = rule.evaluate(config);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(RecommendationType.BEST_PRACTICE);
    }

    @Test
    void shouldNotRecommend_whenTestAlreadyPresent() {
        ProjectConfig config = new ProjectConfig();
        config.setDependencies(List.of("spring-boot-starter-web", "spring-boot-starter-test"));

        List<Recommendation> result = rule.evaluate(config);

        assertThat(result).isEmpty();
    }
}
```

#### Exemple : Ajouter un nouveau canal de notification

**1. Ajouter le canal dans l'enum :**

```java
public enum NotificationChannel {
    WEBHOOK,
    SLACK,
    EMAIL,
    TEAMS  // ← Nouveau
}
```

**2. Gérer le formatage dans NotificationService.deliver() :**

```java
if (config.getChannel() == NotificationChannel.TEAMS) {
    // Microsoft Teams utilise un format "Adaptive Card"
    payload = objectMapper.writeValueAsString(Map.of(
        "@type", "MessageCard",
        "summary", "SpringForge Notification",
        "sections", List.of(Map.of("text", formatTeamsMessage(deliveryLog)))
    ));
}
```

**3. C'est tout !** Le reste (retry, HMAC, logging) fonctionne automatiquement car il est générique.

#### Exemple : Ajouter un nouveau provider LLM

**1. Créer la classe avec la bonne condition :**

```java
@Service
@ConditionalOnProperty(name = "ai.provider", havingValue = "mistral")
public class MistralLlmService implements LlmService {

    @Override
    public LlmResponse complete(LlmRequest request) {
        // Appeler l'API Mistral via WebClient
    }

    @Override
    public Flux<String> stream(LlmRequest request) {
        // Streaming SSE depuis Mistral
    }
}
```

**2. Ajouter la config dans application.yml :**

```yaml
ai:
  provider: mistral
  mistral:
    api-key: ${MISTRAL_API_KEY}
    model: mistral-large-latest
```

**3. C'est tout !** Le `AiAssistantService` utilise l'interface `LlmService` — il ne sait pas quel provider est derrière.

#### Exemple : Ajouter une nouvelle langue (i18n)

**1. Backend** — Créer `springforge-backend/src/main/resources/messages_pt.properties` :

```properties
app.name=SpringForge
generation.started=Geração iniciada
generation.completed=Projeto gerado com sucesso
```

**2. Frontend** — Créer `springforge-frontend/src/assets/i18n/pt.json` :

```json
{
  "app.title": "SpringForge",
  "wizard.next": "Próximo",
  "wizard.previous": "Anterior",
  "wizard.generate": "Gerar Projeto"
}
```

**3. Mettre à jour le locale switcher** — Ajouter le portugais dans la liste des langues supportées.

---

## Partie 3 : Patterns de design utilisés

### 3.1 Tableau récapitulatif

| Pattern | Où | Pourquoi |
|---------|-----|----------|
| **Strategy** | RecommendationRule, LlmService | Algorithmes interchangeables |
| **Template Method** | Generation Pipeline | Phases fixes, implémentation variable |
| **Observer** | WebSocket progress, Webhooks | Notification temps réel |
| **Repository** | JPA Repositories | Abstraction accès données |
| **DTO / Record** | Recommendation, LlmRequest/Response | Immutabilité, transport de données |
| **Interceptor** | TenantInterceptor | Cross-cutting concern (tenant) |
| **Filter Chain** | Security filters | Pipeline de sécurité |
| **Builder** | ProjectConfig | Construction d'objets complexes |
| **Singleton** | TenantContextHolder (ThreadLocal) | Un contexte par thread |
| **Factory** | Blueprint categories | Création d'objets par type |
| **Provider** | StorageService, LlmService | Implémentation conditionnelle (ConditionalOnProperty) |
| **Retry / Backoff** | NotificationService | Résilience livraison webhooks |
| **Async Fire-and-Forget** | NotificationService.dispatch() | Non-bloquant pour l'action principale |
| **Streaming** | AiController (SSE) | Réponses LLM en flux continu |
| **Adapter** | Stripe SDK, MinIO SDK | Encapsulation APIs tierces |

### 3.2 Strategy Pattern — En détail

```
                    ┌─────────────────────┐
                    │ RecommendationRule  │ ← Interface
                    │ + evaluate(config)  │
                    └──────────┬──────────┘
                               │
         ┌─────────────────────┼─────────────────────┐
         │                     │                     │
┌────────▼───────┐  ┌─────────▼────────┐  ┌────────▼────────┐
│ Dependency     │  │ Architecture     │  │ AntiPattern     │
│ Rule           │  │ Rule             │  │ Rule            │
└────────────────┘  └──────────────────┘  └─────────────────┘

Le Service itère sur TOUTES les implémentations sans les connaître.
```

### 3.3 Pipeline Pattern — Génération

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────────┐
│ VALIDATE │───▶│ RESOLVE  │───▶│ GENERATE │───▶│ POST_PROCESS │
└──────────┘    └──────────┘    └──────────┘    └──────────────┘
     │               │               │                │
     ▼               ▼               ▼                ▼
  Schéma OK?    Dépendances    Templates         Format + ZIP
  Permissions?  compatibles?   Freemarker        Compile check
```

Chaque phase peut échouer indépendamment, et le statut est mis à jour en temps réel via WebSocket.

### 3.4 Provider Pattern — Implémentation conditionnelle

```java
// Le code appelant ne connaît que l'interface :
@Service
public class PostProcessStep {
    private final StorageService storageService; // Quelle implémentation ?

    // Spring injecte FileSystemStorageService OU MinioStorageService
    // selon la propriété "storage.type" dans application.yml
}
```

```yaml
# application.yml (dev) :
storage:
  type: filesystem  # → FileSystemStorageService injecté

# application-prod.yml :
storage:
  type: minio       # → MinioStorageService injecté
```

**Mécanisme Spring** : `@ConditionalOnProperty` fait que le bean n'est créé que si la condition est remplie. Résultat : une seule implémentation existe dans le contexte Spring à la fois.

**Quand utiliser** :
- Plusieurs implémentations d'un même service (stockage, notification, LLM)
- Besoin de basculer sans code (juste config)
- Distinction dev/test/prod

### 3.5 Streaming SSE — Réponses en flux

Pour les réponses LLM longues, SpringForge utilise le **Server-Sent Events** :

```java
@PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> chat(@RequestBody LlmRequest request) {
    return aiService.streamChat(request);
    // Chaque token est envoyé au client dès qu'il arrive du LLM
}
```

```
Client                    Serveur                   LLM (Claude/GPT)
  │                         │                           │
  │── POST /ai/chat ──────▶│                           │
  │                         │── Requête API ──────────▶│
  │◀── data: "Bonjour" ────│◀── token "Bonjour" ──────│
  │◀── data: " je" ────────│◀── token " je" ──────────│
  │◀── data: " suis" ──────│◀── token " suis" ────────│
  │◀── data: " Claude" ────│◀── token " Claude" ──────│
  │◀── [DONE] ─────────────│◀── [fin stream] ─────────│
```

**Pourquoi pas WebSocket ?** SSE est plus simple (HTTP standard, reconnexion automatique, unidirectionnel serveur→client). WebSocket est réservé aux cas bidirectionnels (comme la barre de progression de génération).

### 3.6 Retry avec Backoff Exponentiel

```
Tentative 1 (immédiate) : POST → timeout/500 → échec
    ↓ attente 2^1 = 2 minutes
Tentative 2 : POST → timeout/500 → échec
    ↓ attente 2^2 = 4 minutes
Tentative 3 : POST → 200 OK → succès ✅

OU

Tentative 3 : POST → échec → ABANDON (max atteint)
```

**Pourquoi exponentiel ?** Éviter de surcharger un serveur déjà en difficulté. Le délai croissant laisse le temps de récupérer.

**Implémentation** :
```java
@Scheduled(fixedDelay = 60000) // Vérifie toutes les 60s
public void retryFailedDeliveries() {
    List<DeliveryLog> failed = repository
        .findBySuccessFalseAndAttemptCountLessThanAndNextRetryAtBefore(
            MAX_RETRY_ATTEMPTS, LocalDateTime.now());
    // Seuls les logs dont nextRetryAt est dans le passé sont relancés
}
```

### 3.7 ThreadLocal Pattern — Multi-tenant

```java
public class TenantContextHolder {
    private static final ThreadLocal<Organization> currentTenant = new ThreadLocal<>();

    public static void set(Organization org) { currentTenant.set(org); }
    public static Organization get() { return currentTenant.get(); }
    public static void clear() { currentTenant.remove(); } // Important pour éviter les fuites mémoire
}
```

**Attention** : Toujours appeler `clear()` dans un `finally` pour éviter que le tenant d'une requête "fuie" vers la suivante (le thread est réutilisé par le pool).

---

## Partie 4 : Guide des tests

### 4.1 Philosophie de test

| Niveau | Outil | Ce qu'on teste | Quantité |
|--------|-------|----------------|----------|
| Unitaire | JUnit 5 + Mockito | Logique métier isolée | Beaucoup |
| Intégration | @SpringBootTest + Testcontainers | Interactions composants | Modéré |
| E2E | Playwright | Flux utilisateur complets | Peu (critiques) |

**Pyramide des tests** :
```
        /\
       /E2E\        ← Peu, lents, fragiles
      /------\
     /Intégra-\     ← Modéré, moyens
    /  tion    \
   /────────────\
  /  Unitaires   \  ← Beaucoup, rapides, stables
 /________________\
```

### 4.2 Écrire un test unitaire

```java
@ExtendWith(MockitoExtension.class)  // Pas de contexte Spring = rapide
class BlueprintServiceTest {

    @Mock
    private BlueprintRepository repository;  // Dépendance mockée

    @InjectMocks
    private BlueprintService service;  // Classe sous test

    @Test
    void shouldReturnBlueprint_whenExists() {
        // GIVEN (Arrangement)
        Blueprint blueprint = new Blueprint();
        blueprint.setId(1L);
        blueprint.setName("Hexagonal API");
        when(repository.findById(1L)).thenReturn(Optional.of(blueprint));

        // WHEN (Action)
        Blueprint result = service.getById(1L);

        // THEN (Assertion)
        assertThat(result.getName()).isEqualTo("Hexagonal API");
        verify(repository).findById(1L);  // Vérifie l'appel
    }

    @Test
    void shouldThrow_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
```

**Conventions** :
- Nommage : `should[Résultat]_when[Condition]`
- Structure : GIVEN / WHEN / THEN (ou Arrange / Act / Assert)
- Un seul assert logique par test

### 4.3 Écrire un test E2E (Playwright)

```typescript
import { test, expect } from '@playwright/test';

test.describe('Wizard de génération', () => {

  test('devrait compléter le flux de génération', async ({ page }) => {
    // Navigation
    await page.goto('/wizard');

    // Step 1 : Metadata
    await page.fill('[data-testid="project-name"]', 'my-awesome-app');
    await page.fill('[data-testid="group-id"]', 'com.example');
    await page.click('[data-testid="next-button"]');

    // Step 2 : Versions
    await page.selectOption('[data-testid="java-version"]', '21');
    await page.click('[data-testid="next-button"]');

    // ... (autres étapes)

    // Step 10 : Review + Generate
    await page.click('[data-testid="generate-button"]');

    // Vérification
    await expect(page.locator('[data-testid="progress-bar"]')).toBeVisible();
    await expect(page.locator('[data-testid="download-button"]')).toBeVisible({ timeout: 30000 });
  });
});
```

### 4.4 Lancer les tests

```bash
# Tests unitaires backend (rapide, ~10s)
./mvnw test

# Tests unitaires frontend
cd springforge-frontend && npm test

# Tests E2E (nécessite l'application démarrée)
cd springforge-frontend && npx playwright test

# Tests E2E avec UI visible (debug)
cd springforge-frontend && npx playwright test --headed

# Rapport de couverture
cd springforge-backend && ./mvnw test jacoco:report
# Ouvrir springforge-backend/target/site/jacoco/index.html
```

---

## Partie 5 : Sécurité — Comprendre les protections

### 5.1 Defense in Depth (Défense en profondeur)

```
Internet
    │
    ▼
┌───────────────────┐
│ Nginx (TLS/HTTPS) │  ← Chiffrement transport
└─────────┬─────────┘
          ▼
┌───────────────────┐
│ Rate Limiting     │  ← Protection DDoS
└─────────┬─────────┘
          ▼
┌───────────────────┐
│ Security Headers  │  ← Protection navigateur (CSP, HSTS)
└─────────┬─────────┘
          ▼
┌───────────────────┐
│ Authentication    │  ← Qui êtes-vous ? (JWT/API Key)
└─────────┬─────────┘
          ▼
┌───────────────────┐
│ Authorization     │  ← Avez-vous le droit ? (RBAC)
└─────────┬─────────┘
          ▼
┌───────────────────┐
│ Input Validation  │  ← Données sûres ? (sanitize)
└─────────┬─────────┘
          ▼
┌───────────────────┐
│ Business Logic    │  ← Traitement sécurisé
└───────────────────┘
```

### 5.2 Rate Limiting expliqué

```java
// Algorithme : Fixed Window Counter
// Fenêtre : 1 minute
// Limite : 60 requêtes par client

Requête 1  (00:00) → compteur = 1  → ✅ PASS
Requête 2  (00:01) → compteur = 2  → ✅ PASS
...
Requête 60 (00:45) → compteur = 60 → ✅ PASS
Requête 61 (00:50) → compteur = 61 → ❌ 429 Too Many Requests
...
Requête N  (01:01) → nouvelle fenêtre → compteur = 1 → ✅ PASS
```

**Identification du client** :
1. Header `X-API-Key` → identifie par clé API
2. Header `X-Forwarded-For` → identifie par IP (derrière proxy)
3. `request.getRemoteAddr()` → IP directe (fallback)

### 5.3 Content Security Policy (CSP)

Le header CSP dit au navigateur ce qui est autorisé :

```
default-src 'self';          → Par défaut : uniquement notre domaine
script-src 'self';           → JavaScript : uniquement nos scripts
style-src 'self' 'unsafe-inline';  → CSS : nos styles + inline
img-src 'self' data:;        → Images : nous + data URIs
connect-src 'self' ws: wss:; → XHR/WebSocket : nous + WebSocket
frame-ancestors 'none';      → Personne ne peut nous iframer
```

**Protection** : même si un attaquant injecte du HTML, le navigateur bloquera le chargement de scripts externes.

---

## Partie 6 : Infrastructure — Comprendre le déploiement

### 6.1 Docker : du code au conteneur

```
┌─────────────────────────────────────────────────────────┐
│ Dockerfile (Multi-stage build)                          │
│                                                         │
│ Stage 1 : BUILD                                         │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ FROM maven:3.9-eclipse-temurin-21 AS build          │ │
│ │ COPY pom.xml + src/ → mvn package                   │ │
│ │ Résultat : target/app.jar (≈50 Mo)                  │ │
│ └─────────────────────────────────────────────────────┘ │
│                                                         │
│ Stage 2 : RUN                                           │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ FROM eclipse-temurin:21-jre-alpine                  │ │
│ │ COPY --from=build target/app.jar                    │ │
│ │ Image finale : ≈200 Mo (au lieu de ≈800 Mo)        │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

**Pourquoi multi-stage ?** Le JDK complet + Maven + sources = 800 Mo. En production, on n'a besoin que du JRE + le JAR = 200 Mo.

### 6.2 Docker Compose : deux modes

**Mode local (docker-compose.yml)** — développement, tous les services :
```yaml
services:
  backend:    → dépend de postgres, kafka, redis, minio
  frontend:   → dépend de backend (proxy nginx → backend:8080)
  postgres:   → stockage persistant (volume pgdata)
  redis:      → cache en mémoire
  minio:      → stockage objets S3-compatible (port 9000/9001)
  kafka:      → dépend de zookeeper
  zookeeper:  → coordination Kafka
  keycloak:   → authentification
  prometheus: → collecte métriques backend
  grafana:    → visualise les métriques prometheus
```

**Mode production VPS (docker-compose.prod.yml)** — optimisé, services essentiels :
```yaml
services:
  nginx:      → reverse proxy HTTPS (Let's Encrypt) → route vers backend/frontend
  backend:    → dépend de postgres, redis (Kafka/Keycloak optionnels)
  frontend:   → sert l'Angular SPA via Nginx interne
  postgres:   → base de données avec healthcheck
  redis:      → cache avec limite mémoire 128 Mo
  certbot:    → renouvellement automatique certificats SSL
```

**Pourquoi deux fichiers ?**
- En dev : on veut TOUT (Kafka, Keycloak, monitoring) pour tester
- En prod sur VPS 2 Go : on garde le minimum pour économiser la RAM
- Kafka et Keycloak sont désactivés via variables d'environnement

### 6.3 Script de déploiement VPS (deploy.sh)

```bash
./deploy.sh init    # Premier lancement (build + start)
./deploy.sh update  # Mise à jour (git pull + rebuild + restart)
./deploy.sh ssl     # Activer HTTPS avec Let's Encrypt
./deploy.sh status  # Vérifier que tout tourne
./deploy.sh logs    # Voir les logs en temps réel
./deploy.sh backup  # Sauvegarder PostgreSQL
./deploy.sh stop    # Arrêter tout
```

### 6.4 Kubernetes : production haute disponibilité

| Concept K8s | Rôle dans SpringForge |
|-------------|----------------------|
| **Namespace** | Isoler springforge des autres apps du cluster |
| **Deployment** | Définir combien de pods et quelle image |
| **Service** | DNS interne (ex: `backend-service:8080`) |
| **Ingress** | Exposer sur Internet (springforge.io → frontend, api.springforge.io → backend) |
| **HPA** | Autoscaling : ajouter des pods si CPU > 70% |
| **ConfigMap** | Variables d'environnement non-secrètes |
| **Secret** | Mots de passe, clés API (chiffrés) |
| **PVC** | Disque persistant pour PostgreSQL |

### 6.4 CI/CD : du commit au déploiement

```
Developer pushes code
        │
        ▼
┌─────────────────┐
│   ci.yml        │  ← Automatique sur chaque push/PR
│ • mvn test      │
│ • npm test      │
│ • security scan │
└────────┬────────┘
         │ ✅ Tous les checks passent
         ▼
┌─────────────────┐
│  git tag v1.2.0 │  ← Développeur crée un tag
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  release.yml    │  ← Automatique sur tag v*
│ • Build Docker  │
│ • Push GHCR     │
│ • Deploy staging│
└────────┬────────┘
         │ ✅ Tests staging OK
         ▼
┌─────────────────┐
│ deploy-prod.yml │  ← Manuel (workflow_dispatch)
│ • Confirmation  │     Tapez "deploy" pour confirmer
│ • Deploy prod   │
│ • Health check  │
└─────────────────┘
```

---

## Partie 7 : Bonnes pratiques du projet

### 7.1 Conventions de code

| Aspect | Convention |
|--------|-----------|
| Nommage classes | PascalCase (`BlueprintService`) |
| Nommage méthodes | camelCase (`getById`) |
| Nommage constantes | UPPER_SNAKE (`MAX_REQUESTS_PER_MINUTE`) |
| Packages | lowercase (`com.springforge.marketplace`) |
| DTOs | Java Records (immuables) |
| Injection | Constructeur (pas `@Autowired` sur champs) |
| Exceptions | Custom + `@ControllerAdvice` global |
| Logs | SLF4J (`log.info(...)`) |

### 7.2 Conventions Git

```bash
# Format du commit
type: description courte

# Types
feat:     nouvelle fonctionnalité
fix:      correction de bug
docs:     documentation
test:     ajout/modification de tests
refactor: refactoring sans changement fonctionnel
perf:     amélioration performance
infra:    infrastructure (Docker, K8s, CI)
ci:       configuration CI/CD
```

### 7.3 Conventions API REST

| Méthode | Usage | Status code |
|---------|-------|-------------|
| GET | Lecture | 200 OK |
| POST | Création | 201 Created |
| POST (action) | Déclenchement | 202 Accepted |
| PUT | Mise à jour complète | 200 OK |
| PATCH | Mise à jour partielle | 200 OK |
| DELETE | Suppression | 204 No Content |

**Erreurs** : Toujours retourner un body JSON structuré :
```json
{
  "error": "Rate limit exceeded",
  "code": "RATE_LIMIT",
  "details": "Try again in 45 seconds"
}
```

---

## Partie 8 : Glossaire

| Terme | Définition |
|-------|-----------|
| **Blueprint** | Template de projet prédéfini avec architecture, dépendances et structure |
| **Tenant** | Organisation isolée dans le système multi-tenant |
| **Pipeline** | Suite d'étapes séquentielles de traitement (validation → génération → packaging) |
| **Rule** | Composant pluggable qui analyse une configuration et produit des recommandations |
| **HPA** | Horizontal Pod Autoscaler — ajuste le nombre de pods selon la charge |
| **CSP** | Content Security Policy — header HTTP contrôlant les ressources chargeables |
| **HSTS** | HTTP Strict Transport Security — force HTTPS |
| **Flyway** | Outil de migration de schéma de base de données (versionné) |
| **Actuator** | Module Spring Boot exposant des endpoints de monitoring (/health, /metrics) |
| **HikariCP** | Pool de connexions JDBC haute performance |
| **Freemarker** | Moteur de templates pour générer du texte (code Java, YAML, etc.) |
| **WebSocket** | Protocole de communication bidirectionnelle temps réel |
| **Standalone Component** | Composant Angular auto-suffisant (pas besoin de NgModule) |
| **Signal** | Primitive réactive Angular pour gérer l'état |
| **MinIO** | Serveur de stockage d'objets compatible S3 (alternative open-source à AWS S3) |
| **Object Storage** | Stockage de fichiers par clé (key/value), sans hiérarchie de dossiers |
| **Stripe** | Plateforme de paiement en ligne (checkout, abonnements, factures) |
| **Webhook** | Callback HTTP déclenché par un événement — le serveur appelle un URL externe |
| **HMAC-SHA256** | Algorithme de signature cryptographique pour authentifier un message |
| **Backoff exponentiel** | Stratégie de retry où le délai double à chaque tentative (2^n) |
| **SSE** | Server-Sent Events — flux unidirectionnel serveur→client via HTTP |
| **Flux** | Type réactif Spring WebFlux représentant un flux de 0 à N éléments |
| **LLM** | Large Language Model — modèle d'IA générative (Claude, GPT) |
| **ConditionalOnProperty** | Annotation Spring qui active un bean selon une propriété de configuration |
| **Mongock** | Framework de migration de données pour MongoDB (équivalent de Flyway) |
| **Webview API** | API VS Code permettant de créer des UI HTML/CSS/JS dans l'éditeur |
