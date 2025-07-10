package com.nuraly.library.acl.rest;

import com.nuraly.library.acl.dto.*;
import com.nuraly.library.acl.model.*;
import com.nuraly.library.acl.service.*;
import com.nuraly.library.acl.util.ResponseUtil;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
import org.jboss.logging.Logger;
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
    
    private static final Logger LOG = Logger.getLogger(ACLResource.class);
    
    @Inject
    ACLService aclService;
    
    @Inject
    UserContextService userContextService;
    
    @Inject
    ACLValidationService validationService;
    
    @Inject
    ResourceManagementService resourceService;
    
    @Inject
    PermissionManagementService permissionService;
    
    @Inject
    PublicResourceService publicResourceService;

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
            
            PermissionCheckResponse response = new PermissionCheckResponse(hasPermission, null);
            
            return Response.ok(response).build();
        } catch (Exception e) {
            LOG.errorf("Exception in checkPermission: %s - %s", e.getClass().getSimpleName(), e.getMessage());
            
            PermissionCheckResponse errorResponse = new PermissionCheckResponse(false, e.getMessage());
            
            return Response.ok(errorResponse).build();
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
            
            PermissionCheckResponse response = new PermissionCheckResponse(hasPermission, null);
            
            return Response.ok(response).build();
        } catch (Exception e) {
            LOG.errorf("Exception in checkAnonymousPermission: %s - %s", e.getClass().getSimpleName(), e.getMessage());
            
            PermissionCheckResponse errorResponse = new PermissionCheckResponse(false, e.getMessage());
            
            return Response.ok(errorResponse).build();
        }
    }
    
    /**
     * Grant permission to a user on a resource (legacy endpoint)
     * POST /api/v1/acl/grant-permission
     */
    @POST
    @Path("/grant-permission")
    @Operation(
        summary = "Grant permission (legacy)",
        description = "Grant a specific permission to a user on a resource - legacy endpoint with explicit user IDs"
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
        
        return ResponseUtil.executeWithValidation(
            () -> {
                UUID currentUserId = userContextService.getCurrentUserId();
                UUID currentTenantId = userContextService.getCurrentTenantId();
                ResourceGrant grant = aclService.grantPermission(
                    request.userId,
                    request.resourceId,
                    request.permissionId,
                    currentUserId,
                    currentTenantId
                );
                return Response.ok(grant).build();
            },
            () -> validationService.validateAuthentication(),
            () -> validationService.validateResourceExists(request.resourceId),
            () -> validationService.validateCurrentUserOwnershipOrPermission(request.resourceId, "admin")
        );
    }
    
    /**
     * Grant permission to a user on a resource (simplified)
     * POST /api/v1/acl/grant
     */
    @POST
    @Path("/grant")
    @Operation(
        summary = "Grant permission",
        description = "Grant a specific permission to a user on a resource using current user context"
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
            responseCode = "401",
            description = "Unauthorized - authentication required"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions to grant access"
        )
    })
    public Response grantPermissionSimple(
        @Parameter(description = "Grant permission request", required = true)
        SimpleGrantPermissionRequest request) {
        
        return ResponseUtil.executeWithValidation(
            () -> {
                UUID currentUserId = userContextService.getCurrentUserId();
                UUID currentTenantId = userContextService.getCurrentTenantId();
                ResourceGrant grant = permissionService.grantPermission(request, currentUserId, currentTenantId);
                return Response.ok(grant).build();
            },
            () -> validationService.validateAuthentication(),
            () -> validationService.validateResourceExists(request.resourceId),
            () -> validationService.validateCurrentUserOwnershipOrPermission(request.resourceId, "admin")
        );
    }
    
    /**
     * Grant role-based permission on a resource
     * POST /api/v1/acl/grant-role-permission
     */
    @POST
    @Path("/grant-role-permission")
    @Operation(
        summary = "Grant role-based permission",
        description = "Grant a permission to a role on a resource using current user context"
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
            responseCode = "401",
            description = "Unauthorized - authentication required"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions to grant access"
        )
    })
    public Response grantRolePermission(
        @Parameter(description = "Grant role permission request", required = true)
        GrantRolePermissionRequest request) {
        
        return ResponseUtil.executeWithValidation(
            () -> {
                UUID currentUserId = userContextService.getCurrentUserId();
                UUID currentTenantId = userContextService.getCurrentTenantId();
                ResourceGrant grant = permissionService.grantRolePermission(request, currentUserId, currentTenantId);
                return Response.ok(grant).build();
            },
            () -> validationService.validateAuthentication(),
            () -> validationService.validateResourceExists(request.resourceId),
            () -> validationService.validateCurrentUserOwnershipOrPermission(request.resourceId, "admin")
        );
    }
    
    /**
     * Revoke permission from a user on a resource (legacy)
     * POST /api/v1/acl/revoke-permission
     */
    @POST
    @Path("/revoke-permission")
    @Operation(
        summary = "Revoke permission (legacy)",
        description = "Revoke a specific permission from a user on a resource - legacy endpoint"
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
            responseCode = "401",
            description = "Unauthorized - authentication required"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions to revoke access"
        )
    })
    public Response revokePermission(
        @Parameter(description = "Revoke permission request", required = true)
        RevokePermissionRequest request) {
        
        return ResponseUtil.executeWithValidation(
            () -> {
                UUID currentUserId = userContextService.getCurrentUserId();
                UUID currentTenantId = userContextService.getCurrentTenantId();
                boolean revoked = aclService.revokePermission(
                    request.userId,
                    request.resourceId,
                    request.permissionId,
                    currentUserId,
                    request.reason,
                    currentTenantId
                );
                return Response.ok(new OperationResult(revoked, revoked ? "Permission revoked" : "Permission not found")).build();
            },
            () -> validationService.validateAuthentication(),
            () -> validationService.validateResourceExists(request.resourceId),
            () -> validationService.validateCurrentUserOwnershipOrPermission(request.resourceId, "admin")
        );
    }
    
    /**
     * Revoke permission from a user on a resource (simplified)
     * POST /api/v1/acl/revoke
     */
    @POST
    @Path("/revoke")
    @Operation(
        summary = "Revoke permission",
        description = "Revoke a specific permission from a user on a resource using current user context"
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
            responseCode = "401",
            description = "Unauthorized - authentication required"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions to revoke access"
        )
    })
    public Response revokePermissionSimple(
        @Parameter(description = "Revoke permission request", required = true)
        SimpleRevokePermissionRequest request) {
        
        return ResponseUtil.executeWithValidation(
            () -> {
                UUID currentUserId = userContextService.getCurrentUserId();
                UUID currentTenantId = userContextService.getCurrentTenantId();
                boolean revoked = permissionService.revokePermission(request, currentUserId, currentTenantId);
                return Response.ok(new OperationResult(revoked, revoked ? "Permission revoked" : "Permission not found")).build();
            },
            () -> validationService.validateAuthentication(),
            () -> validationService.validateResourceExists(request.resourceId),
            () -> validationService.validateCurrentUserOwnershipOrPermission(request.resourceId, "admin")
        );
    }
    
    /**
     * Share a resource with another user
     * POST /api/v1/acl/share-resource
     */
    @POST
    @Path("/share-resource")
    @Operation(
        summary = "Share resource",
        description = "Share a resource with another user by granting them a specific role using current user context"
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
            responseCode = "401",
            description = "Unauthorized - authentication required"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions to share resource"
        )
    })
    public Response shareResource(
        @Parameter(description = "Share resource request", required = true)
        ShareResourceRequest request) {
        
        return ResponseUtil.executeWithValidation(
            () -> {
                UUID currentUserId = userContextService.getCurrentUserId();
                UUID currentTenantId = userContextService.getCurrentTenantId();
                List<ResourceGrant> grants = aclService.shareResource(
                    request.resourceId,
                    request.targetUserId,
                    request.roleId,
                    currentUserId,
                    currentTenantId
                );
                return Response.ok(grants).build();
            },
            () -> validationService.validateAuthentication(),
            () -> validationService.validateResourceExists(request.resourceId),
            () -> validationService.validateCurrentUserOwnershipOrPermission(request.resourceId, "share")
        );
    }
    
    /**
     * Publish a resource for public access
     * POST /api/v1/acl/publish-resource
     */
    @POST
    @Path("/publish-resource")
    @Operation(
        summary = "Publish resource",
        description = "Make a resource publicly accessible with specified permissions using current user context"
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
            responseCode = "401",
            description = "Unauthorized - authentication required"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions to publish resource"
        )
    })
    public Response publishResource(
        @Parameter(description = "Publish resource request", required = true)
        PublishResourceRequest request) {
        
        return ResponseUtil.executeWithValidation(
            () -> {
                UUID currentUserId = userContextService.getCurrentUserId();
                UUID currentTenantId = userContextService.getCurrentTenantId();
                publicResourceService.publishResource(request, currentUserId, currentTenantId);
                return Response.ok(new OperationResult(true, "Resource published successfully")).build();
            },
            () -> validationService.validateAuthentication(),
            () -> validationService.validateResourceExists(request.resourceId),
            () -> validationService.validateCurrentUserOwnershipOrPermission(request.resourceId, "publish")
        );
    }
    
    /**
     * Unpublish a resource (remove public access)
     * POST /api/v1/acl/unpublish-resource
     */
    @POST
    @Path("/unpublish-resource")
    @Operation(
        summary = "Unpublish resource",
        description = "Remove public access from a resource using current user context"
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
            responseCode = "401",
            description = "Unauthorized - authentication required"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions to unpublish resource"
        )
    })
    public Response unpublishResource(
        @Parameter(description = "Unpublish resource request", required = true)
        UnpublishResourceRequest request) {
        
        return ResponseUtil.executeWithValidation(
            () -> {
                UUID currentUserId = userContextService.getCurrentUserId();
                UUID currentTenantId = userContextService.getCurrentTenantId();
                publicResourceService.unpublishResource(request, currentUserId, currentTenantId);
                return Response.ok(new OperationResult(true, "Resource unpublished successfully")).build();
            },
            () -> validationService.validateAuthentication(),
            () -> validationService.validateResourceExists(request.resourceId),
            () -> validationService.validateCurrentUserOwnershipOrPermission(request.resourceId, "publish")
        );
    }
    
    /**
     * Get all resources accessible by the current authenticated user
     * GET /api/v1/acl/accessible-resources
     */
    @GET
    @Path("/accessible-resources")
    @Operation(
        summary = "Get accessible resources",
        description = "Retrieve all resources that the current authenticated user has access to within a tenant, optionally filtered by resource type and permission"
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
        ),
        @APIResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required"
        )
    })
    public Response getAccessibleResources(
        @Parameter(description = "Resource type to filter by (optional)")
        @QueryParam("resourceType") String resourceType,
        @Parameter(description = "Permission name to filter by (optional)")
        @QueryParam("permission") String permissionName) {
        
        return ResponseUtil.executeWithValidation(
            () -> {
                UUID currentUserId = userContextService.getCurrentUserId();
                UUID currentTenantId = userContextService.getCurrentTenantId();
                List<Resource> resources = aclService.getAccessibleResources(currentUserId, currentTenantId, resourceType, permissionName);
                return Response.ok(resources).build();
            },
            () -> validationService.validateAuthentication()
        );
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
    
    // ============================================
    // RESOURCE MANAGEMENT ENDPOINTS
    // ============================================
    
    /**
     * Register a new resource with the ACL system
     * POST /api/v1/acl/resources
     */
    @POST
    @Path("/resources")
    @Transactional
    @Operation(
        summary = "Register resource",
        description = "Register a new resource with the ACL system for permission management"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Resource registered successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = Resource.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - invalid resource data"
        ),
        @APIResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required"
        )
    })
    public Response registerResource(
        @Parameter(description = "Resource registration request", required = true)
        @Valid RegisterResourceRequest request) {
        
        return ResponseUtil.executeWithValidation(
            () -> {
                UUID currentUserId = userContextService.getCurrentUserId();
                UUID currentTenantId = userContextService.getCurrentTenantId();
                Resource resource = resourceService.registerResource(request, currentUserId, currentTenantId);
                return Response.status(Response.Status.CREATED).entity(resource).build();
            },
            () -> validationService.validateAuthentication()
        );
    }
    
    /**
     * Transfer ownership of a resource
     * PUT /api/v1/acl/resources/{resourceId}/owner
     */
    @PUT
    @Path("/resources/{resourceId}/owner")
    @Transactional
    @Operation(
        summary = "Transfer resource ownership",
        description = "Transfer ownership of a resource to another user"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Ownership transferred successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = Resource.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - resource not found or invalid parameters"
        ),
        @APIResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Forbidden - only resource owner can transfer ownership"
        )
    })
    public Response transferOwnership(
        @Parameter(description = "Resource ID", required = true)
        @PathParam("resourceId") String resourceId,
        @Parameter(description = "Transfer ownership request", required = true)
        TransferOwnershipRequest request) {
        
        return ResponseUtil.executeWithValidation(
            () -> {
                UUID currentUserId = userContextService.getCurrentUserId();
                UUID currentTenantId = userContextService.getCurrentTenantId();
                Resource resource = resourceService.transferOwnership(resourceId, request.newOwnerId, currentUserId, currentTenantId);
                return Response.ok(resource).build();
            },
            () -> validationService.validateAuthentication(),
            () -> validationService.validateResourceExists(resourceId),
            () -> validationService.validateCurrentUserIsOwner(resourceId, "Only resource owner can transfer ownership"),
            () -> validationService.validateResourceInCurrentTenant(resourceId)
        );
    }
    
    /**
     * Update resource metadata
     * PUT /api/v1/acl/resources/{resourceId}
     */
    @PUT
    @Path("/resources/{resourceId}")
    @Transactional
    @Operation(
        summary = "Update resource",
        description = "Update resource metadata (name, description, etc.)"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Resource updated successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = Resource.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - resource not found or invalid parameters"
        ),
        @APIResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions to update resource"
        )
    })
    public Response updateResource(
        @Parameter(description = "Resource ID", required = true)
        @PathParam("resourceId") String resourceId,
        @Parameter(description = "Update resource request", required = true)
        UpdateResourceRequest request) {
        
        return ResponseUtil.executeWithValidation(
            () -> {
                Resource resource = resourceService.updateResource(resourceId, request);
                return Response.ok(resource).build();
            },
            () -> validationService.validateAuthentication(),
            () -> validationService.validateResourceExists(resourceId),
            () -> validationService.validateCurrentUserOwnershipOrPermission(resourceId, "admin")
        );
    }
    
    /**
     * Delete/deactivate a resource
     * DELETE /api/v1/acl/resources/{resourceId}
     */
    @DELETE
    @Path("/resources/{resourceId}")
    @Transactional
    @Operation(
        summary = "Delete resource",
        description = "Delete a resource and all its associated permissions"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Resource deleted successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = OperationResult.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - resource not found"
        ),
        @APIResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Forbidden - only resource owner can delete resource"
        )
    })
    public Response deleteResource(
        @Parameter(description = "Resource ID", required = true)
        @PathParam("resourceId") String resourceId) {
        
        return ResponseUtil.executeWithValidation(
            () -> {
                UUID currentUserId = userContextService.getCurrentUserId();
                resourceService.deleteResource(resourceId, currentUserId);
                return Response.ok(new OperationResult(true, "Resource deleted successfully")).build();
            },
            () -> validationService.validateAuthentication(),
            () -> validationService.validateResourceExists(resourceId),
            () -> validationService.validateCurrentUserIsOwner(resourceId, "Only resource owner can delete resource")
        );
    }
    
    /**
     * Set or update parent resource for hierarchical relationships
     * PUT /api/v1/acl/resources/{resourceId}/parent
     */
    @PUT
    @Path("/resources/{resourceId}/parent")
    @Transactional
    @Operation(
        summary = "Set parent resource",
        description = "Set or update parent resource for hierarchical relationships"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Parent resource updated successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = Resource.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - resource not found or invalid parent"
        ),
        @APIResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions to modify hierarchy"
        )
    })
    public Response setParentResource(
        @Parameter(description = "Resource ID", required = true)
        @PathParam("resourceId") String resourceId,
        @Parameter(description = "Set parent resource request", required = true)
        @Valid SetParentResourceRequest request) {
        
        return ResponseUtil.executeWithValidation(
            () -> {
                UUID currentUserId = userContextService.getCurrentUserId();
                UUID currentTenantId = userContextService.getCurrentTenantId();
                Resource resource = resourceService.setParentResource(resourceId, request, currentUserId, currentTenantId);
                return Response.ok(resource).build();
            },
            () -> validationService.validateAuthentication(),
            () -> validationService.validateResourceExists(resourceId),
            () -> validationService.validateCurrentUserOwnershipOrPermission(resourceId, "admin")
        );
    }
    
    /**
     * Get child resources for a given resource
     * GET /api/v1/acl/resources/{resourceId}/children
     */
    @GET
    @Path("/resources/{resourceId}/children")
    @Operation(
        summary = "Get child resources",
        description = "Get all child resources in the hierarchy for a given resource"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Child resources retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = Resource.class, type = SchemaType.ARRAY)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - resource not found"
        ),
        @APIResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required"
        )
    })
    public Response getResourceChildren(
        @Parameter(description = "Resource ID", required = true)
        @PathParam("resourceId") String resourceId) {
        
        return ResponseUtil.executeWithValidation(
            () -> {
                UUID currentTenantId = userContextService.getCurrentTenantId();
                List<Resource> children = resourceService.getResourceChildren(resourceId, currentTenantId);
                return Response.ok(children).build();
            },
            () -> validationService.validateAuthentication(),
            () -> validationService.validateResourceExists(resourceId),
            () -> validationService.validateResourceInCurrentTenant(resourceId)
        );
    }
}
