#!/bin/bash
set -e

host="$1"
port="$2"
shift 2
cmd="$@"

echo "Waiting for $host:$port..."

for i in $(seq 1 60); do
  if curl -s "http://$host:$port/actuator/health" | grep -q "UP"; then
    echo "$host:$port is available"
    exec $cmd
    exit 0
  fi
  echo "Attempt $i: $host:$port not available yet..."
  sleep 2
done

echo "Timeout waiting for $host:$port"
exit 1
