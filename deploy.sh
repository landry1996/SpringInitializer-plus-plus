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
#   Cleanup:     ./deploy.sh cleanup

COMPOSE_FILE="docker-compose.prod.yml"
PROJECT_NAME="springforge"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

log() { echo -e "${GREEN}[SpringForge]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }
info() { echo -e "${CYAN}[INFO]${NC} $1"; }

check_requirements() {
    command -v docker >/dev/null 2>&1 || error "Docker is not installed"
    command -v docker compose >/dev/null 2>&1 || error "Docker Compose is not installed"
    log "Requirements OK"
}

check_system_resources() {
    local total_mem=$(free -m 2>/dev/null | awk '/^Mem:/{print $2}' || echo "0")
    if [ "$total_mem" -gt 0 ] && [ "$total_mem" -lt 3500 ]; then
        warn "System has only ${total_mem}MB RAM. Recommended: 4GB+ for advanced architecture generation."
        warn "Consider increasing GENERATION_POOL_CORE/MAX in .env to avoid OOM during parallel generation."
    fi
    local disk_free=$(df -BG . 2>/dev/null | awk 'NR==2{print $4}' | tr -d 'G' || echo "0")
    if [ "$disk_free" -gt 0 ] && [ "$disk_free" -lt 20 ]; then
        warn "Only ${disk_free}GB disk space remaining. Generated projects can be large (microservices with multiple DBs)."
    fi
}

init() {
    log "Initializing SpringForge deployment..."
    check_system_resources

    # Check .env exists
    if [ ! -f .env ]; then
        if [ -f .env.example ]; then
            cp .env.example .env
            warn ".env created from .env.example - EDIT IT before continuing!"
        else
            warn "No .env.example found. Creating minimal .env..."
            cat > .env <<'ENVEOF'
DB_PASSWORD=changeme_strong_password
JWT_SECRET=changeme_generate_with_openssl_rand_base64_64
CORS_ORIGINS=http://localhost
GENERATION_POOL_CORE=5
GENERATION_POOL_MAX=10
STORAGE_TYPE=filesystem
AI_PROVIDER=claude
NOTIFICATION_EMAIL_ENABLED=false
ENVEOF
        fi
        warn "Run: nano .env"
        exit 1
    fi

    # Validate critical env vars
    source .env 2>/dev/null || true
    if [ -z "$DB_PASSWORD" ] || [ "$DB_PASSWORD" = "changeme_strong_password" ]; then
        warn "DB_PASSWORD is not set or still default. Please update .env"
    fi
    if [ -z "$JWT_SECRET" ] || [ ${#JWT_SECRET} -lt 32 ]; then
        warn "JWT_SECRET is too short or not set. Generate with: openssl rand -base64 64"
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
    sleep 15
    for i in $(seq 1 30); do
        if docker exec springforge-backend wget -qO- http://localhost:8080/actuator/health 2>/dev/null | grep -q "UP"; then
            log "Backend is healthy!"
            break
        fi
        if [ $i -eq 30 ]; then
            warn "Backend not healthy after 30 attempts. Check logs: ./deploy.sh logs backend"
        fi
        sleep 3
    done

    log "Deployment complete!"
    echo ""
    echo "  Frontend:  http://$(hostname -I | awk '{print $1}')"
    echo "  API:       http://$(hostname -I | awk '{print $1}')/api/v1"
    echo "  Health:    http://$(hostname -I | awk '{print $1}')/actuator/health"
    echo "  Swagger:   http://$(hostname -I | awk '{print $1}')/swagger-ui.html"
    echo ""
    info "Supported architectures: Microservices, Hexagonal, DDD, CQRS, Event-Driven, Modulith, Layered, Monolithic"
    info "Microservices generation supports: PostgreSQL, MySQL, MongoDB, Redis, Cassandra, Neo4j per service"
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

    # Wait for backend to be healthy after update
    sleep 10
    for i in $(seq 1 15); do
        if docker exec springforge-backend wget -qO- http://localhost:8080/actuator/health 2>/dev/null | grep -q "UP"; then
            log "Backend is healthy after update!"
            break
        fi
        sleep 3
    done

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
    sed -i "s/\${DOMAIN:-localhost}/$domain/g" infra/nginx/conf.d/default.conf

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
    local health=$(docker exec springforge-backend wget -qO- http://localhost:8080/actuator/health 2>/dev/null || echo '{"status":"DOWN"}')
    echo "  $health"
    echo ""
    log "Resource usage:"
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" $(docker compose -f $COMPOSE_FILE -p $PROJECT_NAME ps -q) 2>/dev/null || true
    echo ""
    log "Disk usage:"
    docker system df 2>/dev/null | head -5
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

    # Also backup generated projects volume info
    log "Backing up generation metadata..."
    docker exec springforge-db psql -U springforge -c "SELECT count(*) as total_generations FROM generations;" > "$backup_dir/generation_stats.txt" 2>/dev/null || true

    local backup_size=$(du -sh "$backup_dir" 2>/dev/null | awk '{print $1}')
    log "Backup saved to $backup_dir ($backup_size)"

    # Keep only last 7 backups
    ls -dt ./backups/*/ 2>/dev/null | tail -n +8 | xargs rm -rf 2>/dev/null || true
    log "Old backups cleaned (keeping last 7)"
}

cleanup() {
    log "Cleaning up old generated projects and Docker resources..."

    # Clean generated projects older than 30 days
    docker exec springforge-backend find /app/generated -maxdepth 1 -mtime +30 -type d -exec rm -rf {} \; 2>/dev/null || true
    log "Cleaned generated projects older than 30 days"

    # Clean Docker resources
    docker image prune -f 2>/dev/null || true
    log "Cleaned unused Docker images"

    local freed=$(docker system df 2>/dev/null | grep "Images" | awk '{print $4}')
    info "Reclaimable space: $freed"
}

stop() {
    log "Stopping SpringForge..."
    docker compose -f $COMPOSE_FILE -p $PROJECT_NAME down
    log "Stopped."
}

# Main
check_requirements

case "${1:-help}" in
    init)    init ;;
    update)  update ;;
    ssl)     setup_ssl "$2" "$3" ;;
    status)  status ;;
    logs)    show_logs "$2" ;;
    backup)  backup ;;
    cleanup) cleanup ;;
    stop)    stop ;;
    *)
        echo "SpringForge Deployment Script"
        echo ""
        echo "Usage: ./deploy.sh <command>"
        echo ""
        echo "Commands:"
        echo "  init                    First-time deployment (build + start)"
        echo "  update                  Pull latest code and redeploy"
        echo "  ssl <domain> <email>    Setup Let's Encrypt SSL"
        echo "  status                  Show service status, health, and resource usage"
        echo "  logs [service]          Show logs (optional: backend, frontend, postgres, redis)"
        echo "  backup                  Backup PostgreSQL database"
        echo "  cleanup                 Remove old generated projects and unused Docker images"
        echo "  stop                    Stop all services"
        echo ""
        echo "Supported architecture generation:"
        echo "  Microservices (multi-DB: PostgreSQL, MySQL, MongoDB, Redis, Cassandra, Neo4j)"
        echo "  Hexagonal, DDD, CQRS, Event-Driven, Modulith, Layered, Monolithic"
        ;;
esac
