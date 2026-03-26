#!/bin/bash

echo "Waiting for services to start..."

# Проверка stats service
echo "Checking stats service..."
for i in {1..30}; do
  if curl -s http://localhost:9091/actuator/health | grep -q "UP"; then
    echo "Stats service is ready!"
    break
  fi
  echo "Attempt $i: Stats service not ready yet..."
  sleep 5
done

# Проверка main service
echo "Checking main service..."
for i in {1..30}; do
  if curl -s http://localhost:8081/actuator/health | grep -q "UP"; then
    echo "Main service is ready!"
    exit 0
  fi
  echo "Attempt $i: Main service not ready yet..."
  sleep 5
done

echo "Services failed to start"
exit 1
