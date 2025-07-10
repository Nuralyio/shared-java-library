package com.nuraly.library.acl.service;

import com.nuraly.library.acl.dto.*;
import com.nuraly.library.acl.model.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.UUID;

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
        
        // Create new resource
        Resource resource = new Resource();
        resource.id = UUID.randomUUID().toString(); // Generate unique ID
        resource.name = request.name.trim();
        resource.description = request.description != null ? request.description.trim() : null;
        resource.resourceType = request.resourceType.trim();
        resource.externalId = request.externalId.trim();
        resource.externalTenantId = currentTenantId;
        resource.ownerId = request.ownerId != null ? request.ownerId : currentUserId; // Allow specifying owner or default to current user
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
}
