# Keycloak Service

A Quarkus-based microservice for managing Keycloak user operations and providing user lookup functionality for the Nuraly ACL system.

## Features

- User lookup by email
- User lookup by ID
- Email to UUID mapping
- Bulk user retrieval
- Redis caching for performance
- REST API with OpenAPI documentation

## Prerequisites

- Java 21
- Maven 3.9+
- Redis server
- Keycloak server

## Configuration

Copy the application.properties and configure the following:

```properties
# Keycloak Configuration
keycloak.base-url=http://your-keycloak-server:8080
keycloak.realm=your-realm
keycloak.admin.username=admin
keycloak.admin.password=your-admin-password

# Redis Configuration
quarkus.redis.hosts=redis://your-redis-server:6379
```

## Running the Application

### Development Mode
```bash
./mvnw compile quarkus:dev
```

### Production Mode
```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

## API Endpoints

- `GET /api/v1/users/by-email/{email}` - Get user by email
- `GET /api/v1/users/{id}` - Get user by ID
- `GET /api/v1/users/email-to-uuid/{email}` - Get UUID for email
- `POST /api/v1/users/by-ids` - Get multiple users by IDs
- `GET /health` - Health check

## API Documentation

OpenAPI/Swagger UI is available at: `http://localhost:7012/q/swagger-ui/`

## Docker

```bash
# Build native image
./mvnw package -Pnative

# Build JVM image
docker build -f src/main/docker/Dockerfile.jvm -t keycloak-service .
```
