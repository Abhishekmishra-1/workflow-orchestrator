# PowerShell script to start Redis for local development
# This script starts a Redis container for token revocation

Write-Host "Starting Redis container for auth-service..." -ForegroundColor Green

# Check if Redis container already exists
$existingContainer = docker ps -a --format '{{.Names}}' | Select-String -Pattern "^auth-redis$"
if ($existingContainer) {
    Write-Host "Redis container 'auth-redis' already exists. Removing it..." -ForegroundColor Yellow
    docker rm -f auth-redis
}

# Start Redis container
docker run -d --name auth-redis -p 6379:6379 redis:7

if ($LASTEXITCODE -eq 0) {
    Write-Host "Redis container started successfully!" -ForegroundColor Green
    Write-Host "Redis is running on localhost:6379" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "To stop Redis: docker stop auth-redis" -ForegroundColor Yellow
    Write-Host "To remove Redis: docker rm -f auth-redis" -ForegroundColor Yellow
} else {
    Write-Host "Failed to start Redis container. Make sure Docker is running." -ForegroundColor Red
    exit 1
}

