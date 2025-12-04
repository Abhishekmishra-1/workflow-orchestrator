# Setup Summary: Dev Configuration and User Service

## Files Created/Modified

### Root Level Files
- `.env.example` - Environment variable template (DO NOT commit actual `.env` file)
- `.gitignore` - Updated with exclusions for secrets, env files, and build artifacts
- `scripts/run-dev.sh` - Linux/macOS script to run auth-service with dev profile
- `scripts/run-dev.ps1` - PowerShell script to run auth-service with dev profile

### Auth Service Configuration
- `services/auth-service/src/main/resources/application.yml` - Base config (passwords removed, use env vars)
- `services/auth-service/src/main/resources/application-dev.yml` - Dev profile (uses env vars with fallbacks)
- `services/auth-service/src/main/resources/application-prod.yml` - Prod profile (already existed, uses env vars only)
- `services/auth-service/README.md` - Updated with "Run locally (dev)" section

### User Service (New Microservice)
- `services/user-service/pom.xml` - Maven configuration
- `services/user-service/src/main/java/com/workflow/userservice/UserServiceApplication.java` - Main application class
- `services/user-service/src/main/java/com/workflow/userservice/entity/Tenant.java` - Tenant entity
- `services/user-service/src/main/java/com/workflow/userservice/entity/User.java` - User entity
- `services/user-service/src/main/java/com/workflow/userservice/entity/Role.java` - Role entity
- `services/user-service/src/main/java/com/workflow/userservice/repository/UserRepository.java` - User repository
- `services/user-service/src/main/java/com/workflow/userservice/service/UserService.java` - User service
- `services/user-service/src/main/java/com/workflow/userservice/controller/UserController.java` - User controller
- `services/user-service/src/main/resources/application.yml` - Base configuration
- `services/user-service/src/main/resources/application-dev.yml` - Dev profile configuration
- `services/user-service/src/main/resources/db/migration/V1__init_user.sql` - Flyway migration
- `services/user-service/README.md` - User service documentation

## Git Commands to Run

If this is not yet a git repository, initialize it first:

```bash
git init
git add .
git commit -m "Initial commit: Setup dev config and user-service"
```

Then create the branch and commit:

```bash
git checkout -b setup/dev-config-and-user-service

# Commit .env.example and .gitignore
git add .env.example .gitignore
git commit -m "Add .env.example and update .gitignore"

# Commit README updates
git add services/auth-service/README.md
git commit -m "Add README dev run instructions"

# Commit run scripts
git add scripts/
git commit -m "Add scripts to run dev"

# Commit config file updates (removed hardcoded passwords)
git add services/auth-service/src/main/resources/application.yml services/auth-service/src/main/resources/application-dev.yml
git commit -m "Remove hardcoded passwords from config files, use env vars"

# Commit user-service
git add services/user-service/
git commit -m "Scaffold user-service"

# Push to origin (if remote exists)
git push -u origin setup/dev-config-and-user-service
```

## Running Auth Service Locally (Dev)

### Step 1: Create .env file
```bash
cp .env.example .env
# Edit .env and set your DB_PASSWORD and JWT_SECRET
```

### Step 2: Run the service

**Option A: Using the run script (Linux/macOS)**
```bash
chmod +x scripts/run-dev.sh
./scripts/run-dev.sh
```

**Option B: Using the run script (Windows PowerShell)**
```powershell
.\scripts\run-dev.ps1
```

**Option C: Manual (Linux/macOS)**
```bash
set -a
. ./.env
set +a
cd services/auth-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Option C: Manual (Windows PowerShell)**
```powershell
Get-Content .\.env | ForEach-Object {
  $kv = $_ -split '=', 2
  if ($kv.Length -eq 2) { $envName = $kv[0].Trim(); $envVal = $kv[1].Trim(); $env:$envName = $envVal }
}
cd services\auth-service
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

## Testing Auth Service APIs

### Register a user
```bash
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "testpass123",
    "email": "test@example.com"
  }'
```

### Login
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "testpass123"
  }'
```

Save the token from the response, then:

### Ping (test authenticated endpoint)
```bash
curl -X GET http://localhost:8081/auth/ping \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Running User Service Locally (Dev)

```bash
# Load env vars (same .env file)
set -a
. ./.env
set +a

cd services/user-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The user service runs on port 8082.

## Important Notes

1. **Never commit `.env` file** - It contains real secrets
2. **The `.env.example` file contains a real password** - This should be changed to a placeholder before committing if you haven't already
3. **Config files now use environment variables** - No hardcoded passwords in committed files
4. **User service is standalone** - No authentication or cross-service calls yet

## Next Steps

1. Initialize git repository if not already done
2. Review `.env.example` and replace real password with placeholder if needed
3. Create your local `.env` file from `.env.example`
4. Test both services locally
5. Commit and push the branch

