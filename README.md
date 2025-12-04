# Workflow Orchestrator

**Enterprise-Grade Workflow Orchestration Engine**

A secure, scalable workflow orchestration platform inspired by OpenText, Camunda, and Temporal. Built with modern microservices architecture and enterprise security best practices.

---

## 📋 Summary

The Workflow Orchestrator is a comprehensive platform designed to manage complex business workflows at enterprise scale. It provides robust authentication, user management, and will evolve into a full-featured workflow engine capable of handling distributed task execution, event-driven processes, and multi-tenant operations.

---

## 🚀 Current Progress

**Status:** Foundation Phase Complete ✅

- **auth-service**: Fully implemented with enterprise-grade security
- **user-service**: Complete with multi-tenant support
- **Next Phase**: Event-driven architecture with Kafka integration

---

## ✨ Features Implemented

### Authentication & Security
- ✅ **RS256 JWT Signing**: RSA-based token signing for enhanced security
- ✅ **Refresh Token Rotation**: Automatic token rotation on refresh requests
- ✅ **Redis Token Revocation**: Immediate token invalidation via Redis
- ✅ **Rate Limiting**: Protection against brute-force attacks
- ✅ **Session & Device Tracking**: Multi-device session management
- ✅ **JWKS Endpoint**: Public key endpoint for token verification
- ✅ **Actuator Health Checks**: Spring Boot Actuator integration

### User Management
- ✅ **Multi-Tenant Support**: Tenant-based user isolation
- ✅ **Role-Based Access Control**: Flexible role assignment system
- ✅ **User CRUD Operations**: Complete user lifecycle management

### Infrastructure
- ✅ **MySQL Database**: Persistent data storage with Flyway migrations
- ✅ **Redis Integration**: Token revocation and caching
- ✅ **Database Migrations**: Version-controlled schema management
- ✅ **Profile-Based Configuration**: Dev and production profiles

---

## 📁 Repository Structure

```
workflow-orchestrator/
├── services/
│   ├── auth-service/          # Authentication & authorization service
│   │   ├── src/
│   │   │   └── main/
│   │   │       ├── java/       # Java source code
│   │   │       └── resources/  # Configuration & migrations
│   │   └── pom.xml
│   │
│   ├── user-service/           # User management service
│   │   ├── src/
│   │   │   └── main/
│   │   │       ├── java/       # Java source code
│   │   │       └── resources/  # Configuration & migrations
│   │   └── pom.xml
│   │
│   └── [future services]       # Task service, Workflow engine, etc.
│
├── scripts/                    # Development scripts
│   ├── run-dev.ps1            # PowerShell dev script
│   ├── run-dev.sh             # Bash dev script
│   └── run-user-service.*     # User service scripts
│
└── README.md                   # This file
```

---

## 🛠️ Tech Stack

### Core Technologies
- **Java 21**: Modern Java with latest language features
- **Spring Boot 3.x**: Enterprise application framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database abstraction layer

### Data & Caching
- **MySQL 8.0**: Primary relational database
- **Redis**: Token revocation and caching layer
- **Flyway**: Database migration management

### Infrastructure (Planned)
- **Apache Kafka**: Event streaming and message broker
- **Docker**: Containerization
- **Kubernetes**: Container orchestration (future)

### Development Tools
- **Maven**: Build and dependency management
- **Spring Boot Actuator**: Health checks and monitoring

---

## 🚀 Setup Instructions

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Git

### 1. Clone Repository

```bash
git clone <repository-url>
cd workflow-orchestrator
```

### 2. Environment Variables

Create a `.env` file at the repository root (do not commit this file):

```bash
# Database Configuration
DB_URL=jdbc:mysql://localhost:3306/authdb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=your_password_here

# JWT Configuration
JWT_SECRET=your_strong_jwt_secret_key_min_256_bits

# Optional Configuration
JWT_EXPIRATION=86400000
SERVER_PORT=8081
DB_POOL_MAX=20
DB_POOL_MIN_IDLE=5
```

### 3. Start Infrastructure Services

**MySQL:**
```bash
# Using Docker (recommended)
docker run -d \
  --name mysql-auth \
  -e MYSQL_ROOT_PASSWORD=your_password_here \
  -e MYSQL_DATABASE=authdb \
  -p 3306:3306 \
  mysql:8.0

# Or use your local MySQL instance
```

**Redis:**
```bash
# Using Docker (recommended)
docker run -d \
  --name redis-auth \
  -p 6379:6379 \
  redis:7-alpine

# Or use your local Redis instance
```

### 4. Running Services

#### Auth Service

**Linux/macOS:**
```bash
# Load environment variables
set -a
. ./.env
set +a

# Run service
cd services/auth-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Windows PowerShell:**
```powershell
# Load environment variables
Get-Content ..\.env | ForEach-Object {
  $kv = $_ -split '=', 2
  if ($kv.Length -eq 2) { 
    $envName = $kv[0].Trim()
    $envVal = $kv[1].Trim()
    Set-Item -Path "env:$envName" -Value $envVal
  }
}

# Run service
cd services\auth-service
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

**Or use the convenience script:**
```bash
# Linux/macOS
./scripts/run-dev.sh

# Windows PowerShell
.\scripts\run-dev.ps1
```

The auth service will start on **port 8081**.

#### User Service

**Linux/macOS:**
```bash
# Load environment variables
set -a
. ./.env
set +a

# Run service
cd services/user-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Windows PowerShell:**
```powershell
# Load environment variables
Get-Content ..\..\.env | ForEach-Object {
  $kv = $_ -split '=', 2
  if ($kv.Length -eq 2) { 
    $envName = $kv[0].Trim()
    $envVal = $kv[1].Trim()
    Set-Item -Path "env:$envName" -Value $envVal
  }
}

# Run service
cd services\user-service
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

**Or use the convenience script:**
```bash
# Linux/macOS
./scripts/run-user-service.sh

# Windows PowerShell
.\scripts\run-user-service.ps1
```

The user service will start on **port 8082**.

---

## 📡 API Overview

### Auth Service (Port 8081)

#### Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/auth/register` | Register a new user |
| `POST` | `/auth/login` | Authenticate and receive tokens |
| `POST` | `/auth/refresh` | Refresh access token using refresh token |
| `POST` | `/auth/revoke` | Revoke refresh token |
| `GET` | `/auth/sessions` | Get all active sessions for current user |
| `DELETE` | `/auth/sessions/{sessionId}` | Revoke a specific session |
| `GET` | `/auth/jwks` | Get JSON Web Key Set (public keys) |
| `GET` | `/auth/ping` | Health check endpoint |

#### Example Requests

**Register:**
```bash
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "securepass123",
    "email": "test@example.com"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "securepass123"
  }'
```

**Refresh Token:**
```bash
curl -X POST http://localhost:8081/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "your_refresh_token_here"
  }'
```

**Get Sessions:**
```bash
curl -X GET http://localhost:8081/auth/sessions \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**JWKS Endpoint:**
```bash
curl http://localhost:8081/auth/jwks
```

### User Service (Port 8082)

#### User Management Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/users` | Create a new user |
| `GET` | `/users/{id}` | Get user by ID |
| `GET` | `/users?tenant={tenantId}` | Get users by tenant |
| `GET` | `/users` | Get all users |
| `PUT` | `/users/{id}` | Update user |
| `DELETE` | `/users/{id}` | Delete user |

For detailed API documentation, see:
- [Auth Service README](services/auth-service/README.md)
- [User Service README](services/user-service/README.md)

---

## 🗺️ Roadmap

### ✅ Week 1-2: Authentication Foundation & Hardening (COMPLETE)
- [x] RS256 JWT implementation
- [x] Refresh token rotation
- [x] Redis-based token revocation
- [x] Rate limiting
- [x] Session and device tracking
- [x] JWKS endpoint
- [x] Actuator health checks
- [x] User service with multi-tenant support

### 📅 Week 3-4: Event-Driven Architecture
- [ ] Apache Kafka integration
- [ ] Event producer/consumer setup
- [ ] Event schema registry
- [ ] Service-to-service communication via events

### 📅 Week 5-6: Transactional Outbox Pattern
- [ ] Outbox table implementation
- [ ] Event publishing from database transactions
- [ ] Reliable event delivery
- [ ] Dead letter queue handling

### 📅 Week 7-8: Task Service
- [ ] Task definition and execution
- [ ] Task scheduling
- [ ] Task status tracking
- [ ] Task retry mechanisms

### 📅 Week 9-10: Workflow Engine Core
- [ ] Workflow definition DSL
- [ ] Workflow execution engine
- [ ] State machine implementation
- [ ] Workflow versioning

### 📅 Week 11-12: Observability & Monitoring
- [ ] Distributed tracing (OpenTelemetry)
- [ ] Centralized logging
- [ ] Metrics collection and dashboards
- [ ] Alerting system

### 📅 Week 13-14: API Gateway & Multi-Tenancy
- [ ] API Gateway implementation
- [ ] Request routing and load balancing
- [ ] Enhanced multi-tenant isolation
- [ ] Tenant-specific configurations

### 📅 Week 15: Kubernetes & Scaling
- [ ] Kubernetes deployment manifests
- [ ] Horizontal pod autoscaling
- [ ] Service mesh integration
- [ ] Resource optimization

### 📅 Week 16: Resilience & Release Preparation
- [ ] Circuit breakers
- [ ] Bulkhead pattern
- [ ] Chaos engineering tests
- [ ] Performance benchmarking
- [ ] Documentation finalization
- [ ] Release candidate preparation

---

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

1. **Fork the repository** and create a feature branch
2. **Follow code style** and ensure all tests pass
3. **Write meaningful commit messages**
4. **Update documentation** for any new features
5. **Submit a pull request** with a clear description

### Development Guidelines

- Use Java 21 features where appropriate
- Follow Spring Boot best practices
- Write unit and integration tests
- Ensure code passes linting and formatting checks
- Never commit secrets or sensitive information

---

## 📝 License

[Specify your license here]

---

## 👤 Author

**Workflow Orchestrator Team**

Built with ❤️ for enterprise workflow automation.

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)

---

## 🔒 Security Notes

- **Never commit** `.env` files or secrets to version control
- Use secrets management tools in production (AWS Secrets Manager, HashiCorp Vault, etc.)
- Rotate JWT secrets and RSA keys regularly
- Use strong RSA keys (2048-bit minimum, 4096-bit recommended for production)
- Enable SSL/TLS for all database connections in production
- Implement proper network security and firewall rules

---

**Last Updated:** 2024

