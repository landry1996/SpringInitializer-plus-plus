#!/bin/bash
set -e

# ============================================
# SpringForge - VPS Deployment Script
# ============================================
# Usage:
#   First time:  ./deploy.sh init
#   Update:      ./deploy.sh update
#   SSL setup:   ./deploy.sh ssl your-domain.com your-email@example.com
#   Status:      ./deploy.sh status
#   Logs:        ./deploy.sh logs [service]
#   Backup:      ./deploy.sh backup

COMPOSE_FILE="docker-compose.prod.yml"
PROJECT_NAME="springforge"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log() { echo -e "${GREEN}[SpringForge]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

check_requirements() {
    command -v docker >/dev/null 2>&1 || error "Docker is not installed"
    command -v docker compose >/dev/null 2>&1 || error "Docker Compose is not installed"
    log "Requirements OK"
}

init() {
    log "Initializing SpringForge deployment..."

    # Check .env exists
    if [ ! -f .env ]; then
        cp .env.example .env
        warn ".env created from .env.example - EDIT IT before continuing!"
        warn "Run: nano .env"
        exit 1
    fi

    # Use no-SSL config for first deployment
    if [ ! -f infra/nginx/conf.d/default.conf ]; then
        cp infra/nginx/conf.d/default-nossl.conf.example infra/nginx/conf.d/default.conf
        log "Using HTTP-only nginx config (run './deploy.sh ssl' after to enable HTTPS)"
    fi

    # Build and start
    log "Building images..."
    docker compose -f $COMPOSE_FILE -p $PROJECT_NAME build

    log "Starting services..."
    docker compose -f $COMPOSE_FILE -p $PROJECT_NAME up -d

    log "Waiting for backend health check..."
    sleep 10
    for i in $(seq 1 30); do
        if docker exec springforge-backend wget -qO- http://localhost:8080/actuator/health 2>/dev/null | grep -q "UP"; then
            log "Backend is healthy!"
            break
        fi
        if [ $i -eq 30 ]; then
            warn "Backend not healthy after 30 attempts. Check logs: ./deploy.sh logs backend"
        fi
        sleep 2
    done

    log "Deployment complete!"
    echo ""
    echo "  Frontend:  http://$(hostname -I | awk '{print $1}')"
    echo "  API:       http://$(hostname -I | awk '{print $1}')/api/v1"
    echo "  Health:    http://$(hostname -I | awk '{print $1}')/actuator/health"
    echo ""
}

update() {
    log "Updating SpringForge..."

    git pull origin main

    log "Rebuilding images..."
    docker compose -f $COMPOSE_FILE -p $PROJECT_NAME build

    log "Restarting services (zero downtime)..."
    docker compose -f $COMPOSE_FILE -p $PROJECT_NAME up -d --force-recreate --no-deps backend
    docker compose -f $COMPOSE_FILE -p $PROJECT_NAME up -d --force-recreate --no-deps frontend

    log "Update complete!"
}

setup_ssl() {
    local domain=$1
    local email=$2

    if [ -z "$domain" ] || [ -z "$email" ]; then
        error "Usage: ./deploy.sh ssl <domain> <email>"
    fi

    log "Setting up SSL for $domain..."

    # Get certificate
    docker compose -f $COMPOSE_FILE -p $PROJECT_NAME run --rm certbot \
        certbot certonly --webroot \
        --webroot-path=/var/lib/letsencrypt \
        --email $email \
        --agree-tos \
        --no-eff-email \
        -d $domain

    # Switch to SSL config
    sed "s/\${DOMAIN:-localhost}/$domain/g" infra/nginx/conf.d/default.conf > /tmp/nginx-ssl.conf
    cp infra/nginx/conf.d/default.conf infra/nginx/conf.d/default.conf.bak
    # Use the SSL template
    sed "s/\${DOMAIN:-localhost}/$domain/g" infra/nginx/conf.d/default.conf > infra/nginx/conf.d/default.conf

    # Reload nginx
    docker exec springforge-proxy nginx -s reload

    log "SSL configured for $domain!"
    echo "  HTTPS: https://$domain"
}

status() {
    log "Service status:"
    docker compose -f $COMPOSE_FILE -p $PROJECT_NAME ps
    echo ""
    log "Health check:"
    docker exec springforge-backend wget -qO- http://localhost:8080/actuator/health 2>/dev/null || echo "Backend not responding"
}

show_logs() {
    local service=${1:-""}
    if [ -z "$service" ]; then
        docker compose -f $COMPOSE_FILE -p $PROJECT_NAME logs -f --tail=100
    else
        docker compose -f $COMPOSE_FILE -p $PROJECT_NAME logs -f --tail=100 $service
    fi
}

backup() {
    local backup_dir="./backups/$(date +%Y%m%d_%H%M%S)"
    mkdir -p $backup_dir

    log "Backing up PostgreSQL..."
    docker exec springforge-db pg_dump -U springforge springforge > "$backup_dir/springforge_db.sql"

    log "Backup saved to $backup_dir"

    # Keep only last 7 backups
    ls -dt ./backups/*/ 2>/dev/null | tail -n +8 | xargs rm -rf 2>/dev/null || true
    log "Old backups cleaned (keeping last 7)"
}

stop() {
    log "Stopping SpringForge..."
    docker compose -f $COMPOSE_FILE -p $PROJECT_NAME down
    log "Stopped."
}

# Main
check_requirements

case "${1:-help}" in
    init)   init ;;
    update) update ;;
    ssl)    setup_ssl "$2" "$3" ;;
    status) status ;;
    logs)   show_logs "$2" ;;
    backup) backup ;;
    stop)   stop ;;
    *)
        echo "SpringForge Deployment Script"
        echo ""
        echo "Usage: ./deploy.sh <command>"
        echo ""
        echo "Commands:"
        echo "  init              First-time deployment (build + start)"
        echo "  update            Pull latest code and redeploy"
        echo "  ssl <domain> <email>  Setup Let's Encrypt SSL"
        echo "  status            Show service status and health"
        echo "  logs [service]    Show logs (optional: backend, frontend, postgres, redis)"
        echo "  backup            Backup PostgreSQL database"
        echo "  stop              Stop all services"
        ;;
esac
