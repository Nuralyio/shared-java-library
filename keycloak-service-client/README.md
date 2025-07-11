# Keycloak Service Client

A lightweight client library for interacting with the Keycloak Service. This client provides a simple API for user lookup operations and email-to-UUID conversions.

## Features

- **User Lookup**: Get users by email or UUID
- **Batch Operations**: Convert multiple emails to UUIDs in a single request
- **CDI Integration**: Full Jakarta EE/MicroProfile integration
- **Configuration-based**: Configurable service endpoints and timeouts
- **Error Handling**: Comprehensive error handling and logging
- **Health Checks**: Service availability monitoring

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.nuraly.library</groupId>
    <artifactId>keycloak-service-client</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Configuration

Configure the client using MicroProfile Config properties:

```properties
# Keycloak Service Configuration
keycloak.service.base-url=http://localhost:7012
keycloak.service.timeout-seconds=30
```

## Usage

### CDI Injection

```java
@Inject
KeycloakClient keycloakClient;

// Get user by email
Optional<User> user = keycloakClient.getUserByEmail("user@example.com");

// Get user by UUID
Optional<User> user = keycloakClient.getUserById("123e4567-e89b-12d3-a456-426614174000");

// Convert email to UUID
Optional<String> uuid = keycloakClient.getEmailToUuidMapping("user@example.com");
```

### Programmatic Usage

```java
// Create client instance
KeycloakClient client = HttpKeycloakClient.create("http://localhost:7012");

// Or use configuration
KeycloakClient client = HttpKeycloakClient.createFromConfig();

// Batch email to UUID conversion
List<String> emails = Arrays.asList("user1@example.com", "user2@example.com");
List<EmailToUuidMapping> mappings = client.getEmailToUuidMappings(emails);
```

### User Context Service

Extract user information from HTTP request headers:

```java
@Inject
UserContextService userContext;

@GET
@Path("/profile")
public Response getUserProfile() {
    Optional<String> userId = userContext.getCurrentUserId();
    Optional<String> email = userContext.getCurrentUserEmail();
    Optional<String> tenantId = userContext.getCurrentTenantId();
    
    if (userContext.isAuthenticated()) {
        // Handle authenticated request
        return Response.ok().build();
    } else {
        return Response.status(401).build();
    }
}
```

## API Reference

### KeycloakClient Interface

#### User Lookup Methods

- `Optional<User> getUserByEmail(String email)` - Get user by email address
- `Optional<User> getUserById(String id)` - Get user by UUID
- `List<User> getUsersByIds(List<String> ids)` - Get multiple users by UUIDs

#### Email to UUID Conversion

- `Optional<String> getEmailToUuidMapping(String email)` - Convert single email to UUID
- `List<String> getUserIdsByEmails(List<String> emails)` - Convert multiple emails to UUIDs
- `List<EmailToUuidMapping> getEmailToUuidMappings(List<String> emails)` - Detailed mappings
- `EmailToUuidResponse convertEmailsToUuids(EmailToUuidRequest request)` - Batch conversion with error details

#### Health Check

- `boolean isServiceHealthy()` - Check if Keycloak service is available

### Model Classes

#### User
Represents a Keycloak user with essential information:
- `id` - User UUID
- `username` - Username
- `email` - Email address
- `firstName` - First name
- `lastName` - Last name
- `enabled` - Account status
- `emailVerified` - Email verification status
- `attributes` - Custom attributes

#### EmailToUuidMapping
Represents an email to UUID mapping:
- `email` - Email address
- `uuid` - Corresponding user UUID

#### EmailToUuidRequest/Response
Request and response models for batch operations.

## Error Handling

The client handles various error scenarios:

- **Service Unavailable**: Returns empty results, logs warnings
- **Invalid Configuration**: Throws configuration exceptions at startup
- **Network Errors**: Returns empty results, logs errors
- **Malformed Responses**: Returns empty results, logs warnings

### Exception Types

- `KeycloakServiceException` - General service errors with optional HTTP status codes

## Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `keycloak.service.base-url` | `http://localhost:7012` | Base URL of the Keycloak service |
| `keycloak.service.timeout-seconds` | `30` | HTTP request timeout in seconds |

## HTTP Headers

The client expects these headers for user context:

- `X-USER` - JSON object with user information (contains `uuid`, `email`, etc.)
- `X-TENANT` - Tenant ID for multi-tenant applications

Example X-USER header:
```json
{
  "uuid": "123e4567-e89b-12d3-a456-426614174000",
  "email": "user@example.com",
  "username": "johndoe",
  "firstName": "John",
  "lastName": "Doe"
}
```

## Examples

### Basic User Lookup

```java
@RestController
@Path("/users")
public class UserController {
    
    @Inject
    KeycloakClient keycloakClient;
    
    @GET
    @Path("/search")
    public Response searchUser(@QueryParam("email") String email) {
        Optional<User> user = keycloakClient.getUserByEmail(email);
        
        if (user.isPresent()) {
            return Response.ok(user.get()).build();
        } else {
            return Response.status(404)
                .entity("User not found")
                .build();
        }
    }
}
```

### Batch Email Conversion

```java
@POST
@Path("/convert-emails")
public Response convertEmails(EmailToUuidRequest request) {
    EmailToUuidResponse response = keycloakClient.convertEmailsToUuids(request);
    return Response.ok(response).build();
}
```

### Health Check

```java
@GET
@Path("/health/keycloak")
public Response checkKeycloakHealth() {
    boolean healthy = keycloakClient.isServiceHealthy();
    
    if (healthy) {
        return Response.ok()
            .entity("{\"status\": \"UP\", \"service\": \"keycloak\"}")
            .build();
    } else {
        return Response.status(503)
            .entity("{\"status\": \"DOWN\", \"service\": \"keycloak\"}")
            .build();
    }
}
```

## Integration with Permission System

This client can be used together with the Nuraly Permissions Client for complete user and permission management:

```java
@Inject
KeycloakClient keycloakClient;

@Inject
PermissionClient permissionClient;

@GET
@Path("/documents/{id}")
public Response getDocument(@PathParam("id") String documentId) {
    // Get current user from context
    Optional<String> userEmail = userContext.getCurrentUserEmail();
    
    if (userEmail.isPresent()) {
        // Convert email to UUID for permission check
        Optional<String> userId = keycloakClient.getEmailToUuidMapping(userEmail.get());
        
        if (userId.isPresent()) {
            // Check permissions
            boolean hasAccess = permissionClient.hasPermission(
                userId.get(), documentId, "read"
            );
            
            if (hasAccess) {
                // Return document
                return Response.ok().build();
            }
        }
    }
    
    return Response.status(403).build();
}
```

## Logging

The client uses Java Util Logging. Configure logging levels as needed:

```properties
com.nuraly.keycloak.client.level=INFO
```

For detailed debugging:
```properties
com.nuraly.keycloak.client.level=FINE
```
