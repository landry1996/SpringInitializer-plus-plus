# P7 — Suivi d'implementation — Frontend P6 & Completude

## Items

| # | Item | Statut | Description |
|---|------|--------|-------------|
| 1 | Frontend Billing | ✅ DONE | Page Angular abonnements, checkout Stripe, historique factures |
| 2 | Frontend Webhooks | ✅ DONE | Page Angular configuration webhooks, historique envois, test |
| 3 | Frontend Chat IA | ✅ DONE | Panel chat SSE dans le wizard, bouton "Ask AI", code review panel |
| 4 | Canal Email (SMTP) | ✅ DONE | Implementation NotificationService pour canal EMAIL (SendGrid/SMTP) |
| 5 | Tests unitaires P6 | ✅ DONE | JUnit 5 pour storage, billing, ai, notification |
| 6 | Generateur Gradle | ✅ DONE | Support Gradle Groovy/Kotlin DSL dans le pipeline de generation |
| 7 | Generateur MySQL | ✅ DONE | Support MySQL comme option DB dans le wizard + templates |
| 8 | Tests E2E P6 | ✅ DONE | Playwright pour billing, webhooks, chat IA |
| 9 | Periode d'essai PRO | ✅ DONE | Trial 14 jours automatique a l'inscription |
| 10 | Publication extensions | ✅ DONE | VS Code Marketplace + JetBrains Marketplace (CI workflow) |

## Details techniques

### 1. Frontend Billing
- Composant Angular standalone : `BillingComponent`
- Route : `/settings/billing`
- Sections :
  - Plan actuel avec badge (FREE/PRO/ENTERPRISE)
  - Boutons upgrade/downgrade avec redirection Stripe Checkout
  - Bouton "Gerer mon abonnement" (portail Stripe)
  - Tableau historique factures (date, montant, statut, lien PDF)
  - Barre de progression usage quotas (generations restantes)
- Services : `BillingService` (getSubscription, createCheckout, createPortal, getInvoices)
- Integration avec le `QuotaService` existant pour afficher l'usage

### 2. Frontend Webhooks
- Composant Angular standalone : `WebhooksComponent`
- Route : `/settings/webhooks`
- Sections :
  - Liste des webhooks configures (nom, URL, canal, statut actif/inactif)
  - Formulaire creation/edition (nom, URL, secret, canal, evenements)
  - Bouton "Test" avec feedback succes/echec
  - Detail webhook : historique des envois (pagine, statut, date, HTTP status)
  - Toggle actif/inactif par webhook
- Services : `WebhookService` (list, create, update, delete, test, getDeliveries)

### 3. Frontend Chat IA
- Composant Angular standalone : `AiChatPanelComponent`
- Integration dans le wizard (panel lateral ou drawer)
- Fonctionnalites :
  - Chat streaming SSE (affichage token par token, effet typing)
  - Bouton "Ask AI" contextuel dans chaque step du wizard
  - Panel code review : affiche les resultats de POST /ai/review
  - Panel suggestions : affiche les resultats de POST /ai/suggest
  - Historique de conversation (session locale)
- Service : `AiService` (review, suggest, generate, streamChat via EventSource)
- UX : Markdown rendering des reponses, code highlighting

### 4. Canal Email (SMTP)
- Implementation `EmailNotificationSender` dans le module notification
- Configuration SMTP (host, port, username, password) ou SendGrid API key
- Template email HTML (FreeMarker) pour chaque type d'evenement
- @ConditionalOnProperty(name = "notification.email.enabled", havingValue = "true")
- Integration dans `NotificationService.deliver()` pour canal EMAIL
- Configuration :
  ```yaml
  notification:
    email:
      enabled: true
      provider: smtp  # ou sendgrid
      smtp:
        host: ${SMTP_HOST}
        port: ${SMTP_PORT:587}
        username: ${SMTP_USERNAME}
        password: ${SMTP_PASSWORD}
      sendgrid:
        api-key: ${SENDGRID_API_KEY}
      from: notifications@springforge.io
  ```

### 5. Tests unitaires P6
- `StorageServiceTest` : upload, download, delete, exists (mock MinioClient)
- `StorageCleanupSchedulerTest` : retention 30 jours, suppression correcte
- `BillingServiceTest` : createCheckout, handleCheckoutCompleted, handleInvoicePaid, handleSubscriptionDeleted
- `StripeWebhookControllerTest` : signature valide/invalide, evenements traites
- `AiAssistantServiceTest` : review, suggest, generate (mock LlmService)
- `ClaudeLlmServiceTest` / `OpenAiLlmServiceTest` : appels API mockes (WebClient)
- `NotificationServiceTest` : dispatch, deliver success/failure, retry logic
- `WebhookControllerTest` : CRUD, test endpoint, deliveries pagination
- Objectif : couverture > 80% sur les modules P6

### 6. Generateur Gradle
- Templates Freemarker :
  - `build.gradle.ftl` (Groovy DSL)
  - `build.gradle.kts.ftl` (Kotlin DSL)
  - `settings.gradle.ftl` / `settings.gradle.kts.ftl`
  - `gradle/wrapper/gradle-wrapper.properties.ftl`
  - `gradlew` + `gradlew.bat` (fichiers statiques)
- Modification `GenerateStep` :
  - Condition sur `buildTool` (MAVEN/GRADLE_GROOVY/GRADLE_KOTLIN)
  - Generation pom.xml OU build.gradle selon le choix
- Modification `ResolveStep` : resolution des dependances format Gradle
- Modification `ValidateStep` : validation compatibilite build tool / plugins
- Wizard frontend : step 3 avec 3 options (Maven, Gradle Groovy, Gradle Kotlin)

### 7. Generateur MySQL
- Templates Freemarker :
  - `application-mysql.yml.ftl` (datasource, dialect, DDL)
  - `docker-compose-mysql.yml.ftl` (service MySQL 8)
- Modification `GenerateStep` :
  - Detection `hasMySQL()` dans la config
  - Generation config specifique MySQL (dialect, driver)
  - Docker Compose avec service mysql:8 + healthcheck
- Modification `ResolveStep` :
  - Dependance auto : `mysql-connector-j`
  - Detection conflit MySQL + MongoDB
- Flyway : templates de migration compatibles MySQL
- Wizard frontend : step DB avec option MySQL (en plus de PostgreSQL et MongoDB)

### 8. Tests E2E P6
- `billing.spec.ts` :
  - Affichage plan actuel
  - Clic upgrade → redirection Stripe (mock)
  - Affichage historique factures
- `webhooks.spec.ts` :
  - Creation webhook (formulaire complet)
  - Test webhook (bouton test + feedback)
  - Historique deliveries
  - Toggle actif/inactif
  - Suppression webhook
- `template-editor.spec.ts` :
  - Creation nouveau blueprint
  - Edition sections (metadata, deps, fichiers, variables)
  - Preview live
  - Export/Import JSON
- `ai-chat.spec.ts` :
  - Ouverture panel chat
  - Envoi message + reception reponse streamee
  - Bouton "Ask AI" dans wizard step

### 9. Periode d'essai PRO
- A l'inscription, creer automatiquement une Subscription avec :
  - plan = PRO
  - status = ACTIVE
  - trialEndsAt = now + 14 jours
- Champ `trialEndsAt` dans l'entite Subscription
- `QuotaService` : utiliser les quotas PRO si trial actif
- Scheduler `TrialExpirationScheduler` :
  - @Scheduled quotidien
  - Passe en FREE les subscriptions dont trialEndsAt < now et pas de paiement Stripe
- Notification `TRIAL_EXPIRING` (3 jours avant expiration)
- Notification `TRIAL_EXPIRED` (jour J)
- Frontend : banniere "Votre essai PRO expire dans X jours" + bouton upgrade

### 10. Publication extensions
- **VS Code** :
  - Fichier `.vscodeignore` (deja present)
  - `vsce package` → genere `.vsix`
  - `vsce publish` → publie sur VS Code Marketplace
  - CI workflow : build + publish sur tag
  - README.md de l'extension avec screenshots
- **IntelliJ** :
  - `plugin.xml` avec description, changelog, compatibility
  - Build avec Gradle IntelliJ Plugin
  - Publication via JetBrains Marketplace API
  - CI workflow : build + publish sur tag
  - Screenshots + documentation

## Ordre d'implementation recommande

```
Phase 7A (Frontend P6) :  1 → 2 → 3        ~3-4 jours
Phase 7B (Backend) :      4 → 9             ~1-2 jours
Phase 7C (Tests) :        5 → 8             ~2-3 jours
Phase 7D (Generateur) :   6 → 7             ~2-3 jours
Phase 7E (Publication) :  10                 ~1 jour
```

Total estime : 9-13 jours de developpement
