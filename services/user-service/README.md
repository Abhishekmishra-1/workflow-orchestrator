# User Service

Spring Boot microservice for user management with multi-tenant support.

## Configuration

This service uses Spring profiles:
- **dev**: Development profile for local development
- **prod**: Production profile (requires environment variables)

## Running the Application

### Development Profile

1. Copy `.env.example` to `.env` at the repository root and set your database credentials.

2. Load environment variables and run:

   **Linux/macOS:**
   ```bash
   set -a
   . ./.env
   set +a
   cd services/user-service
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   **Or use the run script:**
   ```bash
   chmod +x ../../scripts/run-user-service.sh
   ../../scripts/run-user-service.sh
   ```

   **Windows PowerShell:**
   ```powershell
   Get-Content ..\..\..env | ForEach-Object {
     $kv = $_ -split '=', 2
     if ($kv.Length -eq 2) { $envName = $kv[0].Trim(); $envVal = $kv[1].Trim(); $env:$envName = $envVal }
   }
   .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   **Or use the run script:**
   ```powershell
   ..\..\scripts\run-user-service.ps1
   ```

3. The service will start on port 8082.

## Database Migrations

Flyway migrations are located in `src/main/resources/db/migration/`. Migrations run automatically on application startup when Flyway is enabled.

### Initial Migration (V1__init_user.sql)

Creates the following tables:
- `tenant` - Tenant/organization information
- `role` - Role definitions
- `users` - User information with password
- `user_role` - Many-to-many relationship between users and roles

## API Endpoints

### Create User
```bash
POST http://localhost:8082/users
Content-Type: application/json

{
  "username": "john.doe",
  "password": "securepassword123",
  "email": "john.doe@example.com",
  "tenantId": 1,
  "roleIds": [1, 2]
}
```

### Get User by ID
```bash
GET http://localhost:8082/users/{id}
```

### Get Users by Tenant
```bash
GET http://localhost:8082/users?tenant={tenantId}
```

### Get All Users
```bash
GET http://localhost:8082/users
```

### Update User
```bash
PUT http://localhost:8082/users/{id}
Content-Type: application/json

{
  "email": "newemail@example.com",
  "roleIds": [1, 3]
}
```

### Delete User
```bash
DELETE http://localhost:8082/users/{id}
```

## Example Requests

### Using curl

**Create User:**
```bash
curl -X POST http://localhost:8082/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jane.smith",
    "password": "password123",
    "email": "jane.smith@example.com",
    "tenantId": 1,
    "roleIds": [1]
  }'
```

**Get User:**
```bash
curl http://localhost:8082/users/1
```

**Get Users by Tenant:**
```bash
curl http://localhost:8082/users?tenant=1
```

**Update User:**
```bash
curl -X PUT http://localhost:8082/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "email": "updated@example.com",
    "roleIds": [1, 2]
  }'
```

**Delete User:**
```bash
curl -X DELETE http://localhost:8082/users/1
```

### Using Postman

1. **Create User:**
   - Method: POST
   - URL: `http://localhost:8082/users`
   - Headers: `Content-Type: application/json`
   - Body (raw JSON):
     ```json
     {
       "username": "jane.smith",
       "password": "password123",
       "email": "jane.smith@example.com",
       "tenantId": 1,
       "roleIds": [1]
     }
     ```

2. **Get User:**
   - Method: GET
   - URL: `http://localhost:8082/users/1`

3. **Get Users by Tenant:**
   - Method: GET
   - URL: `http://localhost:8082/users?tenant=1`

4. **Update User:**
   - Method: PUT
   - URL: `http://localhost:8082/users/1`
   - Headers: `Content-Type: application/json`
   - Body (raw JSON):
     ```json
     {
       "email": "updated@example.com",
       "roleIds": [1, 2]
     }
     ```

5. **Delete User:**
   - Method: DELETE
   - URL: `http://localhost:8082/users/1`

## Database Schema

- `tenant` - Tenant/organization information
- `role` - Role definitions
- `users` - User information (includes password field)
- `user_role` - Many-to-many relationship between users and roles

## Building

```bash
cd services/user-service
./mvnw clean package
```

The JAR file will be created in `target/user-service-0.0.1-SNAPSHOT.jar`

## Testing

```bash
cd services/user-service
./mvnw test
```
