package com.nuraly.library.acl.rest;

import com.nuraly.library.acl.model.*;
import com.nuraly.library.acl.service.ACLService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Comprehensive REST API for ACL Management
 * Provides endpoints for all ACL operations including anonymous access
 */
@Path("/api/v1/acl")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ACLResource {
    
    @Inject
    ACLService aclService;
    
    /**
     * Validates that a resource exists
     * @param resourceId The resource ID to validate
     * @return Response with error if resource not found, null if valid
     */
    private Response validateResourceExists(UUID resourceId) {
        Resource resource = Resource.findById(resourceId);
        if (resource == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("Resource not found"))
                .build();
        }
        return null;
    }
    
    /**
     * Validates that a user has the required permission on a resource
     * @param userId The user ID
     * @param resourceId The resource ID
     * @param permissionName The required permission name
     * @param tenantId The tenant ID
     * @param skipIfNullTenant Whether to skip check if tenantId is null
     * @return Response with error if permission denied, null if authorized
     */
    private Response validatePermission(UUID userId, UUID resourceId, String permissionName, 
                                      UUID tenantId, boolean skipIfNullTenant) {
        // Skip authorization check if tenantId is null and skipIfNullTenant is true
        if (skipIfNullTenant && tenantId == null) {
            return null;
        }
        
        boolean hasPermission = aclService.hasPermission(userId, resourceId, permissionName, tenantId);
        
        if (!hasPermission) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(new ErrorResponse("Access denied: " + permissionName + " permission required"))
                .build();
        }
        return null;
    }
    
    /**
     * Validates that a user has the required permission on a resource (no tenant skip option)
     */
    private Response validatePermission(UUID userId, UUID resourceId, String permissionName, UUID tenantId) {
        return validatePermission(userId, resourceId, permissionName, tenantId, false);
    }
    
    /**
     * Creates a standardized error response for exceptions
     */
    private Response createErrorResponse(Exception e) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(new ErrorResponse(e.getMessage()))
            .build();
    }
    
    /**
     * Check if user has permission on a resource
     * POST /api/v1/acl/check-permission
     */
    @POST
    @Path("/check-permission")
    public Response checkPermission(PermissionCheckRequest request) {
        try {
            boolean hasPermission = aclService.hasPermission(
                request.userId, 
                request.resourceId, 
                request.permissionName, 
                request.tenantId
            );
            
            return Response.ok(new PermissionCheckResponse(hasPermission, null)).build();
        } catch (Exception e) {
            return Response.ok(new PermissionCheckResponse(false, e.getMessage())).build();
        }
    }
    
    /**
     * Check anonymous permission on a resource
     * POST /api/v1/acl/check-anonymous-permission
     */
    @POST
    @Path("/check-anonymous-permission")
    public Response checkAnonymousPermission(AnonymousPermissionCheckRequest request) {
        try {
            boolean hasPermission = aclService.hasAnonymousPermission(
                request.resourceId, 
                request.permissionName, 
                request.tenantId
            );
            
            return Response.ok(new PermissionCheckResponse(hasPermission, null)).build();
        } catch (Exception e) {
            return Response.ok(new PermissionCheckResponse(false, e.getMessage())).build();
        }
    }
    
    /**
     * Grant permission to a user on a resource
     * POST /api/v1/acl/grant-permission
     */
    @POST
    @Path("/grant-permission")
    public Response grantPermission(GrantPermissionRequest request) {
        try {
            // Validate resource exists
            Response validationError = validateResourceExists(request.resourceId);
            if (validationError != null) return validationError;
            
            // Check authorization (skip if tenantId is null for backwards compatibility)
            Response authError = validatePermission(request.grantedBy, request.resourceId, 
                "admin", request.tenantId, true);
            if (authError != null) return authError;
            
            ResourceGrant grant = aclService.grantPermission(
                request.userId,
                request.resourceId,
                request.permissionId,
                request.grantedBy,
                request.tenantId
            );
            
            return Response.ok(grant).build();
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
    
    /**
     * Grant role-based permission on a resource
     * POST /api/v1/acl/grant-role-permission
     */
    @POST
    @Path("/grant-role-permission")
    public Response grantRolePermission(GrantRolePermissionRequest request) {
        try {
            // Validate resource exists
            Response validationError = validateResourceExists(request.resourceId);
            if (validationError != null) return validationError;
            
            // Check authorization
            Response authError = validatePermission(request.grantedBy, request.resourceId, 
                "admin", request.tenantId);
            if (authError != null) return authError;
            
            ResourceGrant grant = aclService.grantRolePermission(
                request.roleId,
                request.resourceId,
                request.permissionId,
                request.grantedBy,
                request.tenantId
            );
            
            return Response.ok(grant).build();
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
    
    /**
     * Revoke permission from a user on a resource
     * POST /api/v1/acl/revoke-permission
     */
    @POST
    @Path("/revoke-permission")
    public Response revokePermission(RevokePermissionRequest request) {
        try {
            // Validate resource exists
            Response validationError = validateResourceExists(request.resourceId);
            if (validationError != null) return validationError;
            
            // Check authorization
            Response authError = validatePermission(request.revokedBy, request.resourceId, 
                "admin", request.tenantId);
            if (authError != null) return authError;
            
            boolean revoked = aclService.revokePermission(
                request.userId,
                request.resourceId,
                request.permissionId,
                request.revokedBy,
                request.reason,
                request.tenantId
            );
            
            return Response.ok(new OperationResult(revoked, revoked ? "Permission revoked" : "Permission not found")).build();
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
    
    /**
     * Share a resource with another user
     * POST /api/v1/acl/share-resource
     */
    @POST
    @Path("/share-resource")
    public Response shareResource(ShareResourceRequest request) {
        try {
            // Validate resource exists
            Response validationError = validateResourceExists(request.resourceId);
            if (validationError != null) return validationError;
            
            // Check authorization
            Response authError = validatePermission(request.sharedBy, request.resourceId, 
                "share", request.tenantId);
            if (authError != null) return authError;
            
            List<ResourceGrant> grants = aclService.shareResource(
                request.resourceId,
                request.targetUserId,
                request.roleId,
                request.sharedBy,
                request.tenantId
            );
            
            return Response.ok(grants).build();
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
    
    /**
     * Publish a resource for public access
     * POST /api/v1/acl/publish-resource
     */
    @POST
    @Path("/publish-resource")
    public Response publishResource(PublishResourceRequest request) {
        try {
            // Validate resource exists
            Response validationError = validateResourceExists(request.resourceId);
            if (validationError != null) return validationError;
            
            // Check authorization
            Response authError = validatePermission(request.publishedBy, request.resourceId, 
                "publish", request.tenantId);
            if (authError != null) return authError;
            
            aclService.publishResource(
                request.resourceId,
                request.permissionNames,
                request.publishedBy,
                request.tenantId
            );
            
            return Response.ok(new OperationResult(true, "Resource published successfully")).build();
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
    
    /**
     * Unpublish a resource (remove public access)
     * POST /api/v1/acl/unpublish-resource
     */
    @POST
    @Path("/unpublish-resource")
    public Response unpublishResource(UnpublishResourceRequest request) {
        try {
            // Validate resource exists
            Response validationError = validateResourceExists(request.resourceId);
            if (validationError != null) return validationError;
            
            // Check authorization
            Response authError = validatePermission(request.unpublishedBy, request.resourceId, 
                "publish", request.tenantId);
            if (authError != null) return authError;
            
            aclService.unpublishResource(
                request.resourceId,
                request.unpublishedBy,
                request.tenantId
            );
            
            return Response.ok(new OperationResult(true, "Resource unpublished successfully")).build();
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
    
    /**
     * Get all resources accessible by a user
     * GET /api/v1/acl/accessible-resources/{userId}
     */
    @GET
    @Path("/accessible-resources/{userId}")
    public Response getAccessibleResources(@PathParam("userId") UUID userId, 
                                         @QueryParam("tenantId") UUID tenantId) {
        try {
            List<Resource> resources = aclService.getAccessibleResources(userId, tenantId);
            return Response.ok(resources).build();
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
    
    /**
     * Get all public resources
     * GET /api/v1/acl/public-resources
     */
    @GET
    @Path("/public-resources")
    public Response getPublicResources() {
        try {
            List<Resource> resources = aclService.getPublicResources();
            return Response.ok(resources).build();
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
    
    /**
     * Validate public link access
     * GET /api/v1/acl/validate-public-link/{token}
     */
    @GET
    @Path("/validate-public-link/{token}")
    public Response validatePublicLink(@PathParam("token") String token,
                                     @QueryParam("permission") String permissionName) {
        try {
            boolean isValid = aclService.validatePublicLink(token, permissionName);
            return Response.ok(new OperationResult(isValid, isValid ? "Valid public link" : "Invalid public link")).build();
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
    
    /**
     * Get resource by public token (for anonymous access)
     * GET /api/v1/acl/public-resource/{token}
     */
    @GET
    @Path("/public-resource/{token}")
    public Response getPublicResource(@PathParam("token") String token) {
        try {
            Resource resource = Resource.findByPublicToken(token);
            if (resource != null && resource.isPublicLinkValid()) {
                return Response.ok(resource).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Public resource not found or expired"))
                    .build();
            }
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
}

// Request/Response DTOs

class PermissionCheckRequest {
    public UUID userId;
    public UUID resourceId;
    public String permissionName;
    public UUID tenantId;
}

class AnonymousPermissionCheckRequest {
    public UUID resourceId;
    public String permissionName;
    public UUID tenantId;
}

class PermissionCheckResponse {
    public boolean hasPermission;
    public String errorMessage;
    
    public PermissionCheckResponse() {}
    
    public PermissionCheckResponse(boolean hasPermission, String errorMessage) {
        this.hasPermission = hasPermission;
        this.errorMessage = errorMessage;
    }
}

class GrantPermissionRequest {
    public UUID userId;
    public UUID resourceId;
    public UUID permissionId;
    public UUID grantedBy;
    public UUID tenantId;
    public LocalDateTime expiresAt;
}

class GrantRolePermissionRequest {
    public UUID roleId;
    public UUID resourceId;
    public UUID permissionId;
    public UUID grantedBy;
    public UUID tenantId;
    public LocalDateTime expiresAt;
}

class RevokePermissionRequest {
    public UUID userId;
    public UUID resourceId;
    public UUID permissionId;
    public UUID revokedBy;
    public String reason;
    public UUID tenantId;
}

class ShareResourceRequest {
    public UUID resourceId;
    public UUID targetUserId;
    public UUID roleId;
    public UUID sharedBy;
    public UUID tenantId;
}

class PublishResourceRequest {
    public UUID resourceId;
    public List<String> permissionNames;
    public UUID publishedBy;
    public UUID tenantId;
}

class UnpublishResourceRequest {
    public UUID resourceId;
    public UUID unpublishedBy;
    public UUID tenantId;
}

class OperationResult {
    public boolean success;
    public String message;
    
    public OperationResult() {}
    
    public OperationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}

class ErrorResponse {
    public String error;
    
    public ErrorResponse() {}
    
    public ErrorResponse(String error) {
        this.error = error;
    }
}
