#!/bin/bash

echo "Waiting for services to be ready..."

# Проверяем stats service
echo "Checking stats service (port 9091)..."
for i in {1..30}; do
  if curl -s -f http://localhost:9091/actuator/health 2>/dev/null | grep -q "UP"; then
    echo "✓ Stats service is ready"
    break
  fi
  if [ $i -eq 30 ]; then
    echo "✗ Stats service failed to start"
    docker-compose logs ewm-stats-service
    exit 1
  fi
  echo "Waiting for stats service... ($i/30)"
  sleep 5
done

# Проверяем main service
echo "Checking main service (port 8081)..."
for i in {1..60}; do
  if curl -s -f http://localhost:8081/actuator/health 2>/dev/null | grep -q "UP"; then
    echo "✓ Main service is ready"
    break
  fi
  if [ $i -eq 60 ]; then
    echo "✗ Main service failed to start"
    docker-compose logs ewm-main-service
    exit 1
  fi
  echo "Waiting for main service... ($i/60)"
  sleep 5
done

echo "✅ All services are ready!"
exit 0
