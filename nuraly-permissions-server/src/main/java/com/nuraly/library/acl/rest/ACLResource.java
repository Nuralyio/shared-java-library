package com.nuraly.library.acl.rest;

import com.nuraly.library.acl.model.*;
import com.nuraly.library.acl.service.ACLService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
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
@Tag(name = "ACL Management", description = "Access Control List operations for permissions, roles, and resource sharing")
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
     * Validates that a user is the owner of a resource or has the required permission
     * @param userId The user ID
     * @param resourceId The resource ID
     * @param permissionName The required permission name (if not owner)
     * @param tenantId The tenant ID
     * @param skipIfNullTenant Whether to skip check if tenantId is null
     * @return Response with error if not authorized, null if authorized
     */
    private Response validateOwnershipOrPermission(UUID userId, UUID resourceId, String permissionName, 
                                                 UUID tenantId, boolean skipIfNullTenant) {
        // Skip authorization check if tenantId is null and skipIfNullTenant is true
        if (skipIfNullTenant && tenantId == null) {
            return null;
        }
        
        // Check if user is the owner of the resource
        Resource resource = Resource.findById(resourceId);
        if (resource != null && userId.equals(resource.ownerId)) {
            return null; // Owner has full access
        }
        
        // If not owner, check for specific permission
        boolean hasPermission = aclService.hasPermission(userId, resourceId, permissionName, tenantId);
        
        if (!hasPermission) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(new ErrorResponse("Access denied: must be owner or have " + permissionName + " permission"))
                .build();
        }
        return null;
    }
    
    /**
     * Validates that a user is the owner of a resource or has the required permission (no tenant skip option)
     */
    private Response validateOwnershipOrPermission(UUID userId, UUID resourceId, String permissionName, UUID tenantId) {
        return validateOwnershipOrPermission(userId, resourceId, permissionName, tenantId, false);
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
    @Operation(
        summary = "Check user permission",
        description = "Verify if a user has a specific permission on a resource"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Permission check completed",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = PermissionCheckResponse.class)
            )
        )
    })
    public Response checkPermission(
        @Parameter(description = "Permission check request", required = true)
        PermissionCheckRequest request) {
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
    @Operation(
        summary = "Check anonymous access",
        description = "Verify if anonymous access is allowed for a specific permission on a resource"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Anonymous permission check completed",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = PermissionCheckResponse.class)
            )
        )
    })
    public Response checkAnonymousPermission(
        @Parameter(description = "Anonymous permission check request", required = true)
        AnonymousPermissionCheckRequest request) {
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
    @Operation(
        summary = "Grant permission",
        description = "Grant a specific permission to a user on a resource"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Permission granted successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ResourceGrant.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - resource not found or invalid parameters"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions to grant access"
        )
    })
    public Response grantPermission(
        @Parameter(description = "Grant permission request", required = true)
        GrantPermissionRequest request) {
        try {
            // Validate resource exists
            Response validationError = validateResourceExists(request.resourceId);
            if (validationError != null) return validationError;
            
            // Check authorization (must be owner or have admin permission)
            // Skip if tenantId is null for backwards compatibility
            Response authError = validateOwnershipOrPermission(request.grantedBy, request.resourceId, 
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
    @Operation(
        summary = "Grant role-based permission",
        description = "Grant a permission to a role on a resource"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Permission granted successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ResourceGrant.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - resource not found or invalid parameters"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions to grant access"
        )
    })
    public Response grantRolePermission(
        @Parameter(description = "Grant role permission request", required = true)
        GrantRolePermissionRequest request) {
        try {
            // Validate resource exists
            Response validationError = validateResourceExists(request.resourceId);
            if (validationError != null) return validationError;
            
            // Check authorization (must be owner or have admin permission)
            Response authError = validateOwnershipOrPermission(request.grantedBy, request.resourceId, 
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
    @Operation(
        summary = "Revoke permission",
        description = "Revoke a specific permission from a user on a resource"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Permission revoke operation completed",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = OperationResult.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - resource not found or invalid parameters"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions to revoke access"
        )
    })
    public Response revokePermission(
        @Parameter(description = "Revoke permission request", required = true)
        RevokePermissionRequest request) {
        try {
            // Validate resource exists
            Response validationError = validateResourceExists(request.resourceId);
            if (validationError != null) return validationError;
            
            // Check authorization (must be owner or have admin permission)
            Response authError = validateOwnershipOrPermission(request.revokedBy, request.resourceId, 
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
    @Operation(
        summary = "Share resource",
        description = "Share a resource with another user by granting them a specific role"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Resource shared successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ResourceGrant.class, type = SchemaType.ARRAY)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - resource not found or invalid parameters"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions to share resource"
        )
    })
    public Response shareResource(
        @Parameter(description = "Share resource request", required = true)
        ShareResourceRequest request) {
        try {
            // Validate resource exists
            Response validationError = validateResourceExists(request.resourceId);
            if (validationError != null) return validationError;
            
            // Check authorization (must be owner or have share permission)
            Response authError = validateOwnershipOrPermission(request.sharedBy, request.resourceId, 
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
    @Operation(
        summary = "Publish resource",
        description = "Make a resource publicly accessible with specified permissions"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Resource published successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = OperationResult.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - resource not found or invalid parameters"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions to publish resource"
        )
    })
    public Response publishResource(
        @Parameter(description = "Publish resource request", required = true)
        PublishResourceRequest request) {
        try {
            // Validate resource exists
            Response validationError = validateResourceExists(request.resourceId);
            if (validationError != null) return validationError;
            
            // Check authorization (must be owner or have publish permission)
            Response authError = validateOwnershipOrPermission(request.publishedBy, request.resourceId, 
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
    @Operation(
        summary = "Unpublish resource",
        description = "Remove public access from a resource"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Resource unpublished successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = OperationResult.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - resource not found or invalid parameters"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions to unpublish resource"
        )
    })
    public Response unpublishResource(
        @Parameter(description = "Unpublish resource request", required = true)
        UnpublishResourceRequest request) {
        try {
            // Validate resource exists
            Response validationError = validateResourceExists(request.resourceId);
            if (validationError != null) return validationError;
            
            // Check authorization (must be owner or have publish permission)
            Response authError = validateOwnershipOrPermission(request.unpublishedBy, request.resourceId, 
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
    @Operation(
        summary = "Get accessible resources",
        description = "Retrieve all resources that a user has access to within a tenant"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Accessible resources retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = Resource.class, type = SchemaType.ARRAY)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - invalid parameters"
        )
    })
    public Response getAccessibleResources(
        @Parameter(description = "User ID", required = true)
        @PathParam("userId") UUID userId, 
        @Parameter(description = "Tenant ID for filtering resources")
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
    @Operation(
        summary = "Get public resources",
        description = "Retrieve all resources that are publicly accessible"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Public resources retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = Resource.class, type = SchemaType.ARRAY)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - server error"
        )
    })
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
    @Operation(
        summary = "Validate public link",
        description = "Validate if a public link token is valid for accessing a resource with specific permissions"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Public link validation completed",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = OperationResult.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - invalid token or parameters"
        )
    })
    public Response validatePublicLink(
        @Parameter(description = "Public link token", required = true)
        @PathParam("token") String token,
        @Parameter(description = "Permission name to validate")
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
    @Operation(
        summary = "Get public resource",
        description = "Retrieve a resource using a public access token for anonymous access"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Public resource retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = Resource.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Public resource not found or expired",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - invalid token"
        )
    })
    public Response getPublicResource(
        @Parameter(description = "Public access token", required = true)
        @PathParam("token") String token) {
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

@Schema(description = "Request to check if a user has a specific permission on a resource")
class PermissionCheckRequest {
    @Schema(description = "User ID to check permission for", required = true)
    public UUID userId;
    
    @Schema(description = "Resource ID to check permission on", required = true)
    public UUID resourceId;
    
    @Schema(description = "Permission name to check", required = true, example = "read")
    public String permissionName;
    
    @Schema(description = "Tenant ID for tenant-scoped permissions")
    public UUID tenantId;
}

@Schema(description = "Request to check anonymous access to a resource")
class AnonymousPermissionCheckRequest {
    @Schema(description = "Resource ID to check permission on", required = true)
    public UUID resourceId;
    
    @Schema(description = "Permission name to check", required = true, example = "read")
    public String permissionName;
    
    @Schema(description = "Tenant ID for tenant-scoped permissions")
    public UUID tenantId;
}

@Schema(description = "Response containing permission check result")
class PermissionCheckResponse {
    @Schema(description = "Whether the user has the requested permission")
    public boolean hasPermission;
    
    @Schema(description = "Error message if permission check failed")
    public String errorMessage;
    
    public PermissionCheckResponse() {}
    
    public PermissionCheckResponse(boolean hasPermission, String errorMessage) {
        this.hasPermission = hasPermission;
        this.errorMessage = errorMessage;
    }
}

@Schema(description = "Request to grant a permission to a user on a resource")
class GrantPermissionRequest {
    @Schema(description = "User ID to grant permission to", required = true)
    public UUID userId;
    
    @Schema(description = "Resource ID to grant permission on", required = true)
    public UUID resourceId;
    
    @Schema(description = "Permission ID to grant", required = true)
    public UUID permissionId;
    
    @Schema(description = "User ID who is granting the permission", required = true)
    public UUID grantedBy;
    
    @Schema(description = "Tenant ID for tenant-scoped permissions")
    public UUID tenantId;
    
    @Schema(description = "Optional expiration date for the permission")
    public LocalDateTime expiresAt;
}

@Schema(description = "Request to grant a permission to a role on a resource")
class GrantRolePermissionRequest {
    @Schema(description = "Role ID to grant permission to", required = true)
    public UUID roleId;
    
    @Schema(description = "Resource ID to grant permission on", required = true)
    public UUID resourceId;
    
    @Schema(description = "Permission ID to grant", required = true)
    public UUID permissionId;
    
    @Schema(description = "User ID who is granting the permission", required = true)
    public UUID grantedBy;
    
    @Schema(description = "Tenant ID for tenant-scoped permissions")
    public UUID tenantId;
    
    @Schema(description = "Optional expiration date for the permission")
    public LocalDateTime expiresAt;
}

@Schema(description = "Request to revoke a permission from a user on a resource")
class RevokePermissionRequest {
    @Schema(description = "User ID to revoke permission from", required = true)
    public UUID userId;
    
    @Schema(description = "Resource ID to revoke permission on", required = true)
    public UUID resourceId;
    
    @Schema(description = "Permission ID to revoke", required = true)
    public UUID permissionId;
    
    @Schema(description = "User ID who is revoking the permission", required = true)
    public UUID revokedBy;
    
    @Schema(description = "Reason for revoking the permission")
    public String reason;
    
    @Schema(description = "Tenant ID for tenant-scoped permissions")
    public UUID tenantId;
}

@Schema(description = "Request to share a resource with another user")
class ShareResourceRequest {
    @Schema(description = "Resource ID to share", required = true)
    public UUID resourceId;
    
    @Schema(description = "User ID to share the resource with", required = true)
    public UUID targetUserId;
    
    @Schema(description = "Role ID to assign to the user", required = true)
    public UUID roleId;
    
    @Schema(description = "User ID who is sharing the resource", required = true)
    public UUID sharedBy;
    
    @Schema(description = "Tenant ID for tenant-scoped permissions")
    public UUID tenantId;
}

@Schema(description = "Request to publish a resource for public access")
class PublishResourceRequest {
    @Schema(description = "Resource ID to publish", required = true)
    public UUID resourceId;
    
    @Schema(description = "List of permission names to allow for public access", required = true)
    public List<String> permissionNames;
    
    @Schema(description = "User ID who is publishing the resource", required = true)
    public UUID publishedBy;
    
    @Schema(description = "Tenant ID for tenant-scoped permissions")
    public UUID tenantId;
}

@Schema(description = "Request to unpublish a resource (remove public access)")
class UnpublishResourceRequest {
    @Schema(description = "Resource ID to unpublish", required = true)
    public UUID resourceId;
    
    @Schema(description = "User ID who is unpublishing the resource", required = true)
    public UUID unpublishedBy;
    
    @Schema(description = "Tenant ID for tenant-scoped permissions")
    public UUID tenantId;
}

@Schema(description = "Generic operation result")
class OperationResult {
    @Schema(description = "Whether the operation was successful")
    public boolean success;
    
    @Schema(description = "Message describing the operation result")
    public String message;
    
    public OperationResult() {}
    
    public OperationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}

@Schema(description = "Error response")
class ErrorResponse {
    @Schema(description = "Error message")
    public String error;
    
    public ErrorResponse() {}
    
    public ErrorResponse(String error) {
        this.error = error;
    }
}
