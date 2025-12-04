# Auth Service

Spring Boot microservice for authentication and authorization using JWT tokens with RS256 (RSA) signing, refresh tokens, and Redis-based token revocation.

## Configuration Profiles

This service supports multiple Spring profiles for different environments:

- **default**: Base configuration with minimal settings
- **dev**: Development profile for local development
- **prod**: Production profile (requires environment variables)

## Running the Application

### Development Profile (Local)

The development profile uses environment variables with developer-friendly defaults.

#### Using Maven:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Using Java JAR:

```bash
# Build the application first
mvn clean package

# Run with dev profile
java -jar target/auth-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

#### Setting Environment Variables (Optional for Dev):

**Linux/macOS:**
```bash
export DB_URL=jdbc:mysql://localhost:3306/authdb
export DB_USERNAME=root
export DB_PASSWORD=your_local_password
export JWT_SECRET=your_dev_jwt_secret_key

mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Windows PowerShell:**
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/authdb"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_local_password"
$env:JWT_SECRET="your_dev_jwt_secret_key"

mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Windows CMD:**
```cmd
set DB_URL=jdbc:mysql://localhost:3306/authdb
set DB_USERNAME=root
set DB_PASSWORD=your_local_password
set JWT_SECRET=your_dev_jwt_secret_key

mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Production Profile

The production profile **requires** all sensitive values to be provided via environment variables. No defaults are provided for security.

#### Using Maven:

```bash
# Set all required environment variables first, then:
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

#### Using Java JAR:

```bash
# Build the application
mvn clean package

# Set environment variables, then run:
java -jar target/auth-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

#### Required Environment Variables for Production:

**Linux/macOS:**
```bash
export DB_URL=jdbc:mysql://prod-db-host:3306/authdb?useSSL=true
export DB_USERNAME=prod_db_user
export DB_PASSWORD=prod_db_password
export JWT_SECRET=strong_random_secret_key_min_256_bits
export JWT_EXPIRATION=86400000  # Optional, defaults to 24 hours
export SERVER_PORT=8081  # Optional, defaults to 8081
export DB_POOL_MAX=20  # Optional, defaults to 20
export DB_POOL_MIN_IDLE=5  # Optional, defaults to 5
export LOG_FILE_PATH=/var/log/auth-service/application.log  # Optional
```

**Windows PowerShell:**
```powershell
$env:DB_URL="jdbc:mysql://prod-db-host:3306/authdb?useSSL=true"
$env:DB_USERNAME="prod_db_user"
$env:DB_PASSWORD="prod_db_password"
$env:JWT_SECRET="strong_random_secret_key_min_256_bits"
$env:JWT_EXPIRATION="86400000"
$env:SERVER_PORT="8081"
```

**Windows CMD:**
```cmd
set DB_URL=jdbc:mysql://prod-db-host:3306/authdb?useSSL=true
set DB_USERNAME=prod_db_user
set DB_PASSWORD=prod_db_password
set JWT_SECRET=strong_random_secret_key_min_256_bits
set JWT_EXPIRATION=86400000
set SERVER_PORT=8081
```

## Docker Compose Example

Here's an example `docker-compose.yml` snippet for running the service with environment variables:

```yaml
version: '3.8'

services:
  auth-service:
    image: auth-service:latest
    container_name: auth-service
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_URL=${DB_URL}
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - JWT_EXPIRATION=${JWT_EXPIRATION:-86400000}
      - SERVER_PORT=8081
      - DB_POOL_MAX=${DB_POOL_MAX:-20}
      - DB_POOL_MIN_IDLE=${DB_POOL_MIN_IDLE:-5}
    depends_on:
      - mysql-db
    networks:
      - app-network

  mysql-db:
    image: mysql:8.0
    container_name: auth-mysql
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
      - MYSQL_DATABASE=${MYSQL_DATABASE:-authdb}
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - app-network

networks:
  app-network:
    driver: bridge

volumes:
  mysql-data:
```

**Note**: Create a `.env` file (not committed to git) with your actual values:
```
DB_URL=jdbc:mysql://mysql-db:3306/authdb?useSSL=false
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
MYSQL_ROOT_PASSWORD=your_mysql_password
MYSQL_DATABASE=authdb
```

## Security Features

- **RS256 (RSA) JWT Signing**: Uses RSA keys for token signing (more secure than HS256)
- **Refresh Tokens**: Long-lived refresh tokens stored securely in database (hashed)
- **Token Revocation**: Redis-based revocation for immediate token invalidation
- **Short-lived Access Tokens**: Access tokens expire in 5 minutes (configurable)
- **Token Rotation**: Refresh tokens are rotated on each refresh request

## Security Best Practices

1. **Never commit secrets** to version control
2. **Never commit private RSA keys** - use secrets manager in production
3. **Use secrets management** in production (AWS Secrets Manager, HashiCorp Vault, Azure Key Vault, etc.)
4. **Rotate keys regularly**, especially RSA private keys and JWT secrets
5. **Use strong RSA keys** (2048-bit minimum, 4096-bit recommended for production)
6. **Enable SSL/TLS** for database connections in production
7. **Use different keys** for different environments
8. **Monitor Redis** for revocation patterns and potential attacks
9. **Set appropriate token expiry times** based on your security requirements

## Database Migrations

This service uses Flyway for database migrations. Migration scripts are located in:
```
src/main/resources/db/migration/
```

Migrations run automatically on application startup when Flyway is enabled.

## Building

```bash
mvn clean package
```

The JAR file will be created in `target/auth-service-0.0.1-SNAPSHOT.jar`

## Testing

```bash
mvn test
```

## Prerequisites

### RSA Keys for JWT Signing

The service uses RS256 (RSA) for JWT signing. For local development, RSA keys are provided in `src/main/resources/keys/`.

**To generate new keys (if needed):**

```bash
# Generate private key
openssl genrsa -out src/main/resources/keys/private.pem 2048

# Generate public key from private key
openssl rsa -in src/main/resources/keys/private.pem -pubout -out src/main/resources/keys/public.pem
```

**⚠️ SECURITY WARNING:** 
- The private key in `src/main/resources/keys/private.pem` is for **LOCAL DEVELOPMENT ONLY**
- **DO NOT** commit private keys to version control in production
- In production, store keys in:
  - HashiCorp Vault
  - Kubernetes Secrets
  - AWS Secrets Manager / Azure Key Vault
  - Secure file mounts

### Redis for Token Revocation

The service uses Redis to track revoked tokens. Start Redis before running the service.

**Using Docker (recommended):**

```bash
# Linux/macOS
./scripts/run-redis-dev.sh

# Windows PowerShell
.\scripts\run-redis-dev.ps1

# Or manually:
docker run -d --name auth-redis -p 6379:6379 redis:7
```

## Run locally (dev)

1. **Start Redis:**
   ```bash
   # Linux/macOS
   ./scripts/run-redis-dev.sh
   
   # Windows PowerShell
   .\scripts\run-redis-dev.ps1
   ```

2. **Copy `.env.example` to `.env`** and edit `.env` with local credentials (DO NOT COMMIT `.env`):
   ```
   cp .env.example .env
   # edit .env and set DB_PASSWORD
   ```

3. **Load env and run** (Linux/macOS):
   ```
   set -a
   . ./.env
   set +a
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   **Windows PowerShell** (temporary session):
   ```
   Get-Content .\.env | ForEach-Object {
     $kv = $_ -split '=', 2
     if ($kv.Length -eq 2) { $envName = $kv[0].Trim(); $envVal = $kv[1].Trim(); $env:$envName = $envVal }
   }
   .\mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   **Or use Maven directly:**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

4. **Test APIs:**

   **Register a user:**
   ```bash
   POST http://localhost:8081/auth/register
   Content-Type: application/json
   
   {
     "username": "testuser",
     "password": "password123",
     "role": "USER"
   }
   ```

   **Login (get access + refresh tokens):**
   ```bash
   POST http://localhost:8081/auth/login
   Content-Type: application/json
   
   {
     "username": "testuser",
     "password": "password123"
   }
   ```
   
   **Response:**
   ```json
   {
     "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
     "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
     "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
     "accessTokenExpiresIn": 300000,
     "refreshTokenExpiresIn": 1209600000
   }
   ```

   **Call protected endpoint:**
   ```bash
   GET http://localhost:8081/auth/ping
   Authorization: Bearer <accessToken>
   ```

   **Refresh tokens:**
   ```bash
   POST http://localhost:8081/auth/refresh
   Content-Type: application/json
   
   {
     "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
   }
   ```
   
   **Response:**
   ```json
   {
     "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
     "refreshToken": "new-refresh-token-uuid",
     "accessTokenExpiresIn": 300000,
     "refreshTokenExpiresIn": 1209600000
   }
   ```

   **Revoke tokens:**
   ```bash
   POST http://localhost:8081/auth/revoke
   Content-Type: application/json
   
   {
     "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
     "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
   }
   ```

   **Verify revocation:** After revoking, try to use the access token again - it should be rejected.

## API Endpoints

- `POST /auth/register` - Register a new user (public)
- `POST /auth/login` - Login and get access + refresh tokens (public)
- `POST /auth/refresh` - Refresh access token using refresh token (public)
- `POST /auth/revoke` - Revoke access and/or refresh tokens (public)
- `GET /auth/ping` - Health check (requires authentication)

