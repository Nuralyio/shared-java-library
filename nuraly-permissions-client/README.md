# Nuraly Permissions Client

Lightweight permission checking client library for microservices. This library provides declarative permission checking via annotations without requiring heavyweight database dependencies.

## Features

- 🏷️ **Declarative permissions** with `@RequiresPermission` annotation
- 🌐 **HTTP-based** permission checking (delegates to permissions service)
- 📦 **Lightweight** - no database dependencies
- 🔄 **Multi-access support** - authenticated users, anonymous access, public tokens
- 🏢 **Multi-tenant** ready
- ⚡ **Fast startup** - minimal dependencies
- 🎯 **Context-aware methods** - automatic user/tenant extraction from request headers (NEW)
- 🔧 **Flexible API** - both explicit parameters and context-aware overloaded methods (NEW)
- 📋 **Resource discovery** - get lists of accessible resource IDs for efficient UI building
- 🔄 **Backward compatible** - all existing code continues to work unchanged
                throw new RuntimeException("Failed to create resource: " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create resource", e);
        }
    }
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

The client uses CDI configuration injection and supports MicroProfile Config. Add these properties to your `application.properties`:

```properties
# Required: URL of the permissions service
nuraly.permissions.service.url=http://permissions-service:8080

# Optional: HTTP timeout in seconds (default: 5)
nuraly.permissions.client.timeout.seconds=10
```

**Framework-specific configuration:**

- **Quarkus**: Add to `src/main/resources/application.properties`
- **Spring Boot**: Add to `application.properties` or `application.yml`
- **Other Jakarta EE**: Add to `META-INF/microprofile-config.properties` or as system properties

**Environment Variables** (alternative to properties file):
```bash
export NURALY_PERMISSIONS_SERVICE_URL=http://permissions-service:8080
export NURALY_PERMISSIONS_CLIENT_TIMEOUT_SECONDS=10
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

**Option A: Explicit User/Tenant Parameters (Traditional)**
```java
@Inject
PermissionClient permissionClient;

public void businessMethod() {
    if (permissionClient.hasPermission("user123", "read", "doc456", "tenant789")) {
        // Perform operation
    }
    
    // Get accessible resources for specific user
    List<String> docs = permissionClient.getAccessibleResourceIds(
        "user123", "read", "document", "tenant789"
    );
}
```

**Option B: Context-Aware (NEW - Recommended for REST endpoints)**
```java
@Inject
PermissionClient permissionClient;

@GET
@Path("/documents/{id}")
public Response getDocument(@PathParam("id") String documentId) {
    // User and tenant automatically extracted from X-USER header
    if (permissionClient.hasPermission("read", documentId)) {
        // Perform operation
        return Response.ok(loadDocument(documentId)).build();
    } else {
        return Response.status(403).build();
    }
}

@GET
@Path("/documents")
public Response getMyDocuments() {
    // Get documents accessible to current user (from request context)
    List<String> accessibleDocs = permissionClient.getAccessibleResourceIds(
        "read", "document"
    );
    
    // Load only accessible documents
    List<Document> documents = documentService.loadDocuments(accessibleDocs);
    return Response.ok(documents).build();
}
```

**Key Benefits of Context-Aware Methods:**
- 🎯 **Cleaner code**: No need to manually extract user/tenant from request headers
- 🔒 **More secure**: Can't accidentally pass wrong user ID
- 🚀 **Easier to use**: Perfect for REST endpoints where user context is in headers
- 🔄 **Backward compatible**: Original explicit parameter methods still work

### 5. Creating Resources with Permission Registration

When creating documents/files in your application, you need to also create the corresponding resource in the permissions system to enable access control:

```java
import com.nuraly.library.permissions.client.model.CreateResourceRequest;

@Inject
PermissionClient permissionClient; // For checking permissions and creating resources

@Service
public class DocumentService {
    
    public Document createDocument(Document document) {
        // 1. Create the document in your application
        Document savedDocument = documentRepository.save(document);
        
        // 2. Register the new resource in the permissions system
        // The library automatically extracts user context and makes user the owner
        // Owner automatically gets all permissions (read, write, delete)
        try {
            CreateResourceRequest permissionRequest = CreateResourceRequest
                .forResource(savedDocument.getId(), "document")
                .withMetadata(Map.of(
                    "title", savedDocument.getTitle(),
                    "category", savedDocument.getCategory()
                ));
                
            // Create resource - user becomes owner with full permissions automatically
            permissionClient.createResource(permissionRequest);
            
        } catch (Exception e) {
            // If permission registration fails, consider rolling back the document creation
            documentRepository.delete(savedDocument);
            throw new RuntimeException("Failed to register resource permissions", e);
        }
        
        return savedDocument;
    }
    
    public void deleteDocument(String userId, String documentId, String tenantId) {
        // 1. Check delete permission
        if (!permissionClient.hasPermission(userId, "delete", documentId, tenantId)) {
            throw new PermissionDeniedException("Cannot delete document");
        }
        
        // 2. Delete from application
        documentRepository.deleteById(documentId);
        
        // 3. Clean up permissions (remove resource from permissions system)
        try {
            permissionClient.deleteResource(documentId, tenantId);
        } catch (Exception e) {
            // Log but don't fail - resource is already deleted from app
            log.warn("Failed to clean up permissions for deleted resource: " + documentId, e);
        }
    }
}
```

**File/Image Creation Example:**
```java
@Service
public class FileService {
    
    public FileMetadata uploadFile(MultipartFile file) {
        // 1. Save file to storage
        String fileId = UUID.randomUUID().toString();
        String filePath = fileStorage.save(file, fileId);
        
        FileMetadata metadata = new FileMetadata()
            .setId(fileId)
            .setOriginalName(file.getOriginalFilename())
            .setPath(filePath)
            .setSize(file.getSize())
            .setMimeType(file.getContentType());
            
        fileRepository.save(metadata);
        
        // 2. Register in permissions system (user becomes owner automatically)
        CreateResourceRequest request = CreateResourceRequest
            .forResource(fileId, "file")
            .withMetadata(Map.of(
                "filename", file.getOriginalFilename(),
                "mimetype", file.getContentType(),
                "size", String.valueOf(file.getSize())
            ));
            
        // Create resource - user becomes owner with full permissions automatically
        permissionClient.createResource(request);
        
        return metadata;
    }
}
```

**Bulk Resource Creation:**
```java
@Transactional
public List<Document> createDocuments(List<Document> documents) {
    List<Document> createdDocs = new ArrayList<>();
    List<String> permissionResourceIds = new ArrayList<>();
    
    try {
        // 1. Create all documents first
        for (Document doc : documents) {
            Document saved = documentRepository.save(doc);
            createdDocs.add(saved);
        }
        
        // 2. Register all in permissions system (users become owners automatically)
        for (Document doc : createdDocs) {
            CreateResourceRequest request = CreateResourceRequest
                .forResource(doc.getId(), "document");
                
            // Create resource - user becomes owner with full permissions automatically
            permissionClient.createResource(request);
            permissionResourceIds.add(doc.getId());
        }
        
        return createdDocs;
        
    } catch (Exception e) {
        // Rollback: clean up any created permissions
        for (String resourceId : permissionResourceIds) {
            try {
                permissionClient.deleteResource(resourceId);
            } catch (Exception cleanupError) {
                log.warn("Failed to cleanup permission resource: " + resourceId, cleanupError);
            }
        }
        throw e;
    }
}
```

**Key Points:**
- 🔗 **Always register resources** in permissions system after creating them in your app
- 👤 **User automatically becomes owner** with full permissions (read, write, delete)
- 📦 **Single API call** - just create the resource, ownership and permissions are automatic
- 🔄 **Handle failures** by rolling back if permission registration fails
- 🧹 **Clean up permissions** when deleting resources from your app
- 📊 **Include metadata** to make permission management easier

**PermissionClient Implementation:**

The `PermissionClient` interface now includes the `createResource` method for unified resource management. The `HttpPermissionClient` implementation provides both permission checking and resource creation:

```java
@Inject
PermissionClient permissionClient; // Supports both permission checking AND resource creation

// Create a resource with automatic ownership
CreateResourceRequest request = CreateResourceRequest.forResource("doc123", "My Document", "document");
CreateResourceResponse response = permissionClient.createResource(request);

// Check permissions
boolean hasAccess = permissionClient.hasPermission("user1", "read", "doc123", "tenant1");
```

**Note:** The `createResource` method is now part of the standard `PermissionClient` interface, so you don't need a separate service client.

### 6. Resource Discovery
    
    @ConfigProperty(name = "nuraly.permissions.service.url")
    String permissionsServiceUrl;
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public void createResource(CreateResourceRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(permissionsServiceUrl + "/api/resources"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
                
            HttpResponse<String> response = httpClient.send(httpRequest, 
                HttpResponse.BodyHandlers.ofString());
                
            if (response.statusCode() != 201) {
                throw new RuntimeException("Failed to create resource: " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create resource", e);
        }
    }
    
    public void grantPermission(String permissionType, String resourceId) {
        try {
            // Library automatically extracts user and tenant from request context
            UserContextService userContext = CDI.current().select(UserContextService.class).get();
            String userId = userContext.getCurrentUserId();
            String tenantId = userContext.getCurrentTenantId();
            
            Map<String, Object> permissionData = Map.of(
                "userId", userId,
                "permissionType", permissionType,
                "resourceId", resourceId,
                "tenantId", tenantId
            );
            
            String json = objectMapper.writeValueAsString(permissionData);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(permissionsServiceUrl + "/api/permissions"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
                
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
                
            if (response.statusCode() != 201) {
                throw new RuntimeException("Failed to grant permission: " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to grant permission", e);
        }
    }
    
    public void deleteResource(String resourceId) {
        try {
            // Library automatically extracts tenant from request context
            UserContextService userContext = CDI.current().select(UserContextService.class).get();
            String tenantId = userContext.getCurrentTenantId();
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(permissionsServiceUrl + "/api/resources/" + resourceId + "?tenantId=" + tenantId))
                .DELETE()
                .build();
                
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
                
            if (response.statusCode() != 204) {
                throw new RuntimeException("Failed to delete resource: " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete resource", e);
        }
    }
}
```

### 6. Resource Discovery

Get lists of accessible resource IDs for building UIs, dashboards, or navigation:

**Option A: Explicit Parameters (Traditional)**
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
```

**Option B: Context-Aware (NEW - Recommended for REST endpoints)**
```java
@Inject
PermissionClient permissionClient;

@GET
@Path("/my-documents")
public Response getMyDocuments(@QueryParam("limit") @DefaultValue("50") int limit,
                              @QueryParam("offset") @DefaultValue("0") int offset) {
    // User and tenant automatically extracted from X-USER header
    AccessibleResourcesResponse response = permissionClient.getAccessibleResources(
        "read", "document", limit, offset
    );
    
    // Load actual documents
    List<Document> documents = documentService.loadDocuments(response.getResourceIds());
    
    return Response.ok(new DocumentListResponse(documents, response.getTotalCount())).build();
}

@GET
@Path("/has-any-images")
public Response hasAnyImages() {
    // Quick check if current user has any accessible images
    boolean hasImages = permissionClient.hasAnyAccessibleResources("read", "image");
    return Response.ok(Map.of("hasImages", hasImages)).build();
}

@GET
@Path("/accessible-document-ids")
public Response getAccessibleDocumentIds() {
    // Get just the IDs for current user (useful for efficient filtering)
    List<String> docIds = permissionClient.getAccessibleResourceIds("read", "document");
    return Response.ok(docIds).build();
}
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
    // Permission checking (explicit parameters)
    boolean hasPermission(String userId, String permissionType, String resourceId, String tenantId);
    boolean hasAnonymousPermission(String resourceId, String permissionType, String tenantId);
    boolean validatePublicLink(String token, String permissionType);
    
    // Permission checking (context-aware - NEW)
    boolean hasPermission(String permissionType, String resourceId);
    
    // Resource discovery (explicit parameters) - tenantId and resourceType are optional (can be null)
    List<String> getAccessibleResourceIds(String userId, String permissionType, 
                                         String resourceType, String tenantId);
    AccessibleResourcesResponse getAccessibleResources(String userId, String permissionType, 
                                                      String resourceType, String tenantId, 
                                                      int limit, int offset);
    boolean hasAnyAccessibleResources(String userId, String permissionType, 
                                     String resourceType, String tenantId);
    
    // Resource discovery (context-aware - NEW)
    List<String> getAccessibleResourceIds(String permissionType, String resourceType);
    AccessibleResourcesResponse getAccessibleResources(String permissionType, String resourceType, 
                                                      int limit, int offset);
    boolean hasAnyAccessibleResources(String permissionType, String resourceType);
    
    // Resource creation
    CreateResourceResponse createResource(CreateResourceRequest request);
    
    // Health check
    boolean isHealthy();
}
```

### Context-Aware Methods (NEW)

The client now supports **two ways** to call permission methods:

1. **Explicit parameters** (original): You provide userId and tenantId explicitly
2. **Context-aware** (new): User and tenant are extracted automatically from the current request's X-USER header

```java
@Inject
PermissionClient permissionClient;

// Option 1: Explicit parameters (traditional approach)
boolean hasAccess1 = permissionClient.hasPermission(
    "user123", "read", "doc456", "tenant789"
);

// Option 2: Context-aware (NEW - extracts user/tenant from current request)
boolean hasAccess2 = permissionClient.hasPermission("read", "doc456");

// Both methods work the same way internally, but option 2 is more convenient
// in REST endpoints where the user context is already in the request headers
```

**How Context Extraction Works:**

The context-aware methods automatically:
1. Extract the `X-USER` header from the current HTTP request
2. Parse the JSON to get `uuid` (userId) and `tenantId` 
3. Call the explicit parameter version with extracted values
4. Fall back to CDI lookup if `UserContextService` is not available

**Example X-USER header:**
```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "tenantId": "123e4567-e89b-12d3-a456-426614174000"
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
