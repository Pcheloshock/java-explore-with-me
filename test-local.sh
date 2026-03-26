#!/bin/bash

echo "🚀 Starting services..."
docker-compose up -d

echo "⏳ Waiting for services to be ready..."
for i in {1..30}; do
  if curl -s http://localhost:8081/actuator/health 2>/dev/null | grep -q "UP"; then
    echo "✅ Main service is ready!"
    break
  fi
  echo "⏳ Waiting for main service... ($i/30)"
  sleep 5
done

echo ""
echo "📊 Service status:"
docker-compose ps

echo ""
echo "🔍 Testing API endpoints:"
echo "Categories:"
curl -s http://localhost:8081/categories | jq '.'

echo ""
echo "Stats:"
curl -s "http://localhost:9091/stats?start=2024-01-01%2000:00:00&end=2026-12-31%2023:59:59"

echo ""
echo "✅ Test completed!"
