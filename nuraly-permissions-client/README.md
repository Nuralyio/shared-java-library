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

### 5. Resource Discovery

Get lists of accessible resource IDs for building UIs, dashboards, or navigation:

```java
@Inject
PermissionClient permissionClient;

// Get all document IDs a user can read (with tenant context)
List<String> readableDocuments = permissionClient.getAccessibleResourceIds(
    "user123", "read", "document", "tenant789"
);

// Get accessible resources across all tenants (no tenant filter)
List<String> allAccessibleDocs = permissionClient.getAccessibleResourceIds(
    "user123", "read", "document", null  // tenantId is optional
);

// Get accessible resources with pagination and metadata
AccessibleResourcesResponse response = permissionClient.getAccessibleResources(
    "user123", "read", "document", "tenant789",  // or null for all tenants
    50,  // limit
    0    // offset
);

System.out.println("Found " + response.getTotalCount() + " accessible documents");
response.getResourceIds().forEach(System.out::println);

// Quick check if user has any accessible resources of a type
boolean hasAnyImages = permissionClient.hasAnyAccessibleResources(
    "user123", "read", "image", null  // Check across all tenants
);

if (hasAnyImages) {
    // Show image gallery UI
}
```

**Use Cases for Resource Discovery:**
- 📋 **Dashboard widgets**: Show counts of accessible resources
- 🗂️ **Navigation menus**: Hide/show menu items based on accessible resources  
- 📊 **Data tables**: Pre-filter data to only accessible items
- 🔍 **Search results**: Limit search scope to accessible resources
- 📱 **Mobile apps**: Efficient data synchronization

## API Reference

### PermissionClient Interface

```java
public interface PermissionClient {
    // Permission checking
    boolean hasPermission(String userId, String permissionType, String resourceId, String tenantId);
    boolean hasAnonymousPermission(String resourceId, String permissionType, String tenantId);
    boolean validatePublicLink(String token, String permissionType);
    
    // Resource discovery (NEW) - tenantId and resourceType are optional (can be null)
    List<String> getAccessibleResourceIds(String userId, String permissionType, 
                                         String resourceType, String tenantId);
    AccessibleResourcesResponse getAccessibleResources(String userId, String permissionType, 
                                                      String resourceType, String tenantId, 
                                                      int limit, int offset);
    boolean hasAnyAccessibleResources(String userId, String permissionType, 
                                     String resourceType, String tenantId);
    
    // Health check
    boolean isHealthy();
}
```

### AccessibleResourcesResponse Model

```java
public class AccessibleResourcesResponse {
    private List<String> resourceIds;      // List of accessible resource IDs
    private String permissionType;         // The permission type queried
    private String resourceType;           // The resource type filter (if any)
    private String tenantId;              // The tenant context
    private int totalCount;               // Total count (for pagination)
    
    // getters and setters...
}
```

### Parameter Behavior

**Optional Parameters:**
- `tenantId`: When `null`, searches across all tenants the user has access to
- `resourceType`: When `null`, returns resources of all types
- `limit`: When `0`, returns all results (no pagination)
- `offset`: When `0`, starts from the beginning

**Examples:**
```java
// All accessible resources across all tenants and types
List<String> everything = client.getAccessibleResourceIds(userId, "read", null, null);

// Only documents in a specific tenant
List<String> tenantDocs = client.getAccessibleResourceIds(userId, "read", "document", "tenant123");

// All images the user can edit (any tenant)
List<String> editableImages = client.getAccessibleResourceIds(userId, "write", "image", null);
```

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
