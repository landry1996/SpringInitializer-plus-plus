# SpringForge — Document Fonctionnel

## 1. Présentation du produit

**SpringForge** est une plateforme SaaS de génération de projets Spring Boot enterprise-grade. Elle permet aux équipes de développement de créer des projets production-ready en moins de 60 secondes, en respectant les standards architecturaux de leur organisation.

### 1.1 Proposition de valeur

| Problème | Solution SpringForge |
|----------|---------------------|
| Setup d'un nouveau microservice : 2-5 jours | Génération complète en < 60 secondes |
| Architectures incohérentes entre équipes | Blueprints imposant les standards |
| Dépendances mal configurées | Résolution automatique + recommandations IA |
| Pas de bonnes pratiques par défaut | Templates intégrant sécurité, tests, CI/CD |
| Onboarding développeurs lent | Wizard guidé pas à pas |

### 1.2 Utilisateurs cibles

| Persona | Usage |
|---------|-------|
| **Développeur** | Génère des projets via wizard, CLI ou IDE |
| **Tech Lead** | Crée et publie des blueprints pour son équipe |
| **Architecte** | Définit les standards via le marketplace |
| **Admin plateforme** | Gère utilisateurs, quotas, audit |
| **CTO / Manager** | Consulte les statistiques d'utilisation |

---

## 2. Fonctionnalités implémentées

### 2.1 Wizard de génération (10 étapes)

Le wizard Angular guide l'utilisateur à travers la configuration complète d'un projet :

| Étape | Nom | Description |
|-------|-----|-------------|
| 1 | Metadata | Nom du projet, description, group ID, artifact ID |
| 2 | Versions | Version Java (11/17/21), version Spring Boot |
| 3 | Build Tool | Maven ou Gradle (Groovy/Kotlin DSL) |
| 4 | Architecture | Choix parmi 8 types (Monolithic, Layered, Hexagonal, DDD, CQRS, Event-Driven, Microservices, Modulith) |
| 5 | **Architecture Config** | Configuration dynamique selon l'architecture choisie (voir section 2.20) |
| 6 | Dépendances | Catalogue avec recherche et filtres |
| 7 | Sécurité | None, JWT, OAuth2 |
| 8 | Infrastructure | Docker, CI/CD, Base de données |
| 9 | Options | Paramètres avancés (profils, logging) |
| 10 | Review | Récapitulatif + **diagramme d'architecture auto-généré** + bouton Générer |

**Caractéristiques UX :**
- Progression visuelle avec barre de steps
- Validation en temps réel à chaque étape
- Navigation avant/arrière sans perte de données
- Recommandations IA affichées en sidebar
- Interface dynamique à l'étape 5 qui s'adapte au type d'architecture sélectionné
- Diagramme SVG auto-généré à l'étape 10 montrant les services, connexions et bases de données

### 2.2 Moteur de recommandations IA

Le système analyse la configuration en cours et fournit des suggestions intelligentes :

#### Types de recommandations

| Type | Exemple |
|------|---------|
| DEPENDENCY | "Ajoutez Flyway pour gérer vos migrations (vous utilisez JPA)" |
| ARCHITECTURE | "Avec DDD, ajoutez un module shared-kernel" |
| ANTI_PATTERN | "Spring Security + JWT custom sont incompatibles" |
| SECURITY | "Activez HTTPS en production" |
| PERFORMANCE | "Ajoutez Spring Cache avec votre volume de données" |
| BEST_PRACTICE | "Utilisez des records Java pour vos DTOs" |

#### Scoring de compatibilité

Le système calcule un score global (0-100) basé sur :
- Cohérence des dépendances
- Adéquation architecture/complexité
- Couverture sécurité
- Bonnes pratiques respectées

Chaque catégorie obtient un score individuel avec forces et axes d'amélioration.

### 2.3 Marketplace de blueprints

Un catalogue communautaire de templates réutilisables :

#### Fonctionnalités

| Action | Description |
|--------|-------------|
| Rechercher | Texte libre + filtrage par catégorie, tags, popularité |
| Consulter | Détail avec README, dépendances, structure, avis |
| Télécharger | Utiliser un blueprint pour générer un projet |
| Publier | Soumettre un blueprint (validation admin requise) |
| Noter | Système de notation 1-5 étoiles |
| Versionner | Chaque blueprint a un versioning sémantique |

#### Catégories de blueprints

- Microservices
- API REST
- Batch Processing
- Event-Driven
- Gateway
- BFF (Backend for Frontend)

### 2.4 Panel d'administration

Interface réservée aux administrateurs pour gérer la plateforme :

#### Dashboard

| Métrique | Description |
|----------|-------------|
| Total utilisateurs | Nombre d'utilisateurs inscrits |
| Projets générés (mois) | Compteur mensuel de générations |
| Blueprints actifs | Nombre de blueprints publiés |
| Taux de succès | % de générations réussies |
| Architecture populaire | Répartition par type d'architecture |
| Activité récente | Timeline des dernières actions |

#### Gestion des utilisateurs

- Liste avec recherche et filtres (rôle, statut, date)
- Activation / désactivation de comptes
- Attribution de rôles (ADMIN, USER, VIEWER)
- Historique d'activité par utilisateur

#### Audit

- Log complet de toutes les actions significatives
- Filtrable par : utilisateur, action, date, entité
- Actions tracées : login, génération, publication, modification settings
- Export des logs

#### Validation blueprints

- File d'attente des blueprints soumis
- Prévisualisation avant approbation
- Approuver / Rejeter avec commentaire

### 2.5 Multi-tenant SaaS

Architecture multi-organisation avec isolation des données :

#### Plans d'abonnement

| Fonctionnalité | FREE | PRO | ENTERPRISE |
|---------------|------|-----|------------|
| Générations/mois | 5 | 50 | Illimité |
| Membres | 2 | 10 | Illimité |
| Blueprints privés | 10 | 100 | Illimité |
| Recommandations IA | Basiques | Complètes | Complètes + priorité |
| Support | Communauté | Email | Dédié |

#### Gestion d'organisation

| Fonctionnalité | Description |
|---------------|-------------|
| Créer organisation | Nom, slug, plan initial |
| Inviter membres | Par email, rôle (OWNER, ADMIN, MEMBER) |
| Clés API | Génération, révocation, rotation |
| Quotas | Visualisation usage en temps réel (barres de progression) |
| Paramètres | Logo, URL, préférences par défaut |

#### Isolation des données

- Chaque requête est contextualisée par le tenant (header X-API-Key ou JWT)
- Les données d'une organisation sont invisibles aux autres
- Les quotas sont vérifiés avant chaque action consommatrice

### 2.6 Internationalisation (i18n)

Support multilingue complet pour 4 langues :

| Langue | Code | Couverture |
|--------|------|-----------|
| English | en | 100% |
| Français | fr | 100% |
| Deutsch | de | 100% |
| Español | es | 100% |

#### Fonctionnement

- **Détection automatique** : header `Accept-Language` du navigateur
- **Sélection manuelle** : dropdown avec drapeaux dans le header
- **Persistance** : choix sauvegardé en localStorage
- **Fallback** : anglais si clé manquante dans la locale choisie

#### Éléments traduits

- Interface utilisateur complète (labels, boutons, messages)
- Messages d'erreur et validations
- Descriptions des blueprints et dépendances
- Emails et notifications

### 2.7 Plugin IntelliJ

Extension IDE permettant de générer des projets sans quitter l'éditeur :

#### Fonctionnalités

| Feature | Description |
|---------|-------------|
| New Project Action | Menu File → New → SpringForge Project |
| Formulaire complet | Tous les paramètres du wizard web |
| Configuration serveur | URL + API key dans Settings |
| Progress dialog | Barre de progression avec statut en temps réel |
| Auto-ouverture | Le projet généré s'ouvre automatiquement |

### 2.8 CLI Go

Outil en ligne de commande pour les workflows terminal :

```bash
# Génération interactive
springforge generate

# Génération avec flags
springforge generate --name my-app --arch hexagonal --java 21

# Lister les blueprints
springforge blueprints list

# Vérifier la configuration
springforge config show
```

### 2.9 Extension VS Code

Plugin complémentaire au plugin IntelliJ pour les développeurs VS Code :

| Feature | Description |
|---------|-------------|
| Commande palette | "SpringForge: Generate Project" |
| Wizard webview | Formulaire multi-step dans un panel VS Code |
| Barre de statut | Progression de génération en temps réel |
| Output channel | Logs détaillés de la génération |
| Settings | URL serveur, API key, langue |
| Auto-ouverture | Le projet généré s'ouvre dans le workspace |

### 2.10 Éditeur visuel de templates (Template Editor)

Composant Angular pour créer et éditer des blueprints en mode WYSIWYG :

| Section | Description |
|---------|-------------|
| Metadata | Nom, description, version, catégorie, tags |
| Dépendances | Ajout/suppression avec auto-complétion |
| Fichiers | Arborescence drag-and-drop des fichiers à générer |
| Variables | Définition de variables template avec types et valeurs par défaut |
| Raw YAML | Éditeur Monaco pour édition directe du YAML/Freemarker |
| Preview | Prévisualisation live de la structure générée |
| Import/Export | Import/Export de blueprints (JSON/YAML) |

Routes : `/editor/new` (nouveau blueprint), `/editor/:id` (éditer existant)

### 2.11 Intégration LLM (Intelligence Artificielle)

Assistance IA pour améliorer les projets générés :

| Fonctionnalité | Description |
|---------------|-------------|
| Code Review | Analyse automatique du projet généré (qualité, sécurité, performance) |
| Suggestions | Recommandations d'amélioration d'architecture |
| Génération de code | Création de services, controllers, tests boilerplate |
| Chat assistant | Conversation interactive avec streaming SSE |
| Multi-provider | Claude (Anthropic) ou GPT (OpenAI), configurable |

### 2.12 Stockage cloud (MinIO)

Persistence des artefacts générés sur object storage S3-compatible :

| Fonctionnalité | Description |
|---------------|-------------|
| Upload automatique | Les ZIPs générés sont uploadés dans MinIO |
| Download | Téléchargement via URL pré-signée ou streaming |
| Rétention | Suppression automatique après 30 jours |
| Fallback local | Mode filesystem en développement |
| Bucket | `springforge-generations` |

### 2.13 Billing & Abonnements (Stripe)

Gestion complète de la facturation :

| Fonctionnalité | Description |
|---------------|-------------|
| Plans | FREE (0€), PRO (29€/mois), ENTERPRISE (99€/mois) |
| Checkout | Session de paiement Stripe hébergée |
| Portail client | Gestion abonnement, méthode de paiement |
| Factures | Historique complet des paiements |
| Webhooks Stripe | Mise à jour automatique du plan en temps réel |
| Période d'essai | 14 jours PRO offerts automatiquement aux nouveaux utilisateurs |
| Expiration trial | Scheduler horaire downgrade vers FREE si trial expiré |
| Conversion | Le flag trial est retiré lors du paiement Stripe réussi |

### 2.14 Webhooks & Notifications

Système de notifications sortantes configurables par organisation :

| Fonctionnalité | Description |
|---------------|-------------|
| Canaux | Webhook HTTP, Slack (Incoming Webhook), Email |
| Événements | Génération réussie/échouée, quota atteint/dépassé, changement plan, nouveau membre |
| Configuration | Interface CRUD pour ajouter/modifier/supprimer des webhooks |
| Test | Envoi d'un message de test pour vérifier la configuration |
| Historique | Journal complet des envois avec statut (succès/échec) |
| Retry | 3 tentatives avec backoff exponentiel en cas d'échec |
| Sécurité | Signature HMAC-SHA256 pour authentifier les payloads |

### 2.15 Support MongoDB (Générateur)

Option de base de données MongoDB dans le wizard de génération :

| Fonctionnalité | Description |
|---------------|-------------|
| Wizard step DB | Choix entre PostgreSQL (JPA) et MongoDB |
| Templates | Configuration MongoDB, documents, repositories |
| Migrations | Mongock au lieu de Flyway |
| Docker Compose | Service MongoDB 7 avec healthcheck |
| Tests | Testcontainers MongoDB + embedded-mongo |
| Dépendances auto | Résolution automatique des libs complémentaires |

### 2.16 Support MySQL (Générateur)

Option de base de données MySQL dans le wizard de génération :

| Fonctionnalité | Description |
|---------------|-------------|
| Wizard step DB | Choix entre PostgreSQL, MongoDB et MySQL |
| Templates | `application-mysql.yml.ftl` (datasource, dialect MySQL, Flyway) |
| Docker Compose | Service MySQL 8.0 avec healthcheck (`mysqladmin ping`) |
| Configuration | Port 3306, driver `com.mysql.cj.jdbc.Driver`, dialect `MySQLDialect` |
| Flyway | Migrations SQL compatibles MySQL |
| Détection | Automatique par présence de `mysql-connector-j` dans les dépendances |

### 2.17 Support Gradle (Générateur)

Support des build tools Gradle en plus de Maven :

| Fonctionnalité | Description |
|---------------|-------------|
| Wizard step Build | Choix entre Maven, Gradle Groovy, Gradle Kotlin DSL |
| Templates | `build.gradle.ftl`, `build.gradle.kts.ftl`, `settings.gradle.kts.ftl` |
| Wrapper | Fichiers `gradlew` et `gradlew.bat` générés |
| Dépendances | Format Gradle (`implementation`, `testImplementation`) |
| Plugins | Spring Boot, Dependency Management, Java/Kotlin |

### 2.18 Frontend Pages P6 (Billing, Webhooks, Chat IA)

Pages Angular standalone ajoutées pour les fonctionnalités P6 :

| Page | Route | Description |
|------|-------|-------------|
| Billing | `/settings/billing` | Plans (FREE/PRO/ENTERPRISE), checkout Stripe, portail, factures, bannière trial |
| Webhooks | `/settings/webhooks` | Liste, CRUD, formulaire création, test, toggle actif, historique envois |
| AI Chat | `/ai/chat` | Chat streaming SSE, suggestions, messages temps réel, input Enter |

### 2.19 Publication Extensions (Marketplace)

Automatisation de la publication des extensions IDE :

| Marketplace | Extension | Méthode |
|-------------|-----------|---------|
| VS Code Marketplace | springforge-vscode | `vsce publish` via GitHub Actions |
| Open VSX Registry | springforge-vscode | `ovsx publish` (compatible Codium/Gitpod) |
| JetBrains Marketplace | springforge-intellij-plugin | `gradlew publishPlugin` via GitHub Actions |

### 2.20 Configuration avancée des architectures (Étape 5 du Wizard)

L'étape 5 du wizard s'adapte dynamiquement selon l'architecture choisie à l'étape 4. Chaque architecture offre des options de configuration spécifiques :

#### Microservices — Configuration multi-services complète

| Onglet | Options |
|--------|---------|
| **Services** | Définition de chaque microservice (nom, description, port), ajout/suppression dynamique, choix de bases de données par service (PostgreSQL, MySQL, MongoDB, Redis, Cassandra, Neo4j) avec purpose (Primary Store, Cache, Search, Event Store) |
| **Communication** | Synchrone (REST/gRPC) entre services, Asynchrone (Kafka/RabbitMQ) avec topics, types d'événements, format de sérialisation (JSON/Avro/Protobuf) |
| **Résilience** | Circuit Breaker (seuil d'échec, durée ouverture), Retry (max tentatives, délai), Timeout (durée), Bulkhead (threads max), Rate Limit (requêtes par seconde) — configurable par service |
| **Infrastructure** | Service Discovery (Eureka/Consul), API Gateway (rate limiting, CORS, auth par route), Config Server (profils dev/staging/prod), Secret Management (Vault/Env), Orchestration Saga (Choreography/Orchestration) |
| **Observabilité** | Tracing distribué (Zipkin/Jaeger), Métriques (Prometheus), Logging centralisé (ELK/Loki) |

#### DDD (Domain-Driven Design)

| Option | Description |
|--------|-------------|
| Bounded Contexts | Définition de chaque contexte (nom, agrégats, événements domaine, repositories) |
| Context Mapping | Relations entre contextes (Shared Kernel, Anti-Corruption Layer, Customer-Supplier, Conformist, Open Host) |
| Shared Kernel | Module partagé entre contextes |
| Anti-Corruption Layers | Couches de protection entre contextes |

#### CQRS (Command Query Responsibility Segregation)

| Option | Description |
|--------|-------------|
| Command Store | Type de base pour les écritures (PostgreSQL, MySQL, MongoDB) |
| Query Store | Type de base pour les lectures (PostgreSQL, MongoDB, Elasticsearch) |
| Event Store | Stockage des événements (PostgreSQL, EventStoreDB, Kafka) |
| Modèles séparés | Activation de la séparation commandes/queries |
| Event Replay | Capacité de rejouer les événements |
| Projections | Définition des projections (nom, source, target) |

#### Event-Driven

| Option | Description |
|--------|-------------|
| Broker | Choix du message broker (Kafka/RabbitMQ) |
| Events | Définition des événements (nom, topic, schéma) |
| Schema Registry | Activation du registre de schémas (Avro/Protobuf/JSON Schema) |
| Dead Letter Queue | Gestion des messages en erreur |
| Ordering | Garantie d'ordre des messages |
| Consumer Groups | Définition des groupes de consommateurs |

#### Hexagonal (Ports & Adapters)

| Option | Description |
|--------|-------------|
| Ports | Définition des ports (nom, type: inbound/outbound) |
| Adapters | Choix des adaptateurs par port (REST, gRPC, JPA, Redis, Kafka...) |
| Domain Modules | Organisation des modules domaine |
| Domain Events | Activation des événements de domaine |

#### Modulith (Monolithe Modulaire)

| Option | Description |
|--------|-------------|
| Modules | Définition des modules (nom, packages exposés, packages internes, dépendances) |
| ArchUnit Enforcement | Vérification des règles d'architecture au build |
| Allowed Dependencies | Matrice des dépendances autorisées entre modules |
| Event Publishing | Communication inter-modules par événements |

#### Monolithic

| Option | Description |
|--------|-------------|
| Packaging | JAR ou WAR |
| Embedded Server | Tomcat, Jetty, ou Undertow |
| Modules | Liste des modules fonctionnels |
| Scheduling | Activation des tâches planifiées |
| Caching | Activation du cache applicatif |

#### Layered (Architecture en couches)

| Option | Description |
|--------|-------------|
| Layers | Couches actives (Controller, Service, Repository, DTO, Mapper) |
| Strict Layering | Interdit les accès cross-layer |
| Validation | Activation de la validation Bean |
| Swagger | Génération documentation OpenAPI |

### 2.21 Diagramme d'architecture auto-généré

À l'étape 10 (Review), un diagramme SVG est automatiquement généré représentant :
- **Nœuds** : chaque service/module avec son nom et ses bases de données (badges colorés)
- **Connexions synchrones** : traits pleins (REST/gRPC) avec label du protocole
- **Connexions asynchrones** : traits en pointillés (Kafka/RabbitMQ) avec nom du topic
- **Layout automatique** : positionnement intelligent des nœuds pour minimiser les croisements

Le diagramme est interactif et se met à jour en temps réel quand la configuration change.

---

## 3. Parcours utilisateur

### 3.1 Parcours "Développeur — Première génération"

```
1. Accès à springforge.io
2. Création de compte (ou SSO via Keycloak)
3. Dashboard → Bouton "Nouveau Projet"
4. Wizard étapes 1-4 : metadata, versions, build tool, architecture
5. Étape 5 : Configuration architecture (ex: définir 3 microservices avec leurs DB)
6. Étapes 6-9 : dépendances, sécurité, infrastructure, options
7. Étape 10 : Review avec diagramme d'architecture → Clic "Générer"
8. Barre de progression temps réel (WebSocket)
9. Téléchargement ZIP automatique
10. Import dans IDE → Projet fonctionnel avec architecture complète
```

### 3.2 Parcours "Tech Lead — Publication blueprint"

```
1. Création d'un projet type via le wizard
2. Export de la configuration comme blueprint
3. Accès Marketplace → "Publier"
4. Remplissage metadata (titre, description, tags)
5. Soumission pour validation
6. Admin approuve → Visible dans le catalogue
7. L'équipe peut utiliser ce blueprint standardisé
```

### 3.3 Parcours "Admin — Gestion quotidienne"

```
1. Connexion avec rôle ADMIN
2. Dashboard : vue d'ensemble activité
3. Vérification blueprints en attente → Approuver/Rejeter
4. Consultation audit logs si incident
5. Gestion des utilisateurs si besoin
```

### 3.4 Parcours "Développeur VS Code — Génération depuis l'IDE"

```
1. Installation extension SpringForge depuis le Marketplace VS Code
2. Configuration Settings : URL serveur + API key
3. Ctrl+Shift+P → "SpringForge: Generate Project"
4. Wizard multi-step dans panel webview
5. Clic "Generate" → Barre de statut affiche la progression
6. Projet téléchargé → Ouverture automatique dans le workspace
```

### 3.5 Parcours "Tech Lead — Configuration webhooks"

```
1. Accès Settings Organisation → Webhooks
2. Clic "Nouveau Webhook"
3. Configuration : nom, URL, canal (Webhook/Slack), événements souscrits
4. Optionnel : secret token pour signature HMAC
5. Clic "Test" → Vérification envoi réussi
6. Activation → Notifications automatiques sur événements
7. Consultation historique des envois si problème
```

### 3.6 Parcours "CTO — Upgrade plan et IA"

```
1. Dashboard → Quotas presque atteints
2. Clic "Upgrade" → Redirection Stripe Checkout
3. Paiement → Plan PRO activé immédiatement
4. Accès à l'assistant IA : code review, suggestions
5. Chat IA pour optimiser l'architecture du projet
6. Portail Stripe pour gérer la facturation
```

---

## 4. Règles métier

### 4.1 Génération de projets

| Règle | Description |
|-------|-------------|
| RG-GEN-01 | Un projet doit avoir un nom unique par organisation |
| RG-GEN-02 | Le package name doit respecter les conventions Java (lowercase, dots) |
| RG-GEN-03 | La version Java sélectionnée doit être compatible avec la version Spring Boot |
| RG-GEN-04 | Les dépendances incompatibles sont bloquées avec explication |
| RG-GEN-05 | La génération est limitée par le quota du plan (5/50/illimité) |
| RG-GEN-06 | Le ZIP généré doit compiler sans erreur |
| RG-GEN-07 | Chaque microservice doit avoir au moins une base PRIMARY_STORE |
| RG-GEN-08 | Les noms de services/modules/contextes doivent être uniques dans un projet |
| RG-GEN-09 | Les ports de microservices doivent être uniques et dans la plage valide |
| RG-GEN-10 | Les communications doivent référencer des services existants |
| RG-GEN-11 | Au moins un agrégat est requis par bounded context (DDD) |
| RG-GEN-12 | Les dépendances entre modules Modulith doivent être déclarées explicitement |

### 4.2 Marketplace

| Règle | Description |
|-------|-------------|
| RG-MKT-01 | Un blueprint doit être approuvé par un admin avant publication |
| RG-MKT-02 | La notation est limitée à 1 vote par utilisateur par blueprint |
| RG-MKT-03 | Un blueprint supprimé reste accessible pour les projets déjà générés |
| RG-MKT-04 | Le versioning suit semver (MAJOR.MINOR.PATCH) |

### 4.3 Quotas & Plans

| Règle | Description |
|-------|-------------|
| RG-QTA-01 | Les quotas sont vérifiés AVANT le lancement de la génération |
| RG-QTA-02 | Un dépassement de quota retourne HTTP 429 avec message explicatif |
| RG-QTA-03 | Les quotas se réinitialisent le 1er de chaque mois |
| RG-QTA-04 | Un upgrade de plan prend effet immédiatement |
| RG-QTA-05 | Un downgrade prend effet à la fin de la période en cours |

### 4.4 Billing

| Règle | Description |
|-------|-------------|
| RG-BIL-01 | Le checkout Stripe redirige vers une session hébergée (pas de collecte carte côté serveur) |
| RG-BIL-02 | Le plan est mis à jour uniquement sur réception du webhook Stripe (pas sur le retour checkout) |
| RG-BIL-03 | Un impayé (`invoice.payment_failed`) passe le statut en PAST_DUE |
| RG-BIL-04 | Une annulation (`customer.subscription.deleted`) passe en CANCELED, le plan FREE est réactivé |
| RG-BIL-05 | La signature du webhook Stripe est vérifiée avant traitement |
| RG-BIL-06 | Un nouvel utilisateur reçoit automatiquement un essai PRO de 14 jours |
| RG-BIL-07 | L'essai expire automatiquement (cron horaire) : downgrade vers FREE |
| RG-BIL-08 | Le paiement Stripe convertit l'essai en abonnement payant (flag trial retiré) |

### 4.5 Webhooks & Notifications

| Règle | Description |
|-------|-------------|
| RG-NOT-01 | Un webhook est dispatché de manière asynchrone (non-bloquant pour l'action principale) |
| RG-NOT-02 | Maximum 3 tentatives de livraison avec backoff exponentiel (2^n minutes) |
| RG-NOT-03 | Le payload est signé en HMAC-SHA256 si un secret token est configuré |
| RG-NOT-04 | Les delivery logs sont conservés 30 jours |
| RG-NOT-05 | Un webhook inactif (`active=false`) ne reçoit aucune notification |
| RG-NOT-06 | Le canal EMAIL utilise JavaMailSender (SMTP) avec template HTML responsive |
| RG-NOT-07 | L'email est conditionnel (`@ConditionalOnProperty notification.email.enabled=true`) |

### 4.6 Intelligence Artificielle

| Règle | Description |
|-------|-------------|
| RG-AI-01 | Le provider LLM est configurable (Claude ou OpenAI) via propriété Spring |
| RG-AI-02 | Les réponses longues sont streamées en SSE (Server-Sent Events) |
| RG-AI-03 | Les clés API LLM sont stockées en variables d'environnement (jamais en dur) |
| RG-AI-04 | Le modèle et la température sont configurables par requête |

### 4.7 Sécurité

| Règle | Description |
|-------|-------------|
| RG-SEC-01 | Rate limiting : 60 requêtes/minute par client |
| RG-SEC-02 | Les API keys sont hashées SHA-256, jamais stockées en clair |
| RG-SEC-03 | Les tokens JWT expirent après 1h (refresh token : 7j) |
| RG-SEC-04 | Les endpoints admin nécessitent le rôle ADMIN |
| RG-SEC-05 | Les entrées utilisateur sont validées contre injection |
| RG-SEC-06 | Les endpoints webhook Stripe sont en permitAll (authentification par signature) |
| RG-SEC-07 | Le stockage MinIO utilise des credentials séparés (MINIO_ACCESS_KEY / SECRET_KEY) |

---

## 5. Matrice des rôles et permissions

| Action | VIEWER | USER | ADMIN | OWNER |
|--------|--------|------|-------|-------|
| Voir blueprints publics | ✅ | ✅ | ✅ | ✅ |
| Générer un projet | ❌ | ✅ | ✅ | ✅ |
| Publier un blueprint | ❌ | ✅ | ✅ | ✅ |
| Voir dashboard admin | ❌ | ❌ | ✅ | ✅ |
| Gérer utilisateurs | ❌ | ❌ | ✅ | ✅ |
| Approuver blueprints | ❌ | ❌ | ✅ | ✅ |
| Gérer organisation | ❌ | ❌ | ❌ | ✅ |
| Créer/révoquer API keys | ❌ | ❌ | ❌ | ✅ |
| Changer plan | ❌ | ❌ | ❌ | ✅ |

---

## 6. Intégrations externes

| Système | Usage | Protocole | Obligatoire |
|---------|-------|-----------|-------------|
| PostgreSQL | Persistance données | JDBC | Oui |
| Redis | Cache applicatif | Redis Protocol | Oui |
| MinIO | Stockage objets (ZIPs générés) | S3 API | Non (fallback filesystem) |
| Stripe | Paiement, abonnements, factures | REST API + Webhooks | Non (mode FREE sans Stripe) |
| Claude API | Assistance IA (Anthropic) | REST + SSE | Non (optionnel) |
| OpenAI API | Assistance IA (OpenAI) | REST + SSE | Non (optionnel) |
| Keycloak | Authentification SSO | OpenID Connect | Non (optionnel) |
| Kafka | Événements async (génération, audit) | PLAINTEXT | Non (optionnel) |
| Prometheus | Collecte métriques | HTTP scrape | Non (monitoring) |
| Grafana | Visualisation métriques | HTTP | Non (monitoring) |
| GitHub Actions | CI/CD | Webhooks | Non (automation) |
| GHCR | Registry Docker | OCI | Non (release) |
| Let's Encrypt | Certificats SSL | ACME | Non (HTTPS prod) |
| Slack | Notifications sortantes | Incoming Webhook | Non (notification) |
| SMTP / SendGrid | Notifications email | SMTP | Non (notification) |

---

## 7. Modes de fonctionnement

### 7.1 Mode minimal (VPS petit budget)

Services requis : Backend + Frontend + PostgreSQL + Redis
RAM minimale : 4 Go (recommandé 8 Go pour génération microservices avancés)
Kafka et Keycloak désactivés.
Pool de génération configurable via `GENERATION_POOL_CORE` / `GENERATION_POOL_MAX`.

### 7.2 Mode complet (développement local)

Tous les 9 services actifs : Backend, Frontend, PostgreSQL, Redis, Kafka, Zookeeper, Schema Registry, Keycloak, Prometheus, Grafana.
RAM requise : 5 Go+

### 7.3 Mode production (Kubernetes)

Déploiement haute disponibilité avec auto-scaling, monitoring complet, et tous les services activés.

---

## 8. Contraintes et limites connues

| Contrainte | Impact | Mitigation |
|-----------|--------|-----------|
| Génération synchrone dans le ZIP | Temps limité à 60s | Pipeline async 4 phases |
| Redis single instance | Pas de HA cache | Acceptable en mode dégradé |
| Kafka optionnel en prod | Pas d'événements async | Génération fonctionne sans Kafka |
| Keycloak optionnel en prod | Auth JWT locale uniquement | SSO désactivé, JWT interne suffit |
| Plan FREE limité à 5 gen/mois | Friction pour adoption | Période d'essai PRO 14 jours |
| i18n 4 langues seulement | Couverture géographique limitée | Extensible (ajout fichier .properties + .json) |
| VPS 2 Go minimum | Pas de monitoring intégré | Monitoring via mode Kubernetes uniquement |
| MinIO requis en prod | Plus de stockage local en production | Fallback filesystem pour dev/test |
| LLM API payante | Coût par token (Claude/OpenAI) | Rate limiting par plan, pas de LLM en FREE |
| Webhooks fire-and-forget | Pas de garantie de livraison exacte | Retry 3x + backoff exponentiel |
| Stripe mode test en dev | Pas de vrais paiements | Basculer en live via clé API prod |
| Extension VS Code | Publication via workflow CI (tag ext-v*) | Secrets VSCE_PAT et OVSX_PAT requis |
