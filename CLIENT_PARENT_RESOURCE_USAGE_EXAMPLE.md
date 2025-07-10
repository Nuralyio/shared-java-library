# Client Library - Parent Resource Management Usage

This document demonstrates how to use the enhanced client library for parent resource management operations.

## Overview

The client library now supports:
- ✅ **Update resource parent** - Update a resource's properties including parent relationships
- ✅ **Remove resource parent** - Remove the parent relationship from a resource
- ✅ **Get child resources** - Fetch all child resources of a given parent

## Setup

```java
import com.nuraly.library.permissions.client.PermissionClient;
import com.nuraly.library.permissions.client.HttpPermissionClient;
import com.nuraly.library.permissions.client.model.*;

// Initialize the client
PermissionClient client = new HttpPermissionClient("http://localhost:8080", 30);
```

## 1. Update Resource Parent

Update a resource's properties including changing its parent:

```java
// Update a resource and change its parent
UpdateResourceRequest updateRequest = new UpdateResourceRequest()
    .withName("Updated Document Name")
    .withDescription("Updated description")
    .withParentResourceId("new-parent-folder-id");

try {
    ResourceResponse updatedResource = client.updateResource("document-123", updateRequest);
    System.out.println("Updated resource: " + updatedResource.getName());
    System.out.println("New parent: " + updatedResource.getParentResourceId());
} catch (RuntimeException e) {
    System.err.println("Failed to update resource: " + e.getMessage());
}
```

## 2. Set Resource Parent (Dedicated Operation)

Use the dedicated parent assignment operation:

```java
// Set a parent for an existing resource
SetParentResourceRequest setParentRequest = new SetParentResourceRequest(
    "parent-folder-123", 
    "Moving document to project folder"
);

try {
    ResourceResponse resource = client.setResourceParent("document-456", setParentRequest);
    System.out.println("Resource " + resource.getId() + " now has parent: " + resource.getParentResourceId());
} catch (RuntimeException e) {
    System.err.println("Failed to set parent: " + e.getMessage());
}
```

## 3. Remove Resource Parent

Remove a parent relationship from a resource:

```java
// Remove parent from a resource
try {
    ResourceResponse resource = client.removeResourceParent("document-789", "Moving to root level");
    System.out.println("Resource " + resource.getId() + " parent removed");
    System.out.println("Parent ID is now: " + resource.getParentResourceId()); // Should be null
} catch (RuntimeException e) {
    System.err.println("Failed to remove parent: " + e.getMessage());
}
```

## 4. Get Child Resources

Fetch all child resources of a parent:

```java
// Get all children of a parent resource
try {
    List<ResourceResponse> children = client.getChildResources("parent-folder-123");
    
    System.out.println("Found " + children.size() + " child resources:");
    for (ResourceResponse child : children) {
        System.out.println("- " + child.getName() + " (" + child.getResourceType() + ")");
    }
} catch (RuntimeException e) {
    System.err.println("Failed to get children: " + e.getMessage());
}
```

## Complete Example: Organizing Resources

Here's a complete example that demonstrates organizing files into folders:

```java
public class ResourceOrganizationExample {
    
    private final PermissionClient client;
    
    public ResourceOrganizationExample() {
        this.client = new HttpPermissionClient("http://localhost:8080", 30);
    }
    
    public void organizeProjectFiles() {
        try {
            // 1. Create a project folder
            CreateResourceRequest folderRequest = new CreateResourceRequest()
                .withResourceId("project-folder")
                .withName("My Project")
                .withResourceType("folder")
                .withTenantId("tenant-123")
                .withOwnerId("user-456");
            
            CreateResourceResponse projectFolder = client.createResource(folderRequest);
            System.out.println("Created project folder: " + projectFolder.getResourceId());
            
            // 2. Create some documents
            String[] documentIds = {"doc-1", "doc-2", "doc-3"};
            for (String docId : documentIds) {
                CreateResourceRequest docRequest = new CreateResourceRequest()
                    .withResourceId(docId)
                    .withName("Document " + docId.split("-")[1])
                    .withResourceType("document")
                    .withTenantId("tenant-123")
                    .withOwnerId("user-456");
                
                client.createResource(docRequest);
                System.out.println("Created document: " + docId);
            }
            
            // 3. Move documents into the project folder
            for (String docId : documentIds) {
                SetParentResourceRequest setParentRequest = new SetParentResourceRequest(
                    projectFolder.getResourceId(), 
                    "Organizing documents into project structure"
                );
                
                ResourceResponse movedDoc = client.setResourceParent(docId, setParentRequest);
                System.out.println("Moved " + docId + " to project folder");
            }
            
            // 4. Verify the organization by listing children
            List<ResourceResponse> children = client.getChildResources(projectFolder.getResourceId());
            System.out.println("\nProject contains " + children.size() + " documents:");
            for (ResourceResponse child : children) {
                System.out.println("- " + child.getName() + " (" + child.getId() + ")");
            }
            
            // 5. Move one document back to root level
            ResourceResponse rootDoc = client.removeResourceParent("doc-1", "Moving back to root for public access");
            System.out.println("\nMoved " + rootDoc.getId() + " back to root level");
            
            // 6. Check remaining children
            List<ResourceResponse> remainingChildren = client.getChildResources(projectFolder.getResourceId());
            System.out.println("Project now contains " + remainingChildren.size() + " documents");
            
        } catch (RuntimeException e) {
            System.err.println("Error during organization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        ResourceOrganizationExample example = new ResourceOrganizationExample();
        example.organizeProjectFiles();
    }
}
```

## Error Handling

All parent resource operations can throw `RuntimeException` in the following cases:

- **Network/Connection Issues**: Service unreachable, timeouts
- **Authentication Issues**: Invalid user context or missing permissions
- **Business Logic Errors**: Circular references, invalid parent IDs
- **Server Errors**: 4xx/5xx HTTP responses from the permissions service

Example error handling:

```java
try {
    ResourceResponse resource = client.setResourceParent("child-id", new SetParentResourceRequest("parent-id"));
} catch (RuntimeException e) {
    if (e.getMessage().contains("HTTP 400")) {
        System.err.println("Invalid request - possibly circular reference");
    } else if (e.getMessage().contains("HTTP 403")) {
        System.err.println("Permission denied - user cannot modify resource");
    } else if (e.getMessage().contains("HTTP 404")) {
        System.err.println("Resource not found");
    } else {
        System.err.println("Unexpected error: " + e.getMessage());
    }
}
```

## API Endpoints

The client library makes calls to these server endpoints:

| Operation | Method | Endpoint | Description |
|-----------|--------|----------|-------------|
| Update Resource | PUT | `/api/v1/acl/resources/{id}` | Update resource properties including parent |
| Set Parent | PUT | `/api/v1/acl/resources/{id}/parent` | Set or change resource parent |
| Remove Parent | PUT | `/api/v1/acl/resources/{id}/parent` | Remove resource parent (null parentResourceId) |
| Get Children | GET | `/api/v1/acl/resources/{id}/children` | Get child resources |

## User Context

All operations automatically include user context from the current request:
- Uses injected `UserContextService` when available
- Falls back to CDI lookup in non-CDI environments
- Includes both user ID and tenant ID in `X-USER` header
- Throws `RuntimeException` if user context is unavailable

This ensures proper authorization and multi-tenancy support for all parent resource operations.
