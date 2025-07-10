package com.nuraly.library.acl.service;

import com.nuraly.library.acl.dto.*;
import com.nuraly.library.acl.model.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.UUID;
import java.util.List;

/**
 * Service for managing resource operations
 */
@ApplicationScoped
public class ResourceManagementService {
    
    @Inject
    ACLValidationService validationService;
    
    @Inject
    UserContextService userContextService;
    
    /**
     * Register a new resource with the ACL system
     */
    @Transactional
    public Resource registerResource(RegisterResourceRequest request, UUID currentUserId, UUID currentTenantId) {
        // Additional null checks (though validation should catch these)
        if (request.name == null || request.name.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource name is required and cannot be blank");
        }
        
        if (request.resourceType == null || request.resourceType.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource type is required and cannot be blank");
        }
        
        if (request.externalId == null || request.externalId.trim().isEmpty()) {
            throw new IllegalArgumentException("External ID is required and cannot be blank");
        }
        
        // Validate parent resource if specified
        Resource parentResource = null;
        if (request.parentResourceId != null && !request.parentResourceId.trim().isEmpty()) {
            parentResource = Resource.findById(request.parentResourceId.trim());
            if (parentResource == null) {
                throw new IllegalArgumentException("Parent resource not found: " + request.parentResourceId);
            }
            
            // Ensure parent resource is in the same tenant
            if (!parentResource.externalTenantId.equals(currentTenantId)) {
                throw new SecurityException("Parent resource not in current tenant");
            }
            
            // Note: No circular reference check needed for new resource creation
            // since the resource doesn't exist yet, it cannot create a cycle
        }
        
        // Create new resource
        Resource resource = new Resource();
        resource.id = UUID.randomUUID().toString(); // Generate unique ID
        resource.name = request.name.trim();
        resource.description = request.description != null ? request.description.trim() : null;
        resource.resourceType = request.resourceType.trim();
        resource.externalId = request.externalId.trim();
        resource.externalTenantId = currentTenantId;
        resource.ownerId = request.ownerId != null ? request.ownerId : currentUserId; // Allow specifying owner or default to current user
        resource.parentResource = parentResource; // Set parent if specified
        resource.isActive = true;
        resource.isPublic = false;
        
        resource.persist();
        
        return resource;
    }
    
    /**
     * Transfer ownership of a resource to another user
     */
    @Transactional
    public Resource transferOwnership(String resourceId, UUID newOwnerId, UUID currentUserId, UUID currentTenantId) {
        Resource resource = Resource.findById(resourceId);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found");
        }
        
        // Only current owner can transfer ownership
        if (!currentUserId.equals(resource.ownerId)) {
            throw new SecurityException("Only resource owner can transfer ownership");
        }
        
        // Ensure resource is in current tenant
        if (!resource.externalTenantId.equals(currentTenantId)) {
            throw new SecurityException("Resource not in current tenant");
        }
        
        // Transfer ownership
        resource.ownerId = newOwnerId;
        resource.persist();
        
        return resource;
    }
    
    /**
     * Update resource metadata
     */
    @Transactional
    public Resource updateResource(String resourceId, UpdateResourceRequest request) {
        Resource resource = Resource.findById(resourceId);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found");
        }
        
        // Update allowed fields
        if (request.name != null) {
            resource.name = request.name;
        }
        if (request.description != null) {
            resource.description = request.description;
        }
        
        // Handle parent resource update
        if (request.parentResourceId != null) {
            if (request.parentResourceId.trim().isEmpty()) {
                // Remove parent relationship
                resource.parentResource = null;
            } else {
                // Set new parent
                Resource newParent = Resource.findById(request.parentResourceId);
                if (newParent == null) {
                    throw new IllegalArgumentException("Parent resource not found: " + request.parentResourceId);
                }
                
                // Ensure parent resource is in the same tenant
                if (!newParent.externalTenantId.equals(resource.externalTenantId)) {
                    throw new SecurityException("Parent resource not in same tenant");
                }
                
                // Prevent setting self as parent
                if (newParent.id.equals(resourceId)) {
                    throw new IllegalArgumentException("Resource cannot be its own parent");
                }
                
                // Prevent circular references
                if (wouldCreateCircularReference(newParent, resourceId)) {
                    throw new IllegalArgumentException("Setting parent would create circular reference");
                }
                
                resource.parentResource = newParent;
            }
        }
        
        resource.persist();
        
        return resource;
    }
    
    /**
     * Delete/deactivate a resource
     */
    @Transactional
    public void deleteResource(String resourceId, UUID currentUserId) {
        Resource resource = Resource.findById(resourceId);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found");
        }
        
        // Only owner can delete resource
        if (!currentUserId.equals(resource.ownerId)) {
            throw new SecurityException("Only resource owner can delete resource");
        }
        
        // Soft delete - deactivate instead of hard delete to preserve audit trail
        resource.isActive = false;
        resource.persist();
    }
    
    /**
     * Set or update parent resource for hierarchical relationships
     */
    @Transactional
    public Resource setParentResource(String resourceId, SetParentResourceRequest request, UUID currentUserId, UUID currentTenantId) {
        Resource resource = Resource.findById(resourceId);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found");
        }
        
        // Ensure resource is in current tenant
        if (!resource.externalTenantId.equals(currentTenantId)) {
            throw new SecurityException("Resource not in current tenant");
        }
        
        // Check if user has permission to modify resource hierarchy
        // Only owner or admin can modify parent relationships
        if (!currentUserId.equals(resource.ownerId)) {
            // TODO: Add check for admin permission when available
            throw new SecurityException("Only resource owner can modify parent relationships");
        }
        
        if (request.parentResourceId == null || request.parentResourceId.trim().isEmpty()) {
            // Remove parent relationship
            resource.parentResource = null;
        } else {
            // Set new parent
            Resource newParent = Resource.findById(request.parentResourceId);
            if (newParent == null) {
                throw new IllegalArgumentException("Parent resource not found: " + request.parentResourceId);
            }
            
            // Ensure parent resource is in the same tenant
            if (!newParent.externalTenantId.equals(currentTenantId)) {
                throw new SecurityException("Parent resource not in current tenant");
            }
            
            // Prevent setting self as parent
            if (newParent.id.equals(resourceId)) {
                throw new IllegalArgumentException("Resource cannot be its own parent");
            }
            
            // Prevent circular references
            if (wouldCreateCircularReference(newParent, resourceId)) {
                throw new IllegalArgumentException("Setting parent would create circular reference");
            }
            
            resource.parentResource = newParent;
        }
        
        resource.persist();
        return resource;
    }
    
    /**
     * Check if setting a parent would create a circular reference
     */
    private boolean wouldCreateCircularReference(Resource potentialParent, String childResourceId) {
        Resource current = potentialParent;
        while (current != null) {
            if (current.id.equals(childResourceId)) {
                return true;
            }
            current = current.parentResource;
        }
        return false;
    }
    
    /**
     * Get resource hierarchy (children) for a given resource
     */
    public List<Resource> getResourceChildren(String resourceId, UUID currentTenantId) {
        Resource resource = Resource.findById(resourceId);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found");
        }
        
        // Ensure resource is in current tenant
        if (!resource.externalTenantId.equals(currentTenantId)) {
            throw new SecurityException("Resource not in current tenant");
        }
        
        return Resource.find("parentResource.id = ?1 and isActive = true", resourceId).list();
    }
}
