#!/bin/bash

echo "Waiting for services to be healthy..."

# Функция для проверки health
check_health() {
    local service=$1
    local port=$2
    local url="http://localhost:$port/actuator/health"
    
    if curl -s -f "$url" 2>/dev/null | grep -q '"status":"UP"'; then
        return 0
    fi
    return 1
}

# Ждем stats service
echo "Checking stats service (port 9091)..."
for i in {1..30}; do
    if check_health "stats" 9091; then
        echo "✓ Stats service is healthy"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "✗ Stats service failed to become healthy"
        docker-compose logs ewm-stats-service
        exit 1
    fi
    echo "Waiting for stats service... ($i/30)"
    sleep 5
done

# Ждем main service
echo "Checking main service (port 8081)..."
for i in {1..60}; do
    if check_health "main" 8081; then
        echo "✓ Main service is healthy"
        break
    fi
    if [ $i -eq 60 ]; then
        echo "✗ Main service failed to become healthy"
        docker-compose logs ewm-main-service
        exit 1
    fi
    echo "Waiting for main service... ($i/60)"
    sleep 5
done

echo "✅ All services are healthy!"
exit 0
