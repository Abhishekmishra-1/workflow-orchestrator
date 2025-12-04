#!/bin/bash
# Script to start Redis for local development
# This script starts a Redis container for token revocation

echo "Starting Redis container for auth-service..."

# Check if Redis container already exists
if docker ps -a --format '{{.Names}}' | grep -q "^auth-redis$"; then
    echo "Redis container 'auth-redis' already exists. Removing it..."
    docker rm -f auth-redis
fi

# Start Redis container
docker run -d --name auth-redis -p 6379:6379 redis:7

echo "Redis container started successfully!"
echo "Redis is running on localhost:6379"
echo ""
echo "To stop Redis: docker stop auth-redis"
echo "To remove Redis: docker rm -f auth-redis"

