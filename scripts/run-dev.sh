#!/bin/bash
# Load .env file and run auth-service with dev profile

set -a
if [ -f .env ]; then
  . ./.env
fi
set +a

cd services/auth-service || exit 1
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

