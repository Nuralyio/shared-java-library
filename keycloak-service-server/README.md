# Keycloak Service

A Quarkus-based microservice for managing Keycloak user operations and providing user lookup functionality for the Nuraly ACL system.

## Features

- User lookup by email address
- User lookup by UUID
- Email to UUID mapping (single and batch)
- Bulk user retrieval by IDs
- Bulk email-to-UUID mapping with structured response
- Redis caching for improved performance
- Service account authentication (secure)
- Comprehensive error handling and logging
- REST API with OpenAPI documentation

## Prerequisites

- Java 21
- Maven 3.9+
- Redis server (for caching)
- Keycloak server (18.0+ recommended)

## 🔧 Keycloak Setup

### Step 1: Create a Service Account Client

1. **Login to Keycloak Admin Console**
   ```
   http://localhost:8080/admin/
   ```

2. **Navigate to your realm** (e.g., `master` or create a new realm)

3. **Create a new Client**:
   - Go to `Clients` → `Create client`
   - **Client ID**: `nuraly-service` (or your preferred name)
   - **Client Type**: `OpenID Connect`
   - **Client authentication**: `On`
   - Click `Save`

4. **Configure Client Settings**:
   - **Access Type**: `confidential`
   - **Service Accounts Enabled**: `On` ✅ **CRITICAL**
   - **Standard Flow Enabled**: `Off`
   - **Direct Access Grants Enabled**: `Off`
   - **Authorization Enabled**: `Off`
   - Click `Save`

5. **Get Client Secret**:
   - Go to `Credentials` tab
   - Copy the `Client Secret` - you'll need this for configuration

### Step 2: Assign Required Roles to Service Account

1. **Navigate to Service Account Roles**:
   - Go to your client → `Service accounts roles` tab

2. **Assign Realm Management Roles**:
   - Click `Assign role`
   - Filter by clients: Select `realm-management`
   - Assign these roles:
     - ✅ `view-users` - **REQUIRED** (allows reading user information)
     - ✅ `query-users` - **REQUIRED** (allows searching for users)
     - ✅ `view-realm` - **RECOMMENDED** (allows viewing realm info)

3. **Verify Role Assignment**:
   Your service account should have these assigned roles:
   ```
   realm-management: view-users
   realm-management: query-users
   realm-management: view-realm
   ```

## ⚙️ Application Configuration

### Option 1: Environment Variables (Recommended for Production)
```bash
export KEYCLOAK_CLIENT_SECRET="your-actual-client-secret-here"
```

### Option 2: Direct Configuration (Development)
Update `src/main/resources/application.properties`:

```properties
# Keycloak Configuration
keycloak.base-url=http://localhost:8080/auth
keycloak.realm=master
keycloak.client-id=nuraly-service
keycloak.client-secret=YOUR_CLIENT_SECRET_HERE

# Redis Configuration (for caching)
quarkus.redis.hosts=redis://localhost:6379

# HTTP Configuration
quarkus.http.port=7012

# OpenAPI Configuration
quarkus.smallrye-openapi.info-title=Keycloak Service API
quarkus.smallrye-openapi.info-version=1.0.0
quarkus.smallrye-openapi.info-description=Keycloak user management service for Nuraly platform
quarkus.swagger-ui.path=/q/swagger-ui
quarkus.swagger-ui.always-include=true

# Logging Configuration
quarkus.log.category."com.nuraly.keycloak".level=DEBUG
```

### Configuration Properties Explained

| Property | Description | Example |
|----------|-------------|---------|
| `keycloak.base-url` | Base URL of your Keycloak server | `http://localhost:8080/auth` |
| `keycloak.realm` | Keycloak realm name | `master` or `nuraly` |
| `keycloak.client-id` | Service account client ID | `nuraly-service` |
| `keycloak.client-secret` | Client secret (use env var in prod) | `DiwCVTave2SLskLEoE9p4HHeef96YsZd` |
| `quarkus.redis.hosts` | Redis connection string | `redis://localhost:6379` |
| `quarkus.http.port` | HTTP port for the service | `7012` |

## 🚀 Running the Application

### Development Mode (Hot Reload)
```bash
cd keycloak-service-server
mvn quarkus:dev
```
or from the root directory:
```bash
mvn quarkus:dev -pl keycloak-service-server
```

### Production Mode
```bash
mvn clean package
java -jar target/quarkus-app/quarkus-run.jar
```

### Using Docker
```bash
# Build JVM image
mvn clean package
docker build -f src/main/docker/Dockerfile.jvm -t keycloak-service .

# Run container
docker run -p 7012:7012 \
  -e KEYCLOAK_CLIENT_SECRET=your-secret \
  -e QUARKUS_REDIS_HOSTS=redis://your-redis:6379 \
  keycloak-service
```

## 📡 API Endpoints

### User Lookup Endpoints

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| `GET` | `/api/v1/users/by-email/{email}` | Get user by email | `UserRepresentation` |
| `GET` | `/api/v1/users/{id}` | Get user by UUID | `UserRepresentation` |
| `GET` | `/api/v1/users/email-to-uuid/{email}` | Get UUID for single email | `{"email": "...", "uuid": "..."}` |
| `POST` | `/api/v1/users/by-ids` | Get multiple users by UUIDs | `UserRepresentation[]` |
| `POST` | `/api/v1/users/emails-to-uuids` | **NEW** Get UUIDs for multiple emails | `EmailToUuidMapping[]` |

### Health and Documentation

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/q/health` | Health check |
| `GET` | `/q/swagger-ui` | API documentation |
| `GET` | `/q/openapi` | OpenAPI specification |

## 🔄 API Usage Examples

### Single User Lookup
```bash
# Get user by email
curl http://localhost:7012/api/v1/users/by-email/john@example.com

# Get user by UUID  
curl http://localhost:7012/api/v1/users/550e8400-e29b-41d4-a716-446655440000
```

### Batch Operations
```bash
# Get multiple users by UUIDs
curl -X POST http://localhost:7012/api/v1/users/by-ids \
  -H "Content-Type: application/json" \
  -d '["uuid1", "uuid2", "uuid3"]'

# Get UUIDs for multiple emails (NEW ENDPOINT)
curl -X POST http://localhost:7012/api/v1/users/emails-to-uuids \
  -H "Content-Type: application/json" \
  -d '["john@example.com", "jane@example.com", "bob@example.com"]'
```

### Response Format for Batch Email-to-UUID Mapping
```json
[
  {
    "email": "john@example.com",
    "uuid": "550e8400-e29b-41d4-a716-446655440000"
  },
  {
    "email": "jane@example.com", 
    "uuid": "660f9500-f30c-52e5-b827-557766551111"
  }
]
```

## 🛠️ Troubleshooting

### Common Issues

#### 1. **Authentication Failed (401/403)**
```
PERMISSION DENIED: Service account 'nuraly-service' lacks required permissions
```
**Solution**: Ensure service account has required roles:
- Go to Keycloak Admin Console
- Navigate to Clients → nuraly-service → Service accounts roles
- Assign `view-users` and `query-users` from `realm-management`

#### 2. **Connection Refused**
```
Failed to initialize Keycloak service
```
**Solution**: Check Keycloak server connectivity:
- Verify `keycloak.base-url` is correct
- Ensure Keycloak server is running
- Check firewall/network connectivity

#### 3. **Invalid Client Credentials**
```
Failed to obtain access token - check Keycloak configuration
```
**Solution**: Verify client configuration:
- Check `keycloak.client-id` matches your client name
- Verify `keycloak.client-secret` is correct
- Ensure "Service Accounts Enabled" is `On`

#### 4. **Redis Connection Issues**
```
Unable to connect to Redis
```
**Solution**: 
- Ensure Redis server is running: `redis-server`
- Check Redis connection string: `quarkus.redis.hosts=redis://localhost:6379`
- Test Redis connectivity: `redis-cli ping`

### Debugging Steps

1. **Enable Debug Logging**:
   ```properties
   quarkus.log.category."com.nuraly.keycloak".level=DEBUG
   ```

2. **Check Application Health**:
   ```bash
   curl http://localhost:7012/q/health
   ```

3. **Verify Keycloak Token**:
   ```bash
   curl -X POST http://localhost:8080/auth/realms/master/protocol/openid-connect/token \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "grant_type=client_credentials&client_id=nuraly-service&client_secret=YOUR_SECRET"
   ```

## 🔒 Security Best Practices

1. **Use Environment Variables for Secrets**:
   ```bash
   export KEYCLOAK_CLIENT_SECRET="your-secret"
   ```

2. **Principle of Least Privilege**:
   - Only assign necessary roles (`view-users`, `query-users`)
   - Don't use admin credentials

3. **Network Security**:
   - Use HTTPS in production
   - Restrict network access to Keycloak admin interface
   - Use secure Redis connection

4. **Monitoring**:
   - Monitor application logs for failed authentication attempts
   - Set up alerts for service health

## 📚 Additional Resources

- **Swagger UI**: `http://localhost:7012/q/swagger-ui/`
- **Health Check**: `http://localhost:7012/q/health`
- **Keycloak Documentation**: [https://www.keycloak.org/documentation](https://www.keycloak.org/documentation)
- **Quarkus Documentation**: [https://quarkus.io/](https://quarkus.io/)

## 🆕 Recent Updates

### v1.0.0-SNAPSHOT
- ✅ Added batch email-to-UUID mapping endpoint (`/emails-to-uuids`)
- ✅ Structured response format with `EmailToUuidMapping` objects
- ✅ Enhanced error handling and logging
- ✅ Service account authentication (more secure than username/password)
- ✅ Comprehensive API documentation
- ✅ Redis caching for improved performance
