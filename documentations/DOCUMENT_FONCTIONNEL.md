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
| 4 | Architecture | Hexagonal, Layered, DDD, Microservices |
| 5 | Modules | Sélection des modules fonctionnels |
| 6 | Dépendances | Catalogue avec recherche et filtres |
| 7 | Sécurité | None, JWT, OAuth2 |
| 8 | Infrastructure | Docker, CI/CD, Base de données |
| 9 | Options | Paramètres avancés (profils, logging) |
| 10 | Review | Récapitulatif + bouton Générer |

**Caractéristiques UX :**
- Progression visuelle avec barre de steps
- Validation en temps réel à chaque étape
- Navigation avant/arrière sans perte de données
- Recommandations IA affichées en sidebar

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

---

## 3. Parcours utilisateur

### 3.1 Parcours "Développeur — Première génération"

```
1. Accès à springforge.io
2. Création de compte (ou SSO via Keycloak)
3. Dashboard → Bouton "Nouveau Projet"
4. Wizard 10 étapes avec recommandations IA
5. Review → Clic "Générer"
6. Barre de progression temps réel (WebSocket)
7. Téléchargement ZIP automatique
8. Import dans IDE → Projet fonctionnel
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

### 4.4 Sécurité

| Règle | Description |
|-------|-------------|
| RG-SEC-01 | Rate limiting : 60 requêtes/minute par client |
| RG-SEC-02 | Les API keys sont hashées SHA-256, jamais stockées en clair |
| RG-SEC-03 | Les tokens JWT expirent après 1h (refresh token : 7j) |
| RG-SEC-04 | Les endpoints admin nécessitent le rôle ADMIN |
| RG-SEC-05 | Les entrées utilisateur sont validées contre injection |

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

| Système | Usage | Protocole |
|---------|-------|-----------|
| Keycloak | Authentification SSO | OpenID Connect |
| Kafka | Événements async (génération, audit) | PLAINTEXT |
| Redis | Cache applicatif | Redis Protocol |
| PostgreSQL | Persistance données | JDBC |
| Prometheus | Collecte métriques | HTTP scrape |
| Grafana | Visualisation | HTTP |
| GitHub Actions | CI/CD | Webhooks |
| GHCR | Registry Docker | OCI |

---

## 7. Contraintes et limites connues

| Contrainte | Impact | Mitigation |
|-----------|--------|-----------|
| Génération synchrone dans le ZIP | Temps limité à 60s | Pipeline async 4 phases |
| Redis single instance | Pas de HA cache | Acceptable en mode dégradé (fallback DB) |
| Kafka single broker | Pas de réplication | Suffisant pour le volume actuel |
| Plan FREE limité à 5 gen/mois | Friction pour adoption | Période d'essai PRO 14 jours |
| i18n 4 langues seulement | Couverture géographique limitée | Extensible facilement (ajout fichier .properties + .json) |
