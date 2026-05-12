# SpringForge — Guide de Déploiement Complet

Ce guide détaille pas à pas comment lancer SpringForge en local (développement) et sur un VPS (production).

---

## Table des matières

1. [Prérequis](#1-prérequis)
2. [Lancement en local (développement)](#2-lancement-en-local-développement)
3. [Déploiement sur VPS (production)](#3-déploiement-sur-vps-production)
4. [Commandes utiles](#4-commandes-utiles)
5. [Dépannage](#5-dépannage)

---

## 1. Prérequis

### Pour le développement local

| Outil | Version minimum | Vérification |
|-------|----------------|--------------|
| Docker | 24+ | `docker --version` |
| Docker Compose | 2.20+ | `docker compose version` |
| Git | 2.30+ | `git --version` |

> **Note** : Docker Desktop (Windows/Mac) inclut Docker Compose. Sur Linux, installez-les séparément.

### Pour le VPS (production)

| Outil | Version minimum |
|-------|----------------|
| Ubuntu/Debian | 22.04+ (ou équivalent) |
| Docker | 24+ |
| Docker Compose | 2.20+ |
| Git | 2.30+ |

| Ressource | Minimum | Recommandé |
|-----------|---------|------------|
| RAM | 2 Go | 4 Go |
| CPU | 1 vCPU | 2 vCPU |
| Disque | 20 Go | 40 Go |
| Réseau | Port 80 et 443 ouverts | — |

---

## 2. Lancement en local (développement)

### Étape 1 : Cloner le projet

```bash
git clone https://github.com/landry1996/SpringInitializer-plus-plus.git
cd SpringInitializer-plus-plus
```

### Étape 2 : Lancer la stack complète

```bash
docker compose up -d
```

Cette commande va :
- Télécharger toutes les images Docker nécessaires (première fois uniquement, ~5 min)
- Construire les images backend et frontend
- Démarrer les 9 services

### Étape 3 : Vérifier que tout fonctionne

```bash
# Voir l'état de tous les services
docker compose ps
```

Tous les services doivent être en état `running` ou `healthy`.

Attendez environ 30 secondes que le backend démarre complètement, puis vérifiez :

```bash
# Tester le health check du backend
curl http://localhost:8080/actuator/health
```

Réponse attendue :
```json
{"status":"UP"}
```

### Étape 4 : Accéder aux services

Ouvrez votre navigateur :

| Service | URL | Identifiants |
|---------|-----|-------------|
| **Frontend (Angular)** | http://localhost:4200 | — |
| **API Backend** | http://localhost:8080/api/v1 | — |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | — |
| **Keycloak (Auth)** | http://localhost:8180 | admin / admin |
| **Grafana (Monitoring)** | http://localhost:3000 | admin / admin |
| **Prometheus** | http://localhost:9090 | — |
| **PostgreSQL** | localhost:5432 | springforge / springforge |
| **Redis** | localhost:6379 | — |
| **Kafka** | localhost:9092 | — |

### Étape 5 : Utiliser l'application

1. Ouvrez http://localhost:4200
2. Cliquez sur "Nouveau Projet" ou accédez au Wizard
3. Suivez les 10 étapes pour configurer votre projet Spring Boot
4. Cliquez sur "Générer" et téléchargez le ZIP

### Arrêter la stack locale

```bash
# Arrêter tous les services (les données sont conservées)
docker compose stop

# Arrêter et supprimer les conteneurs (les volumes/données sont conservés)
docker compose down

# Tout supprimer y compris les données (ATTENTION : perte de données)
docker compose down -v
```

### Relancer après un arrêt

```bash
docker compose up -d
```

Les données PostgreSQL et Redis sont persistées dans des volumes Docker, elles ne sont pas perdues entre les redémarrages.

### Reconstruire après modification du code

```bash
# Reconstruire et relancer le backend uniquement
docker compose up -d --build backend

# Reconstruire et relancer le frontend uniquement
docker compose up -d --build frontend

# Tout reconstruire
docker compose up -d --build
```

---

## 3. Déploiement sur VPS (production)

### Étape 1 : Préparer le VPS

Connectez-vous à votre VPS en SSH :

```bash
ssh utilisateur@votre-ip-vps
```

#### Installer Docker (si pas déjà fait)

```bash
# Mettre à jour le système
sudo apt update && sudo apt upgrade -y

# Installer Docker
curl -fsSL https://get.docker.com | sh

# Ajouter votre utilisateur au groupe docker (évite d'utiliser sudo)
sudo usermod -aG docker $USER

# Déconnectez-vous et reconnectez-vous pour appliquer
exit
ssh utilisateur@votre-ip-vps

# Vérifier l'installation
docker --version
docker compose version
```

#### Ouvrir les ports nécessaires (firewall)

```bash
# Si vous utilisez UFW (Ubuntu)
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 22/tcp
sudo ufw enable

# Vérifier
sudo ufw status
```

### Étape 2 : Cloner le projet sur le VPS

```bash
cd /opt
sudo git clone https://github.com/landry1996/SpringInitializer-plus-plus.git springforge
sudo chown -R $USER:$USER /opt/springforge
cd /opt/springforge
```

### Étape 3 : Configurer les variables d'environnement

```bash
# Copier le fichier d'exemple
cp .env.example .env

# Éditer le fichier
nano .env
```

**Remplissez chaque variable :**

```bash
# Votre nom de domaine (ou IP si pas de domaine)
DOMAIN=votre-domaine.com

# Mot de passe PostgreSQL — CHANGEZ CECI avec un mot de passe fort
DB_PASSWORD=MonMotDePasse_Tres_Securise_2024!

# Secret JWT — MINIMUM 64 caractères aléatoires
# Générer avec : openssl rand -base64 64
JWT_SECRET=collez_ici_la_sortie_de_openssl_rand_base64_64

# Domaines autorisés pour les requêtes frontend → backend
# Si vous avez un domaine :
CORS_ORIGINS=https://votre-domaine.com
# Si vous n'avez pas de domaine (utiliser l'IP) :
# CORS_ORIGINS=http://votre-ip-vps

# Répertoire de génération (laisser par défaut)
GENERATION_OUTPUT_DIR=/app/generated

# Kafka et Keycloak désactivés par défaut (économie de RAM)
KAFKA_ENABLED=false
SPRING_KAFKA_BOOTSTRAP_SERVERS=
KEYCLOAK_ENABLED=false
KEYCLOAK_ISSUER_URI=
```

**Générer un JWT_SECRET sécurisé :**

```bash
openssl rand -base64 64
```

Copiez le résultat dans le champ `JWT_SECRET` de votre `.env`.

### Étape 4 : Premier déploiement

```bash
# Rendre le script exécutable
chmod +x deploy.sh

# Lancer le déploiement
./deploy.sh init
```

Le script va :
1. Vérifier que Docker est installé
2. Utiliser la config Nginx HTTP (sans SSL pour l'instant)
3. Construire les images Docker (~5-10 min la première fois)
4. Démarrer les services
5. Vérifier que le backend est en bonne santé

**Résultat attendu :**

```
[SpringForge] Requirements OK
[SpringForge] Using HTTP-only nginx config (run './deploy.sh ssl' after to enable HTTPS)
[SpringForge] Building images...
[SpringForge] Starting services...
[SpringForge] Waiting for backend health check...
[SpringForge] Backend is healthy!
[SpringForge] Deployment complete!

  Frontend:  http://123.45.67.89
  API:       http://123.45.67.89/api/v1
  Health:    http://123.45.67.89/actuator/health
```

### Étape 5 : Vérifier le déploiement

```bash
# État des services
./deploy.sh status

# Voir les logs en temps réel
./deploy.sh logs

# Voir les logs d'un service spécifique
./deploy.sh logs backend
./deploy.sh logs frontend
./deploy.sh logs postgres
```

Ouvrez votre navigateur et allez sur `http://votre-ip-vps` — vous devriez voir l'interface SpringForge.

### Étape 6 : Activer HTTPS (recommandé)

**Prérequis** : Vous devez avoir un nom de domaine pointant vers l'IP de votre VPS (enregistrement DNS de type A).

```bash
# Remplacez par votre domaine et email
./deploy.sh ssl votre-domaine.com votre-email@example.com
```

Le script va :
1. Demander un certificat Let's Encrypt (gratuit)
2. Configurer Nginx pour HTTPS
3. Recharger la configuration

Votre site est maintenant accessible en HTTPS : `https://votre-domaine.com`

> **Note** : Le certificat se renouvelle automatiquement (le conteneur certbot s'en charge).

### Étape 7 : Configurer les sauvegardes automatiques

```bash
# Faire une sauvegarde manuelle
./deploy.sh backup
```

**Pour automatiser les sauvegardes quotidiennes :**

```bash
# Ouvrir le crontab
crontab -e

# Ajouter cette ligne (sauvegarde tous les jours à 3h du matin)
0 3 * * * cd /opt/springforge && ./deploy.sh backup >> /var/log/springforge-backup.log 2>&1
```

Les sauvegardes sont stockées dans `./backups/` avec rotation automatique (les 7 dernières sont conservées).

---

## 4. Commandes utiles

### En local (développement)

| Action | Commande |
|--------|----------|
| Démarrer tous les services | `docker compose up -d` |
| Arrêter tous les services | `docker compose stop` |
| Voir les logs | `docker compose logs -f` |
| Logs d'un service | `docker compose logs -f backend` |
| Reconstruire une image | `docker compose up -d --build backend` |
| Accéder au conteneur backend | `docker exec -it springforge-backend sh` |
| Accéder à PostgreSQL | `docker exec -it springforge-db psql -U springforge` |
| Accéder à Redis | `docker exec -it springforge-redis redis-cli` |
| Supprimer tout (données incluses) | `docker compose down -v` |

### Sur VPS (production)

| Action | Commande |
|--------|----------|
| Premier déploiement | `./deploy.sh init` |
| Mettre à jour | `./deploy.sh update` |
| Activer HTTPS | `./deploy.sh ssl domaine.com email@ex.com` |
| Voir le statut | `./deploy.sh status` |
| Voir les logs | `./deploy.sh logs` |
| Logs d'un service | `./deploy.sh logs backend` |
| Sauvegarder la DB | `./deploy.sh backup` |
| Arrêter | `./deploy.sh stop` |
| Redémarrer | `docker compose -f docker-compose.prod.yml up -d` |

### Mettre à jour le code sur le VPS

Quand vous avez poussé de nouvelles modifications sur GitHub :

```bash
cd /opt/springforge
./deploy.sh update
```

Cette commande fait un `git pull`, reconstruit les images, et redémarre les services modifiés.

---

## 5. Dépannage

### Le backend ne démarre pas

```bash
# Voir les logs détaillés du backend
docker compose logs backend

# Ou en production
./deploy.sh logs backend
```

**Erreurs courantes :**

| Erreur | Cause | Solution |
|--------|-------|----------|
| `Connection refused: postgres:5432` | PostgreSQL pas encore prêt | Attendre 10s et réessayer, vérifier `docker compose ps` |
| `Password authentication failed` | Mauvais mot de passe DB | Vérifier `.env` → `DB_PASSWORD` |
| `JWT_SECRET must be at least 64 characters` | Secret trop court | Régénérer avec `openssl rand -base64 64` |
| `Connection refused: redis:6379` | Redis pas démarré | `docker compose up -d redis` |
| `OutOfMemoryError` | Pas assez de RAM | Augmenter la RAM du VPS ou réduire `mem_limit` |

### Le frontend affiche une page blanche

```bash
# Vérifier que le frontend est démarré
docker compose ps frontend

# Voir les logs Nginx du frontend
docker compose logs frontend

# En prod, vérifier le proxy Nginx
docker exec springforge-proxy nginx -t
```

**Cause fréquente** : Le frontend essaie d'appeler l'API mais celle-ci n'est pas accessible. Vérifiez que le backend est healthy.

### Impossible d'accéder depuis l'extérieur (VPS)

```bash
# Vérifier que les ports sont ouverts
sudo ufw status

# Vérifier que Nginx écoute
docker exec springforge-proxy ss -tlnp

# Tester localement sur le VPS
curl http://localhost/actuator/health
```

**Si ça marche en local mais pas depuis l'extérieur** : Le firewall de votre hébergeur (panel web OVH, Hetzner, DigitalOcean...) bloque peut-être les ports. Vérifiez les règles réseau dans l'interface de votre hébergeur.

### Erreur SSL / certificat

```bash
# Vérifier que le domaine pointe bien vers votre IP
dig votre-domaine.com

# Relancer la demande de certificat
docker compose -f docker-compose.prod.yml run --rm certbot \
  certbot certonly --webroot \
  --webroot-path=/var/lib/letsencrypt \
  --email votre-email@example.com \
  --agree-tos --no-eff-email \
  -d votre-domaine.com

# Recharger Nginx
docker exec springforge-proxy nginx -s reload
```

### La base de données est corrompue / reset nécessaire

```bash
# ATTENTION : ceci SUPPRIME toutes les données

# En local
docker compose down -v
docker compose up -d

# En production (faire un backup avant !)
./deploy.sh backup
docker compose -f docker-compose.prod.yml down -v
./deploy.sh init
```

### Manque d'espace disque

```bash
# Voir l'espace utilisé par Docker
docker system df

# Nettoyer les images/conteneurs inutilisés
docker system prune -a

# Nettoyer les volumes inutilisés (ATTENTION aux données)
docker volume prune
```

### Voir les métriques de performance (local uniquement)

1. Ouvrez Grafana : http://localhost:3000 (admin/admin)
2. Allez dans Dashboards → Importer
3. Importez le fichier `infra/monitoring/grafana-dashboard.json`
4. Sélectionnez la source de données Prometheus

---

## Résumé rapide

### Local (1 commande)

```bash
git clone https://github.com/landry1996/SpringInitializer-plus-plus.git
cd SpringInitializer-plus-plus
docker compose up -d
# → http://localhost:4200
```

### VPS (4 commandes)

```bash
git clone https://github.com/landry1996/SpringInitializer-plus-plus.git /opt/springforge
cd /opt/springforge
cp .env.example .env && nano .env   # configurer les variables
chmod +x deploy.sh && ./deploy.sh init
# → http://votre-ip
```
