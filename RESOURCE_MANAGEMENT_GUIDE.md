# Resource Management in Nuraly Permissions

## Overview

After analyzing the ACL server code, I found that **the server does not provide a resource creation endpoint**. The ACL system is designed to manage permissions for resources that are created and managed externally.

## How Resource Management Works

### Server Architecture
The ACL server assumes resources exist outside the ACL system and only manages permissions for them. The `Resource` entity has these key fields:

- `externalId` - Reference to the actual resource in your application
- `externalTenantId` - External tenant ID
- `ownerId` - External User ID who owns this resource
- `resourceType` - Type of resource (e.g., "document", "dashboard", "function")
- `name` - Human-readable name
- `description` - Optional description

### Available Endpoints
The ACL server provides these endpoints:
- `/api/v1/acl/check-permission` - Check user permissions
- `/api/v1/acl/check-anonymous-permission` - Check anonymous access
- `/api/v1/acl/grant-permission` - Grant permissions
- `/api/v1/acl/revoke-permission` - Revoke permissions
- `/api/v1/acl/share-resource` - Share resources
- `/api/v1/acl/publish-resource` - Make resources public
- `/api/v1/acl/accessible-resources` - Get accessible resources
- `/api/v1/acl/public-resources` - Get public resources
- `/api/v1/acl/validate-public-link/{token}` - Validate public access tokens

**No resource creation endpoint exists.**

## How to Manage Resources

### 1. Create Resources in Your Application First
Create the actual resource (document, dashboard, etc.) in your application's database/storage.

### 2. Register Resource with ACL System
You need to manually insert the resource into the ACL database:

```sql
INSERT INTO acl_resources (
    id, 
    name, 
    description, 
    resource_type, 
    external_id, 
    external_tenant_id, 
    owner_id, 
    is_active, 
    created_at, 
    updated_at
) VALUES (
    gen_random_uuid(),
    'My Document',
    'A sample document',
    'document',
    'doc_12345',  -- Your application's resource ID
    'tenant_uuid',
    'user_uuid',
    true,
    NOW(),
    NOW()
);
```

### 3. Grant Initial Permissions
After registering the resource, you can use the ACL API to grant permissions:

```java
// Use the client to grant permissions after resource registration
permissionClient.hasPermission(userId, "read", resourceId, tenantId);
```

## Client Library Changes

### Updated Implementation
The `createResource` method in `HttpPermissionClient` now throws `UnsupportedOperationException` with a clear explanation:

```java
@Override
public CreateResourceResponse createResource(CreateResourceRequest request) {
    throw new UnsupportedOperationException(
        "Resource creation is not supported by the ACL server. " +
        "Resources are managed externally and referenced by externalId in the ACL system. " +
        "You should create the resource in your application first, then register it with the ACL system " +
        "by directly inserting into the acl_resources table or using a separate resource registration endpoint."
    );
}
```

## Recommended Workflow

1. **Create Resource**: Create the actual resource in your application
2. **Register with ACL**: Insert into `acl_resources` table with your resource's external ID
3. **Grant Permissions**: Use ACL API to grant initial permissions to the owner
4. **Check Permissions**: Use the client library for all permission checks

## Alternative: Create Resource Registration Endpoint

If you need programmatic resource registration, you could add a new endpoint to the ACL server like:

```java
@POST
@Path("/register-resource")
public Response registerResource(ResourceRegistrationRequest request) {
    // Implementation to register an external resource
}
```

But this would require modifying the server code.

## Summary

The ACL system is designed as a **permissions overlay** for resources managed by external applications, not as a resource management system itself. This separation of concerns allows the ACL system to be lightweight and focused purely on access control.
