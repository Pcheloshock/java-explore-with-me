#!/bin/bash
# wait-for-it.sh

set -e

host="$1"
port="$2"
shift 2
cmd="$@"

echo "Waiting for $host:$port..."

for i in $(seq 1 60); do
  if nc -z "$host" "$port" 2>/dev/null; then
    echo "$host:$port is available"
    exec $cmd
    exit 0
  fi
  echo "Attempt $i: $host:$port not available yet..."
  sleep 1
done

echo "Timeout waiting for $host:$port"
exit 1
