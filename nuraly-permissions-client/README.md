# Nuraly Permissions Client

Lightweight permission checking client library for microservices. This library provides declarative permission checking via annotations without requiring heavyweight database dependencies.

## Features

- 🏷️ **Declarative permissions** with `@RequiresPermission` annotation
- 🌐 **HTTP-based** permission checking (delegates to permissions service)
- 📦 **Lightweight** - no database dependencies
- 🔄 **Multi-access support** - authenticated users, anonymous access, public tokens
- 🏢 **Multi-tenant** ready
- ⚡ **Fast startup** - minimal dependencies

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>com.nuraly.library</groupId>
    <artifactId>nuraly-permissions-client</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Configure Properties

```properties
# Required: URL of the permissions service
nuraly.permissions.service.url=http://permissions-service:8080

# Optional: HTTP timeout (default: 5 seconds)
nuraly.permissions.client.timeout.seconds=10
```

### 3. Use Annotations

```java
@Path("/api/documents")
public class DocumentResource {
    
    @GET
    @Path("/{id}")
    @RequiresPermission(
        permissionType = "read",
        resourceType = "document", 
        resourceId = "#{id}"
    )
    public Response getDocument(@PathParam("id") String id) {
        // Business logic - permission already checked
        return Response.ok(documentService.getDocument(id)).build();
    }
    
    @POST
    @RequiresPermission(
        permissionType = "create",
        resourceType = "document",
        resourceId = "new"
    )
    public Response createDocument(Document doc) {
        // Create logic
        return Response.ok().build();
    }
}
```

### 4. Programmatic Usage

```java
@Inject
PermissionClient permissionClient;

public void businessMethod() {
    if (permissionClient.hasPermission("user123", "read", "doc456", "tenant789")) {
        // Perform operation
    }
}
```

## Supported Access Patterns

### Authenticated Users
```http
X-USER: {"uuid": "user123", "name": "John Doe"}
X-TENANT-ID: tenant789
```

### Anonymous Access
```http
X-TENANT-ID: tenant789
# No X-USER header - will check anonymous permissions
```

### Public Token Access
```http
X-PUBLIC-TOKEN: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
# Token-based access for shared links
```

## Dynamic Parameter Resolution

The `resourceId` field supports dynamic resolution:

```java
@RequiresPermission(
    permissionType = "read",
    resourceType = "document",
    resourceId = "#{documentId}"  // Resolves from @PathParam or @QueryParam
)
```

## Architecture

```
┌─────────────────┐    HTTP     ┌─────────────────────┐
│   Microservice  │─────────────│  Permissions Service │
│                 │             │                     │
│ @RequiresPermi- │             │  ┌─────────────────┐ │
│ ssion           │             │  │   ACL Service   │ │
│                 │             │  │   Database      │ │
│ PermissionClient│             │  │   Audit Logs    │ │
└─────────────────┘             │  └─────────────────┘ │
                                └─────────────────────┘
```

## Dependencies

- Jakarta EE APIs (provided scope)
- Jackson for JSON processing
- Java HTTP Client (built-in)
- **No database dependencies**
- **No ORM frameworks**

Total size: ~200KB vs ~50MB+ for full ACL library

## Error Handling

- **Fail closed**: Denies access on service errors
- **HTTP 403**: Returns proper error responses
- **Configurable timeouts**: Prevents hanging requests
- **Health checks**: Monitor service availability

## Testing

```java
@TestMethodOrder(OrderAnnotation.class)
class PermissionTest {
    
    @Test
    void testWithMockHeaders() {
        given()
            .header("X-USER", "{\"uuid\":\"user123\"}")
            .header("X-TENANT-ID", "tenant1")
            .when()
            .get("/api/documents/doc123")
            .then()
            .statusCode(200);
    }
}
```
