package com.nuraly.library.acl.service;

import com.nuraly.library.acl.dto.ErrorResponse;
import com.nuraly.library.acl.model.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

/**
 * Service for handling ACL validation logic
 */
@ApplicationScoped
public class ACLValidationService {
    
    @Inject
    UserContextService userContextService;
    
    @Inject
    ACLService aclService;
    
    /**
     * Validates that a resource exists
     */
    public ValidationResult validateResourceExists(String resourceId) {
        Resource resource = Resource.findById(resourceId);
        if (resource == null) {
            return ValidationResult.error(
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Resource not found"))
                    .build()
            );
        }
        return ValidationResult.success();
    }
    
    /**
     * Validates that the current user is authenticated
     */
    public ValidationResult validateAuthentication() {
        UUID currentUserId = userContextService.getCurrentUserId();
        if (currentUserId == null) {
            return ValidationResult.error(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Authentication required"))
                    .build()
            );
        }
        return ValidationResult.success();
    }
    
    /**
     * Validates that the current user is the owner of a resource or has the required permission
     */
    public ValidationResult validateCurrentUserOwnershipOrPermission(String resourceId, String permissionName) {
        UUID currentUserId = userContextService.getCurrentUserId();
        if (currentUserId == null) {
            return ValidationResult.error(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Authentication required"))
                    .build()
            );
        }
        
        UUID currentTenantId = userContextService.getCurrentTenantId();
        return validateOwnershipOrPermission(currentUserId, resourceId, permissionName, currentTenantId);
    }
    
    /**
     * Validates that the current user has the required permission on a resource
     */
    public ValidationResult validateCurrentUserPermission(String resourceId, String permissionName) {
        UUID currentUserId = userContextService.getCurrentUserId();
        if (currentUserId == null) {
            return ValidationResult.error(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Authentication required"))
                    .build()
            );
        }
        
        UUID currentTenantId = userContextService.getCurrentTenantId();
        return validatePermission(currentUserId, resourceId, permissionName, currentTenantId);
    }
    
    /**
     * Validates that a user is the owner of a resource or has the required permission
     */
    public ValidationResult validateOwnershipOrPermission(UUID userId, String resourceId, String permissionName, UUID tenantId) {
        // Check if user is the owner of the resource
        Resource resource = Resource.findById(resourceId);
        if (resource != null && userId.equals(resource.ownerId)) {
            return ValidationResult.success(); // Owner has full access
        }
        
        // If not owner, check for specific permission
        boolean hasPermission = aclService.hasPermission(userId, resourceId, permissionName, tenantId);
        
        if (!hasPermission) {
            return ValidationResult.error(
                Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse("Access denied: must be owner or have " + permissionName + " permission"))
                    .build()
            );
        }
        return ValidationResult.success();
    }
    
    /**
     * Validates that a user has the required permission on a resource
     */
    public ValidationResult validatePermission(UUID userId, String resourceId, String permissionName, UUID tenantId) {
        boolean hasPermission = aclService.hasPermission(userId, resourceId, permissionName, tenantId);
        
        if (!hasPermission) {
            return ValidationResult.error(
                Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse("Access denied: " + permissionName + " permission required"))
                    .build()
            );
        }
        return ValidationResult.success();
    }
    
    /**
     * Validates that the current user is the owner of a resource
     */
    public ValidationResult validateCurrentUserIsOwner(String resourceId) {
        return validateCurrentUserIsOwner(resourceId, "Only resource owner can perform this action");
    }
    
    /**
     * Validates that the current user is the owner of a resource with custom error message
     */
    public ValidationResult validateCurrentUserIsOwner(String resourceId, String errorMessage) {
        UUID currentUserId = userContextService.getCurrentUserId();
        if (currentUserId == null) {
            return ValidationResult.error(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Authentication required"))
                    .build()
            );
        }
        
        Resource resource = Resource.findById(resourceId);
        if (resource == null) {
            return ValidationResult.error(
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Resource not found"))
                    .build()
            );
        }
        
        if (!currentUserId.equals(resource.ownerId)) {
            return ValidationResult.error(
                Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(errorMessage))
                    .build()
            );
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validates that the resource belongs to the current tenant
     */
    public ValidationResult validateResourceInCurrentTenant(String resourceId) {
        UUID currentTenantId = userContextService.getCurrentTenantId();
        Resource resource = Resource.findById(resourceId);
        
        if (resource != null && currentTenantId != null && !resource.externalTenantId.equals(currentTenantId)) {
            return ValidationResult.error(
                Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse("Resource not in current tenant"))
                    .build()
            );
        }
        
        return ValidationResult.success();
    }
}
