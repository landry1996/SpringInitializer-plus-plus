# Guide Complet de Lancement - SpringForge

## Table des matieres

1. [Prerequisites](#1-prerequisites)
2. [Lancement Local (Developpement)](#2-lancement-local-developpement)
3. [Lancement Local sans Docker](#3-lancement-local-sans-docker)
4. [Deploiement sur VPS (Production)](#4-deploiement-sur-vps-production)
5. [Configuration SSL/HTTPS](#5-configuration-sslhttps)
6. [Operations courantes](#6-operations-courantes)
7. [Troubleshooting](#7-troubleshooting)
8. [Architecture des services](#8-architecture-des-services)

---

## 1. Prerequisites

### Pour le developpement local

| Outil | Version minimale | Verification |
|-------|-----------------|--------------|
| Docker | 24.0+ | `docker --version` |
| Docker Compose | 2.20+ | `docker compose version` |
| Git | 2.30+ | `git --version` |
| Java (optionnel) | 21 | `java -version` |
| Node.js (optionnel) | 20 | `node --version` |
| Maven (optionnel) | 3.9+ | `mvn -version` |

> Java/Node/Maven ne sont necessaires que si vous lancez les services hors Docker.

### Pour le VPS

| Outil | Version minimale |
|-------|-----------------|
| Ubuntu/Debian | 22.04+ |
| RAM | 4 Go minimum (8 Go recommande) |
| CPU | 2 vCPU minimum (4 vCPU recommande) |
| Disque | 40 Go minimum (80 Go recommande) |
| Docker | 24.0+ |
| Docker Compose | 2.20+ |
| Nom de domaine | Pointe vers l'IP du VPS (pour SSL) |

> **Note** : La generation de projets microservices avances (avec multiple bases de donnees, messaging, observabilite) necessite plus de ressources. Augmenter `GENERATION_POOL_CORE` et `GENERATION_POOL_MAX` dans le `.env` si beaucoup d'utilisateurs generent en parallele.

---

## 2. Lancement Local (Developpement)

### 2.1 Cloner le projet

```bash
git clone https://github.com/landry1996/SpringInitializer-plus-plus.git
cd SpringInitializer-plus-plus
```

### 2.2 Demarrer tous les services avec Docker Compose

```bash
docker compose up -d
```

Cela demarre **11 services** :

| Service | Port | URL |
|---------|------|-----|
| Backend (Spring Boot) | 8080 | http://localhost:8080 |
| Frontend (Angular) | 4200 | http://localhost:4200 |
| PostgreSQL | 5432 | `jdbc:postgresql://localhost:5432/springforge` |
| Redis | 6379 | `redis://localhost:6379` |
| Kafka | 9092 | `kafka://localhost:9092` |
| Zookeeper | 2181 | - |
| Schema Registry | 8081 | http://localhost:8081 |
| MinIO (S3) | 9000/9001 | http://localhost:9001 (console) |
| Keycloak (Auth) | 8180 | http://localhost:8180 |
| Prometheus | 9090 | http://localhost:9090 |
| Grafana | 3000 | http://localhost:3000 |

### 2.3 Verifier que tout fonctionne

```bash
# Voir l'etat des conteneurs
docker compose ps

# Verifier la sante du backend
curl http://localhost:8080/actuator/health

# Voir les logs du backend
docker compose logs -f backend

# Voir les logs du frontend
docker compose logs -f frontend
```

### 2.4 Identifiants par defaut (developpement)

| Service | Utilisateur | Mot de passe |
|---------|-------------|--------------|
| PostgreSQL | springforge | springforge |
| Keycloak Admin | admin | admin |
| MinIO Console | minioadmin | minioadmin |
| Grafana | admin | admin |

### 2.5 Acceder a l'application

- **Frontend** : http://localhost:4200
- **API Swagger** : http://localhost:8080/swagger-ui.html
- **Health check** : http://localhost:8080/actuator/health

### 2.6 Arreter les services

```bash
docker compose down

# Pour supprimer aussi les volumes (reset complet des donnees)
docker compose down -v
```

---

## 3. Lancement Local sans Docker

Si vous preferez lancer les services individuellement (utile pour le debug).

### 3.1 Prerequis infrastructure

Vous avez besoin au minimum de PostgreSQL et Redis. Installez-les localement ou lancez uniquement ces services via Docker :

```bash
docker compose up -d postgres redis
```

### 3.2 Lancer le Backend

```bash
cd springforge-backend

# Compiler
mvn clean compile -DskipTests

# Lancer avec le profil dev
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Le profil `dev` utilise ces parametres :
- Base de donnees : `localhost:5432/springforge` (user: springforge, pass: springforge)
- Redis : `localhost:6379`
- Kafka : desactive (pas necessaire en dev)
- Logs en mode DEBUG

### 3.3 Lancer le Frontend

```bash
cd springforge-frontend

# Installer les dependances
npm ci

# Lancer le serveur de developpement
npm start
```

L'application Angular est accessible sur http://localhost:4200 et proxy les appels API vers le backend sur le port 8080.

### 3.4 Lancer les tests

```bash
# Tests backend
cd springforge-backend
mvn verify

# Tests frontend
cd springforge-frontend
npm test
```

---

## 4. Deploiement sur VPS (Production)

### 4.1 Preparer le serveur

#### Connexion SSH

```bash
ssh root@VOTRE_IP_VPS
```

#### Installer Docker et Docker Compose

```bash
# Mettre a jour le systeme
apt update && apt upgrade -y

# Installer les dependances
apt install -y ca-certificates curl gnupg lsb-release

# Ajouter le repo Docker
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# Installer Docker
apt update
apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Verifier
docker --version
docker compose version
```

#### Configurer le pare-feu

```bash
# Installer ufw
apt install -y ufw

# Regles de base
ufw default deny incoming
ufw default allow outgoing

# Autoriser SSH, HTTP, HTTPS
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp

# Activer
ufw enable
ufw status
```

#### Creer un utilisateur dedie (recommande)

```bash
adduser deploy
usermod -aG docker deploy
su - deploy
```

### 4.2 Cloner le projet

```bash
cd /opt
git clone https://github.com/landry1996/SpringInitializer-plus-plus.git springforge
cd springforge
```

### 4.3 Configurer l'environnement

```bash
cp .env.example .env
nano .env
```

Remplissez les variables :

```bash
# OBLIGATOIRE : votre nom de domaine
DOMAIN=springforge.votredomaine.com

# OBLIGATOIRE : mot de passe fort pour PostgreSQL
DB_PASSWORD=VoTr3_MoT_dE_pAsS3_Tr3s_F0rT_!@#$

# OBLIGATOIRE : cle JWT (au moins 64 caracteres)
# Generer avec : openssl rand -base64 64
JWT_SECRET=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# OBLIGATOIRE : URL de votre frontend
CORS_ORIGINS=https://springforge.votredomaine.com

# Chemin de generation (ne pas modifier)
GENERATION_OUTPUT_DIR=/app/generated

# Pool de threads pour la generation avancee (microservices multi-DB, etc.)
GENERATION_POOL_CORE=5
GENERATION_POOL_MAX=10

# OPTIONNEL : Kafka (desactive par defaut)
KAFKA_ENABLED=false

# OPTIONNEL : Keycloak (desactive par defaut)
KEYCLOAK_ENABLED=false

# OPTIONNEL : Stockage (filesystem par defaut)
STORAGE_TYPE=filesystem

# OPTIONNEL : Stripe (pour la facturation)
STRIPE_SECRET_KEY=sk_live_xxxxx
STRIPE_PRO_PRICE_ID=price_xxxxx
STRIPE_ENTERPRISE_PRICE_ID=price_xxxxx
STRIPE_WEBHOOK_SECRET=whsec_xxxxx

# OPTIONNEL : IA (pour les recommandations)
AI_PROVIDER=claude
ANTHROPIC_API_KEY=sk-ant-xxxxx

# OPTIONNEL : Notifications email
NOTIFICATION_EMAIL_ENABLED=false
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=votre@email.com
SMTP_PASSWORD=app-password-gmail
EMAIL_FROM=notifications@votredomaine.com
```

> Pour generer un JWT_SECRET securise :
> ```bash
> openssl rand -base64 64 | tr -d '\n'
> ```

### 4.4 Premier deploiement

```bash
chmod +x deploy.sh
./deploy.sh init
```

Ce que fait `deploy.sh init` :
1. Verifie que `.env` existe (sinon le cree depuis `.env.example` et s'arrete)
2. Copie la config nginx sans SSL (`default-nossl.conf.example`) si pas de `default.conf`
3. Build les images Docker (backend + frontend)
4. Demarre tous les services : nginx, backend, frontend, postgres, redis, certbot
5. Attend que le backend soit sain (health check en boucle, 30 tentatives)
6. Affiche les URLs d'acces

**Temps de build estime** : 5-10 minutes (premiere fois, telechargement des dependances Maven + npm).

### 4.5 Verifier le deploiement

```bash
# Status de tous les services
./deploy.sh status

# Logs du backend
./deploy.sh logs backend

# Logs du frontend
./deploy.sh logs frontend

# Test direct
curl http://VOTRE_IP/actuator/health
```

A ce stade, l'application est accessible en HTTP sur le port 80.

---

## 5. Configuration SSL/HTTPS

### 5.1 Prerequis DNS

Assurez-vous que votre domaine pointe vers l'IP du VPS :

```bash
# Verifier le DNS
dig +short springforge.votredomaine.com
# Doit retourner l'IP de votre VPS
```

### 5.2 Obtenir le certificat Let's Encrypt

```bash
./deploy.sh ssl springforge.votredomaine.com votre@email.com
```

Ce que ca fait :
1. Lance certbot via Docker pour obtenir un certificat SSL
2. Met a jour la config nginx pour activer HTTPS
3. Recharge nginx

### 5.3 Renouvellement automatique

Le service `certbot` est configure pour renouveler automatiquement les certificats toutes les 12 heures. Aucune action manuelle necessaire.

### 5.4 Verifier HTTPS

```bash
curl https://springforge.votredomaine.com/actuator/health
```

L'application redirige automatiquement HTTP vers HTTPS apres la configuration SSL.

---

## 6. Operations courantes

### 6.1 Mettre a jour l'application

Apres un `git push` sur `main` :

```bash
./deploy.sh update
```

Ce que ca fait :
1. `git pull origin main`
2. Rebuild les images Docker
3. Redeploit backend et frontend sans downtime (`--force-recreate --no-deps`)

### 6.2 Sauvegarder la base de donnees

```bash
./deploy.sh backup
```

- Cree un dump PostgreSQL dans `./backups/YYYYMMDD_HHMMSS/springforge_db.sql`
- Conserve les 7 dernieres sauvegardes automatiquement

### 6.3 Restaurer une sauvegarde

```bash
# Lister les sauvegardes disponibles
ls ./backups/

# Restaurer
docker exec -i springforge-db psql -U springforge springforge < ./backups/20260514_120000/springforge_db.sql
```

### 6.4 Voir les logs

```bash
# Tous les services
./deploy.sh logs

# Un service specifique
./deploy.sh logs backend
./deploy.sh logs frontend
./deploy.sh logs postgres
./deploy.sh logs redis
./deploy.sh logs nginx
```

### 6.5 Arreter l'application

```bash
./deploy.sh stop
```

### 6.6 Redemarrer un service

```bash
docker compose -f docker-compose.prod.yml -p springforge restart backend
```

### 6.7 Acceder a la base de donnees

```bash
docker exec -it springforge-db psql -U springforge springforge
```

### 6.8 Acceder au shell du backend

```bash
docker exec -it springforge-backend sh
```

### 6.9 Nettoyer les images Docker inutilisees

```bash
docker system prune -af
```

---

## 7. Troubleshooting

### Le backend ne demarre pas

```bash
# Verifier les logs
./deploy.sh logs backend

# Causes communes :
# 1. PostgreSQL pas encore pret -> attendre quelques secondes
# 2. Erreur dans .env (JWT_SECRET trop court, DB_PASSWORD incorrect)
# 3. Port 8080 deja utilise
```

### Erreur "connection refused" sur le frontend

```bash
# Verifier que le backend tourne
docker exec springforge-backend wget -qO- http://localhost:8080/actuator/health

# Verifier la config nginx
docker exec springforge-proxy nginx -t
```

### La base de donnees ne se connecte pas

```bash
# Verifier que postgres est sain
docker exec springforge-db pg_isready -U springforge

# Verifier les credentials
docker exec springforge-db psql -U springforge -c "SELECT 1;"
```

### Page blanche sur le frontend

```bash
# Verifier que le build Angular a reussi
docker logs springforge-frontend

# Reconstruire le frontend
docker compose -f docker-compose.prod.yml -p springforge build frontend
docker compose -f docker-compose.prod.yml -p springforge up -d frontend
```

### Certificat SSL expire ou erreur Let's Encrypt

```bash
# Forcer le renouvellement
docker compose -f docker-compose.prod.yml -p springforge run --rm certbot \
  certbot renew --force-renewal

# Recharger nginx
docker exec springforge-proxy nginx -s reload
```

### Disque plein

```bash
# Voir l'espace utilise
df -h

# Nettoyer Docker
docker system prune -af
docker volume prune -f

# Supprimer les anciennes sauvegardes
rm -rf ./backups/2026*
```

### Probleme de memoire (OOM)

```bash
# Verifier la memoire
docker stats --no-stream

# Les limites dans docker-compose.prod.yml :
# - backend: 1 Go
# - frontend: 128 Mo
# - postgres: 512 Mo
# - redis: 192 Mo
# Total minimum : ~2 Go pour les services principaux
```

### Flyway : erreur de migration

```bash
# Voir l'etat des migrations
docker exec springforge-backend java -jar app.jar --spring.flyway.info

# Reparer une migration echouee (ATTENTION: en dernier recours)
docker exec -it springforge-db psql -U springforge springforge \
  -c "DELETE FROM springforge.flyway_schema_history WHERE success = false;"
```

---

## 8. Architecture des services

### Environnement local (docker-compose.yml)

```
                    +------------------+
                    |  localhost:4200   |
                    |    (Frontend)     |
                    +--------+---------+
                             |
                    +--------v---------+
                    |  localhost:8080   |
                    |    (Backend)      |
                    +----+----+---+----+
                         |    |   |
            +------------+    |   +------------+
            |                 |                |
   +--------v---+    +-------v----+    +------v------+
   | PostgreSQL  |    |   Redis    |    |    Kafka    |
   | :5432       |    |   :6379    |    |    :9092    |
   +-------------+    +------------+    +-------------+
```

### Environnement production (docker-compose.prod.yml)

```
     Internet
        |
   +----v----+
   |  Nginx  |  :80 / :443
   |  Proxy  |  (SSL termination)
   +--+---+--+
      |   |
      |   +---------------------------+
      |                               |
+-----v------+               +-------v-------+
|  Backend   |               |   Frontend    |
|  (Spring)  |               |   (Angular)   |
| 1.5 Go RAM |               |   128 Mo RAM  |
| Generation:|               | Wizard 10 etapes
| - 8 archs  |               | Config dynamique
| - Multi-DB  |               | Diagramme SVG
| - Templates |               +---------------+
+---+----+---+
    |    |
+---v-+  +---v---+
| PG  |  | Redis |
| 16  |  |   7   |
+-----+  +-------+

  + Certbot (renouvellement SSL auto)
```

**Fonctionnalites de generation** :
- 8 architectures supportees (Microservices, Hexagonal, DDD, CQRS, Event-Driven, Modulith, Layered, Monolithic)
- Generation microservices avec choix de DB par service (PostgreSQL, MySQL, MongoDB, Redis, Cassandra, Neo4j)
- Communication inter-services (REST, gRPC, Kafka, RabbitMQ)
- Resilience (Circuit Breaker, Retry, Timeout) et observabilite (Zipkin, Jaeger, Prometheus)

### Ports exposes en production

| Port | Service | Acces |
|------|---------|-------|
| 80 | Nginx (HTTP -> redirect HTTPS) | Public |
| 443 | Nginx (HTTPS) | Public |
| 22 | SSH | Admin |
| 5432 | PostgreSQL | Interne uniquement |
| 6379 | Redis | Interne uniquement |
| 8080 | Backend | Interne (via Nginx) |

> En production, seuls les ports 80, 443 et 22 sont exposes publiquement. PostgreSQL, Redis et le backend sont accessibles uniquement via le reseau Docker interne.

---

## Resume des commandes

### Local

```bash
# Tout demarrer
docker compose up -d

# Tout arreter
docker compose down

# Rebuild apres modification
docker compose up -d --build

# Logs
docker compose logs -f backend
```

### VPS / Production

```bash
# Premier deploiement
./deploy.sh init

# Mettre a jour
./deploy.sh update

# SSL
./deploy.sh ssl mondomaine.com email@example.com

# Status
./deploy.sh status

# Logs
./deploy.sh logs [service]

# Sauvegarde
./deploy.sh backup

# Arreter
./deploy.sh stop
```
