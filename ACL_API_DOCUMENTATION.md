# Advanced ACL System for Nuraly Platform

## Overview

This comprehensive Access Control List (ACL) system provides fine-grained permission management inspired by PowerApps, with full support for anonymous access, delegation, and multi-tenant architecture.

## Key Features

### ✅ **Implemented Features**
- **Fine-grained permissions**: Custom permissions beyond CRUD (read, write, annotate, publish, share, moderate, etc.)
- **Multi-scope permissions**: Application, Organization, and Resource level
- **Role-based access control**: Hierarchical roles with inheritance
- **Anonymous/Public access**: Public sharing with configurable permissions
- **Delegation system**: Users can share resources with others
- **Audit logging**: Complete audit trail of all permission changes
- **Multi-tenancy**: Full tenant isolation
- **Public link sharing**: Generate shareable links with expiration
- **Organization membership**: Team-based permissions
- **Share policies**: Reusable permission templates

### 🔐 **Security Features**
- **Immediate revocation**: Real-time permission changes
- **Expiration support**: Time-limited permissions
- **Audit trails**: All actions are logged
- **Token-based access**: Secure public link generation
- **Multi-tenant isolation**: Complete data separation

## API Endpoints

### Permission Checking

#### Check User Permission
```http
POST /api/v1/acl/check-permission
Content-Type: application/json

{
  "userId": "uuid",
  "resourceId": "uuid", 
  "permissionName": "read",
  "tenantId": "uuid"
}
```

#### Check Anonymous Permission
```http
POST /api/v1/acl/check-anonymous-permission
Content-Type: application/json

{
  "resourceId": "uuid",
  "permissionName": "read", 
  "tenantId": "uuid"
}
```

### Permission Management

#### Grant Permission
```http
POST /api/v1/acl/grant-permission
Content-Type: application/json
X-USER: {"uuid": "user-id"}

{
  "userId": "uuid",
  "resourceId": "uuid",
  "permissionId": "uuid",
  "grantedBy": "uuid",
  "tenantId": "uuid"
}
```

#### Revoke Permission
```http
POST /api/v1/acl/revoke-permission
Content-Type: application/json
X-USER: {"uuid": "user-id"}

{
  "userId": "uuid",
  "resourceId": "uuid", 
  "permissionId": "uuid",
  "revokedBy": "uuid",
  "reason": "No longer needed",
  "tenantId": "uuid"
}
```

### Resource Sharing

#### Share Resource with Role
```http
POST /api/v1/acl/share-resource
Content-Type: application/json
X-USER: {"uuid": "user-id"}

{
  "resourceId": "uuid",
  "targetUserId": "uuid",
  "roleId": "uuid",
  "sharedBy": "uuid",
  "tenantId": "uuid"
}
```

#### Publish Resource Publicly
```http
POST /api/v1/acl/publish-resource
Content-Type: application/json
X-USER: {"uuid": "user-id"}

{
  "resourceId": "uuid",
  "permissionNames": ["read", "annotate"],
  "publishedBy": "uuid",
  "tenantId": "uuid"
}
```

#### Unpublish Resource
```http
POST /api/v1/acl/unpublish-resource
Content-Type: application/json
X-USER: {"uuid": "user-id"}

{
  "resourceId": "uuid",
  "unpublishedBy": "uuid",
  "tenantId": "uuid"
}
```

### Resource Access

#### Get Accessible Resources
```http
GET /api/v1/acl/accessible-resources/{userId}?tenantId={tenantId}
```

#### Get Public Resources
```http
GET /api/v1/acl/public-resources
```

#### Validate Public Link
```http
GET /api/v1/acl/validate-public-link/{token}?permission=read
```

#### Get Resource by Public Token
```http
GET /api/v1/acl/public-resource/{token}
```

## Advanced Examples

### Example 1: Share a Resource Publicly with Read-Only Access

```http
POST /api/v1/examples/share-public-readonly
Content-Type: application/json
X-USER: {"uuid": "owner-id"}

{
  "resourceId": "document-123",
  "publishedBy": "owner-id",
  "tenantId": "tenant-123"
}
```

**Response:**
```json
{
  "publicToken": "abc123-def456-ghi789",
  "message": "Resource shared publicly with read-only access",
  "permissions": ["read"]
}
```

### Example 2: Assign a User as "Editor" to Organization Documents

```http
POST /api/v1/examples/assign-editor-role
Content-Type: application/json
X-USER: {"uuid": "admin-id"}

{
  "userId": "editor-user-id",
  "organizationId": "org-123",
  "assignedBy": "admin-id", 
  "tenantId": "tenant-123"
}
```

**Response:**
```json
{
  "message": "User assigned as Editor for organization",
  "grantsCreated": 15,
  "roleName": "Editor"
}
```

### Example 3: Revoke Anonymous Access

```http
POST /api/v1/examples/revoke-anonymous-access
Content-Type: application/json
X-USER: {"uuid": "owner-id"}

{
  "resourceId": "document-123",
  "revokedBy": "owner-id",
  "tenantId": "tenant-123"
}
```

### Example 4: Generate Public Link with Expiration

```http
POST /api/v1/examples/generate-public-link
Content-Type: application/json
X-USER: {"uuid": "owner-id"}

{
  "resourceId": "document-123",
  "generatedBy": "owner-id",
  "tenantId": "tenant-123",
  "accessLevel": "COMMENT",
  "expiresInHours": 24
}
```

**Response:**
```json
{
  "publicToken": "xyz789-abc123-def456",
  "expiresAt": "2025-07-04T12:00:00",
  "permissions": ["read", "annotate"],
  "message": "Public link generated successfully"
}
```

## Anonymous Access Patterns

### 1. Public Dashboard Access
```http
GET /api/v1/acl/public-resource/dashboard-token-123
```

### 2. Anonymous Permission Check
```http
POST /api/v1/acl/check-anonymous-permission
Content-Type: application/json

{
  "resourceId": "dashboard-456",
  "permissionName": "read",
  "tenantId": "tenant-123"
}
```

### 3. Public Link Validation
```http
GET /api/v1/acl/validate-public-link/token-123?permission=read
```

## Authentication Headers

### Authenticated Requests
```http
X-USER: {"uuid": "user-id", "email": "user@example.com"}
X-TENANT-ID: tenant-uuid
```

### Anonymous Requests with Public Token
```http
X-PUBLIC-TOKEN: public-link-token-123
```

### Anonymous Requests (No headers needed)
For public resources that allow anonymous access.

## Default System Roles

### Application Level
- **Super Admin**: Full system access
- **Platform User**: Basic platform access

### Organization Level  
- **Organization Owner**: Full organization control
- **Organization Admin**: Organization management
- **Organization Member**: Regular member access
- **Organization Guest**: Limited guest access

### Resource Level
- **Viewer**: Read-only access
- **Editor**: Read, write, annotate, share
- **Publisher**: Editor permissions + publish
- **Moderator**: Publisher permissions + moderate

## Default System Permissions

### General Permissions
- `read`: View content
- `write`: Edit content  
- `delete`: Delete content
- `share`: Share with others
- `publish`: Publish/unpublish
- `annotate`: Add comments/annotations
- `moderate`: Moderate content
- `admin`: Administrative access

### Resource-Specific Permissions
- `document:read`, `document:write`, `document:annotate`, etc.
- `dashboard:read`, `dashboard:write`, `dashboard:share`, etc.
- `function:read`, `function:write`, `function:execute`, `function:deploy`
- `organization:read`, `organization:write`, `organization:admin`, `organization:invite`, `organization:remove`

## Database Schema

### Core Tables
- `acl_users`: User accounts
- `acl_roles`: Role definitions
- `acl_permissions`: Permission definitions
- `acl_resources`: Resources to protect
- `acl_organizations`: Organization/tenant structure
- `acl_resource_grants`: Permission grants
- `acl_organization_memberships`: User-organization relationships
- `acl_audit_logs`: Audit trail
- `acl_share_policies`: Sharing policy templates

### Key Relationships
- Users ↔ Roles (Many-to-Many)
- Roles ↔ Permissions (Many-to-Many)
- Users ↔ Organizations (Many-to-Many through memberships)
- Resources → Organizations (Many-to-One)
- Resource Grants → Users/Roles/Resources (Many-to-One)

## Multi-Tenant Architecture

### Tenant Isolation
- All entities include `tenantId` field
- Data is automatically filtered by tenant
- Cross-tenant access is prevented
- Audit logs track tenant-specific actions

### Tenant Initialization
```java
@Inject
ACLInitializationService aclInit;

// Initialize new tenant
aclInit.initializeTenant(tenantId);
```

## Performance Considerations

### Caching
- Permission checks are cached using Quarkus Cache
- Role hierarchies are cached for performance
- Public resource lists are cached

### Scalability
- Supports millions of resources
- Efficient PostgreSQL queries with proper indexing
- Audit logs can be archived for long-term storage
- Resource hierarchy supports deep nesting

## Integration Examples

### Annotation-Based Protection
```java
@GET
@Path("/documents/{id}")
@RequiresPermission(
    permissionType = "read",
    resourceType = "document", 
    resourceId = "#{id}"
)
public Response getDocument(@PathParam("id") String id) {
    // Method implementation
}
```

### Service-Based Protection
```java
@Inject
ACLService aclService;

public void accessResource(UUID userId, UUID resourceId) {
    if (!aclService.hasPermission(userId, resourceId, "read", tenantId)) {
        throw new SecurityException("Access denied");
    }
    // Access granted
}
```

### Anonymous Access Support
```java
@GET
@Path("/public/dashboard/{token}")
public Response getPublicDashboard(@PathParam("token") String token) {
    if (!aclService.validatePublicLink(token, "read")) {
        throw new SecurityException("Invalid public link");
    }
    // Return dashboard
}
```

## Error Handling

### Common Error Responses
```json
{
  "error": "Permission denied for user on resource"
}
```

```json
{
  "error": "Invalid or expired public token"
}
```

```json
{
  "error": "Resource not found"
}
```

## Audit Logging

All ACL operations are automatically logged including:
- Permission grants and revocations
- Resource sharing events
- Role assignments
- Anonymous access attempts
- Public link generation
- Organization membership changes

Query audit logs:
```java
List<AuditLog> logs = AuditLog.findByResource(resourceId);
```

This system provides enterprise-grade access control with the flexibility of PowerApps and the security needed for multi-tenant SaaS platforms.
