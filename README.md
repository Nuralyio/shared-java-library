# Nuraly Advanced ACL System

An enterprise-grade Access Control List (ACL) system built with Quarkus, inspired by PowerApps security model with full support for anonymous access, delegation, and multi-tenant architecture.

## 📦 Project Structure

This project is split into two main components:

### 🔧 **nuraly-permissions-client** (Lightweight Library)
Lightweight permission checking client for microservices:
- **Size**: ~200KB 
- **Dependencies**: Jakarta EE APIs, Jackson, HTTP Client
- **Purpose**: Declarative permission checking with `@RequiresPermission` annotation
- **Usage**: Include in microservices that need permission checking

### 🏢 **nuraly-permissions-server** (Full ACL Service)  
Complete ACL service with database and full feature set:
- **Size**: ~50MB+
- **Dependencies**: Quarkus, Hibernate, PostgreSQL, etc.
- **Purpose**: Central permissions service with full ACL capabilities
- **Usage**: Deploy as standalone service

## 🚀 Quick Start for Microservices

### 1. Add Client Dependency
```xml
<dependency>
    <groupId>com.nuraly.library</groupId>
    <artifactId>nuraly-permissions-client</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Configure Properties
```properties
nuraly.permissions.service.url=http://permissions-service:8080
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
        return Response.ok().build();
    }
}
```

### 4. Get Accessible Resources
```java
@Inject
private PermissionClient permissionClient;

// Get all document IDs a user can read in a specific tenant
List<String> readableDocuments = permissionClient.getAccessibleResourceIds(
    userId, "read", "document", tenantId
);

// Get all accessible documents across all tenants (tenantId is optional)
List<String> allDocs = permissionClient.getAccessibleResourceIds(
    userId, "read", "document", null
);

// Get accessible resources with pagination
AccessibleResourcesResponse response = permissionClient.getAccessibleResources(
    userId, "read", "document", tenantId, 50, 0  // limit=50, offset=0
);

// Check if user has any accessible documents (across all tenants)
boolean hasAnyDocs = permissionClient.hasAnyAccessibleResources(
    userId, "read", "document", null  // tenantId is optional
);
```

## 🏗️ Architecture

```
┌─────────────────────┐    HTTP     ┌─────────────────────────┐
│    Microservice A   │─────────────│                         │
│  @RequiresPermission│             │  Permissions Service    │
│  PermissionClient   │             │  (nuraly-permissions-   │
├─────────────────────┤             │   server)               │
│    Microservice B   │─────────────│                         │
│  @RequiresPermission│             │  ┌─────────────────────┐ │
│  PermissionClient   │             │  │   ACL Service       │ │
├─────────────────────┤             │  │   Database          │ │
│    Microservice C   │─────────────│  │   Audit Logs        │ │
│  @RequiresPermission│             │  │   Multi-tenancy     │ │
│  PermissionClient   │             │  └─────────────────────┘ │
└─────────────────────┘             └─────────────────────────┘
```

## 🚀 Features

### Core ACL Capabilities
- **Fine-grained permissions**: Custom permissions beyond CRUD (read, write, annotate, publish, share, moderate, etc.)
- **Multi-scope permissions**: Application, Tenant, and Resource level permissions
- **Role-based access control**: Hierarchical roles with inheritance
- **Anonymous/Public access**: Public sharing with configurable permissions and expiration
- **Delegation system**: Users can share resources with others using predefined roles
- **Real-time revocation**: Immediate permission changes with clean propagation
- **Multi-tenancy**: Complete tenant isolation with shared system roles
- **Resource discovery**: Get lists of accessible resource IDs for efficient UI building

### PowerApps-Inspired Features
- **Security roles**: Reusable permission bundles (Viewer, Editor, Publisher, Moderator)
- **Custom permission sets**: Define tenant-specific permissions
- **Record-level access control**: Per-resource permission management
- **Sharing policies**: Templates like "anyone with the link can view"
- **Environment-level security**: Tenant-wide permission management
- **Ownership-based security**: Automatic owner permissions

### Advanced Security
- **Audit logging**: Complete audit trail of all permission changes
- **Public link generation**: Secure token-based anonymous access
- **Time-limited access**: Permissions with expiration dates
- **Usage tracking**: Monitor resource access patterns

## 🔧 Development

### Building the Entire Project
```shell script
mvn clean install
```

### Running the Permissions Service (Server)
```shell script
cd nuraly-permissions-server
./mvnw quarkus:dev
```

### Running Client Tests
```shell script
cd nuraly-permissions-client  
mvn test
```

## 📚 Documentation

- **Client Usage**: See `nuraly-permissions-client/README.md`
- **Server Setup**: See `nuraly-permissions-server/README.md` 
- **API Documentation**: Available at `/q/swagger-ui` when running server

This project uses Quarkus, the Supersonic Subatomic Java Framework. If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.
