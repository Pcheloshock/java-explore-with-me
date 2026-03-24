#!/bin/bash
# wait-for-it.sh - wait for service to be ready

set -e

host="$1"
port="$2"
shift 2
cmd="$@"

echo "Waiting for $host:$port..."

for i in $(seq 1 60); do
  # Try to connect using nc (netcat) or curl
  if command -v nc &> /dev/null; then
    if nc -z "$host" "$port" 2>/dev/null; then
      echo "$host:$port is available"
      exec $cmd
      exit 0
    fi
  elif command -v curl &> /dev/null; then
    if curl -s "http://$host:$port/actuator/health" | grep -q "UP"; then
      echo "$host:$port is available"
      exec $cmd
      exit 0
    fi
  fi
  echo "Attempt $i: $host:$port not available yet..."
  sleep 1
done

echo "Timeout waiting for $host:$port"
exit 1
