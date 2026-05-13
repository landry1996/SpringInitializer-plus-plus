# P6 — Suivi d'implémentation — Extensions & Monétisation

## Items

| # | Item | Statut | Description |
|---|------|--------|-------------|
| 1 | MongoDB Support | ✅ DONE | Support MongoDB comme option de DB dans le générateur (templates, config, dépendances) |
| 2 | Stockage MinIO | ✅ DONE | Persistence des ZIPs générés sur object storage S3-compatible |
| 3 | Billing Stripe | ✅ DONE | Paiement réel, webhooks Stripe, gestion abonnements, factures |
| 4 | VS Code Extension | ⬜ TODO | Plugin VS Code complémentaire à IntelliJ pour génération de projets |
| 5 | Template Editor visuel | ⬜ TODO | Éditeur WYSIWYG de blueprints dans le frontend Angular |
| 6 | LLM Integration | ⬜ TODO | Génération de code intelligente via Claude/GPT (code review, suggestions) |
| 7 | Webhooks & Notifications | ⬜ TODO | Notifications Slack/email sur événements (génération, quota, alertes) |

## Détails techniques

### 1. MongoDB Support
- Ajout de MongoDB comme option dans le wizard (step DB)
- Templates Freemarker pour configuration MongoDB (application.yml, docker-compose)
- Génération des repositories Spring Data MongoDB (MongoRepository)
- Dépendances auto-résolues : spring-boot-starter-data-mongodb, embedded-mongo (test)
- Migration Flyway remplacée par Mongock pour les schémas MongoDB
- Docker Compose avec service MongoDB 7
- Tests avec Testcontainers MongoDB

### 2. Stockage MinIO
- Service MinIO dans docker-compose (port 9000/9001)
- Client MinIO Java (io.minio:minio)
- StorageService abstrait avec implémentations : FileSystem (dev) et MinIO (prod)
- Upload automatique des ZIPs générés dans un bucket `springforge-generations`
- URLs pré-signées pour le download (expiration 24h)
- Politique de rétention (suppression après 30 jours)
- Configuration K8s : PVC pour MinIO ou connexion S3 AWS

### 3. Billing Stripe
- Intégration Stripe Java SDK
- Modèle de données : Subscription, Invoice, PaymentMethod
- Plans tarifaires : FREE (0€), PRO (29€/mois), ENTERPRISE (99€/mois)
- Endpoints :
  - POST /api/v1/billing/checkout — créer session Stripe Checkout
  - POST /api/v1/billing/portal — accès portail client Stripe
  - POST /api/v1/webhooks/stripe — réception événements Stripe
- Webhooks Stripe : `checkout.session.completed`, `invoice.paid`, `invoice.payment_failed`, `customer.subscription.deleted`
- Mise à jour automatique du SubscriptionPlan de l'organisation
- Page frontend : plans, facturation, historique paiements
- Mode test Stripe pour dev/staging

### 4. VS Code Extension
- Extension TypeScript pour VS Code
- Commande palette : "SpringForge: Generate Project"
- Wizard multi-step dans panel VS Code (Webview API)
- Communication avec l'API SpringForge (même endpoints que IntelliJ)
- Barre de statut avec progression de génération
- Output channel pour logs
- Settings : URL serveur, API key, langue
- Publication sur VS Code Marketplace
- Structure : `springforge-vscode/` (package.json, extension.ts, webview/)

### 5. Template Editor visuel
- Composant Angular standalone : TemplateEditorComponent
- Éditeur de blueprint en drag-and-drop :
  - Sections : metadata, dépendances, structure fichiers, variables
  - Preview live de la structure générée
  - Validation en temps réel (schéma YAML)
- Monaco Editor intégré pour édition YAML/Freemarker brut
- Export/Import de blueprints (JSON/YAML)
- Versioning côté frontend avec diff visuel
- Publication directe vers le Marketplace
- Routes : /editor/new, /editor/:id

### 6. LLM Integration
- Service LLMService avec interface pluggable (Claude, GPT, local)
- Fonctionnalités :
  - Code review automatique du projet généré
  - Suggestions d'amélioration de l'architecture
  - Génération de code boilerplate intelligent (services, controllers, tests)
  - Documentation auto-générée (README, API docs)
  - Chat assistant dans le wizard
- API : POST /api/v1/ai/review, POST /api/v1/ai/suggest, POST /api/v1/ai/generate
- Configuration : provider (claude/openai), model, API key (secret)
- Rate limiting spécifique LLM (tokens/minute par plan)
- Frontend : panel chat, bouton "Ask AI" dans le wizard, code review panel
- Streaming des réponses via SSE (Server-Sent Events)

### 7. Webhooks & Notifications
- Système de webhooks sortants configurables par organisation
- Événements :
  - `generation.completed` — projet généré avec succès
  - `generation.failed` — échec de génération
  - `quota.warning` — 80% du quota atteint
  - `quota.exceeded` — quota dépassé
  - `subscription.changed` — changement de plan
  - `member.joined` — nouveau membre dans l'organisation
- Canaux de notification :
  - Webhook HTTP (POST JSON configurable)
  - Slack (Incoming Webhook URL)
  - Email (SMTP / SendGrid)
- Entités : WebhookConfig, NotificationEvent, DeliveryLog
- Retry policy : 3 tentatives avec backoff exponentiel
- UI : page de configuration des webhooks, historique des envois, test de connexion
- Endpoints :
  - CRUD /api/v1/organizations/{id}/webhooks
  - POST /api/v1/organizations/{id}/webhooks/{wid}/test
  - GET /api/v1/organizations/{id}/webhooks/{wid}/deliveries
