#!/bin/bash

echo "🚀 Iniciando aplicação Desafio Santander com Docker Compose"

export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

check_service_health() {
    local service_name=$1
    local max_attempts=5
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        local container_id=$(docker compose ps -q $service_name 2>/dev/null)
        if [ -n "$container_id" ]; then
            local health_status=$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}healthy{{end}}' $container_id 2>/dev/null)
            if [ "$health_status" = "healthy" ]; then
                return 0
            fi
        fi

        [ $attempt -le 3 ] && sleep 1 || sleep 2
        attempt=$((attempt + 1))
    done
    return 1
}

smart_cleanup() {
    local running_containers=$(docker compose ps -q 2>/dev/null)
    [ -n "$running_containers" ] && docker compose stop >/dev/null 2>&1
    docker compose rm -f -v >/dev/null 2>&1
}

smart_build() {
    local app_image_exists=$(docker images -q desafio_santander-app 2>/dev/null)
    if [ -z "$app_image_exists" ]; then
        docker compose build --parallel >/dev/null 2>&1
    else
        docker compose build >/dev/null 2>&1
    fi
}

wait_postgres() {
    check_service_health "postgres" && echo "ready" > /tmp/pg_status || echo "failed" > /tmp/pg_status
}

wait_redis() {
    check_service_health "redis" && echo "ready" > /tmp/redis_status || echo "failed" > /tmp/redis_status
}

echo "🔍 Verificando ambiente..."
smart_cleanup

echo "🔨 Preparando build..."
smart_build

echo "🐳 Iniciando infraestrutura..."
docker compose up -d postgres redis >/dev/null 2>&1

rm -f /tmp/pg_status /tmp/redis_status

wait_postgres &
wait_redis &
wait

if [ "$(cat /tmp/pg_status 2>/dev/null)" != "ready" ]; then
    echo "❌ PostgreSQL falhou"
    docker compose logs postgres --tail=10
    exit 1
fi

if [ "$(cat /tmp/redis_status 2>/dev/null)" != "ready" ]; then
    echo "❌ Redis falhou"
    docker compose logs redis --tail=10
    exit 1
fi

rm -f /tmp/pg_status /tmp/redis_status

echo "✅ Infraestrutura pronta!"
echo "🚀 Iniciando aplicação..."
docker compose up -d app >/dev/null 2>&1

app_ready=false
for i in {1..25}; do
    if curl -s -f http://localhost:8080/actuator/health >/dev/null 2>&1 || curl -s -f http://localhost:8080 >/dev/null 2>&1; then
        app_ready=true
        break
    fi
    sleep 1.5
done

echo "📊 Status:"
docker compose ps --format "table {{.Service}}\t{{.Status}}"

(docker compose exec -T postgres pg_isready -U postgres -d agencia_db >/dev/null 2>&1 && echo "📊 PostgreSQL: ✅" || echo "📊 PostgreSQL: ❌") &
(docker compose exec -T redis redis-cli ping >/dev/null 2>&1 && echo "🔴 Redis: ✅" || echo "🔴 Redis: ❌") &
wait

if [ "$app_ready" = true ]; then
    echo "🎉 APLICAÇÃO PRONTA!"
    echo "🌐 http://localhost:8080"
else
    echo "⚠️ Aplicação ainda inicializando..."
    docker compose logs app --tail=5
fi

