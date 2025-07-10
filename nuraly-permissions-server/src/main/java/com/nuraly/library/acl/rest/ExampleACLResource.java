package com.nuraly.library.acl.rest;

import com.nuraly.library.acl.dto.*;
import com.nuraly.library.acl.model.*;
import com.nuraly.library.acl.service.ACLService;
import com.nuraly.library.permissions.client.RequiresPermission;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Advanced example resource demonstrating PowerApps-like ACL features
 * Shows various sharing, delegation, and anonymous access scenarios
 */
@Path("/api/v1/examples")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExampleACLResource {
    
    @Inject
    ACLService aclService;
    
    /**
     * Example 1: Share a resource publicly with read-only access
     * POST /api/v1/examples/share-public-readonly
     */
    @POST
    @Path("/share-public-readonly")
    @RequiresPermission(permissionType = "publish", resourceType = "document", resourceId = "#{resourceId}")
    public Response sharePublicReadOnly(SharePublicRequest request) {
        try {
            // Example: Share a document publicly with read-only permissions
            List<String> readOnlyPermissions = Arrays.asList("read");
            
            aclService.publishResource(
                request.resourceId,
                readOnlyPermissions,
                request.publishedBy,
                request.tenantId
            );
            
            // Get the generated public token
            Resource resource = Resource.findById(request.resourceId);
            
            return Response.ok(new PublicShareResponse(
                resource.publicLinkToken,
                "Resource shared publicly with read-only access",
                readOnlyPermissions
            )).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("Failed to share resource publicly: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Example 2: Assign a user as "Editor" to a document at the tenant level
     * POST /api/v1/examples/assign-editor-role
     */
    @POST
    @Path("/assign-editor-role")
    @RequiresPermission(permissionType = "admin", resourceType = "tenant", resourceId = "#{tenantId}")
    public Response assignEditorRole(AssignEditorRequest request) {
        try {
            // Find or create "Editor" role
            Role editorRole = Role.findByName("Editor");
            if (editorRole == null) {
                // Create editor role with comprehensive permissions
                editorRole = createEditorRole(request.tenantId);
            }
            
            // Grant all editor permissions to the user for all tenant resources
            List<Resource> tenantResources = Resource.findByTenant(request.tenantId);
            int grantsCreated = 0;
            
            for (Resource resource : tenantResources) {
                if (resource.resourceType.equals("document")) {
                    List<ResourceGrant> grants = aclService.shareResource(
                        resource.id,
                        request.userId,
                        editorRole.id,
                        request.assignedBy,
                        request.tenantId
                    );
                    grantsCreated += grants.size();
                }
            }
            
            return Response.ok(new AssignRoleResponse(
                "User assigned as Editor for tenant",
                grantsCreated,
                editorRole.name
            )).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("Failed to assign editor role: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Example 3: Revoke anonymous access for a resource
     * POST /api/v1/examples/revoke-anonymous-access
     */
    @POST
    @Path("/revoke-anonymous-access")
    @RequiresPermission(permissionType = "publish", resourceType = "resource", resourceId = "#{resourceId}")
    public Response revokeAnonymousAccess(RevokeAnonymousRequest request) {
        try {
            aclService.unpublishResource(
                request.resourceId,
                request.revokedBy,
                request.tenantId
            );
            
            return Response.ok(new OperationResult(
                true,
                "Anonymous access revoked successfully"
            )).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("Failed to revoke anonymous access: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Example 4: Create a collaborative workspace with team roles
     * POST /api/v1/examples/create-collaborative-workspace
     */
    @POST
    @Path("/create-collaborative-workspace")
    @RequiresPermission(permissionType = "admin", resourceType = "tenant", resourceId = "#{tenantId}")
    public Response createCollaborativeWorkspace(CreateWorkspaceRequest request) {
        try {
            // Create workspace resource
            Resource workspace = new Resource();
            workspace.name = request.workspaceName;
            workspace.description = "Collaborative workspace with team-based permissions";
            workspace.resourceType = "workspace";
            workspace.externalTenantId = request.tenantId;
            workspace.ownerId = request.createdBy;
            workspace.persist();
            
            // Create team roles with different permission levels
            Role viewerRole = createViewerRole(request.tenantId);
            Role collaboratorRole = createCollaboratorRole(request.tenantId);
            Role moderatorRole = createModeratorRole(request.tenantId);
            
            // Apply sharing policies
            SharePolicy teamPolicy = new SharePolicy();
            teamPolicy.name = "Team Collaboration - " + workspace.name;
            teamPolicy.description = "Team members can collaborate on this workspace";
            teamPolicy.tenantId = request.tenantId;
            teamPolicy.policyType = SharePolicyType.TENANT;
            teamPolicy.accessLevel = AccessLevel.FULL_ACCESS;
            teamPolicy.permissions = "[\"read\", \"write\", \"share\", \"annotate\", \"moderate\"]";
            teamPolicy.requireAuthentication = true;
            teamPolicy.allowAnonymous = false;
            teamPolicy.createdBy = request.createdBy;
            teamPolicy.persist();
            
            return Response.ok(new WorkspaceCreatedResponse(
                workspace.id,
                workspace.name,
                Arrays.asList(viewerRole.name, collaboratorRole.name, moderatorRole.name),
                teamPolicy.name
            )).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("Failed to create collaborative workspace: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Example 5: Generate and manage public links with expiration
     * POST /api/v1/examples/generate-public-link
     */
    @POST
    @Path("/generate-public-link")
    @RequiresPermission(permissionType = "share", resourceType = "resource", resourceId = "#{resourceId}")
    public Response generatePublicLink(GeneratePublicLinkRequest request) {
        try {
            Resource resource = Resource.findById(request.resourceId);
            if (resource == null) {
                throw new IllegalArgumentException("Resource not found");
            }
            
            // Set expiration if specified
            if (request.expiresInHours != null && request.expiresInHours > 0) {
                resource.publicLinkExpiresAt = java.time.LocalDateTime.now().plusHours(request.expiresInHours);
            }
            
            // Configure permissions based on access level
            List<String> permissions;
            switch (request.accessLevel.toUpperCase()) {
                case "READ_ONLY":
                    permissions = Arrays.asList("read");
                    break;
                case "COMMENT":
                    permissions = Arrays.asList("read", "annotate");
                    break;
                case "EDIT":
                    permissions = Arrays.asList("read", "write", "annotate");
                    break;
                default:
                    permissions = Arrays.asList("read");
            }
            
            aclService.publishResource(
                request.resourceId,
                permissions,
                request.generatedBy,
                request.tenantId
            );
            
            // Refresh resource to get the generated token
            resource = Resource.findById(request.resourceId);
            
            return Response.ok(new PublicLinkResponse(
                resource.publicLinkToken,
                resource.publicLinkExpiresAt,
                permissions,
                "Public link generated successfully"
            )).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("Failed to generate public link: " + e.getMessage()))
                .build();
        }
    }
    
    // Helper methods to create predefined roles
    
    private Role createEditorRole(UUID tenantId) {
        Role role = new Role();
        role.name = "Editor";
        role.description = "Can read, write, and share documents";
        role.externalTenantId = tenantId;
        role.scope = RoleScope.TENANT;
        role.isSystemRole = true;
        role.persist();
        
        // Add permissions
        addPermissionToRole(role, "read");
        addPermissionToRole(role, "write");
        addPermissionToRole(role, "share");
        addPermissionToRole(role, "annotate");
        
        return role;
    }
    
    private Role createViewerRole(UUID tenantId) {
        Role role = new Role();
        role.name = "Viewer";
        role.description = "Can only read content";
        role.externalTenantId = tenantId;
        role.scope = RoleScope.RESOURCE;
        role.isSystemRole = true;
        role.persist();
        
        addPermissionToRole(role, "read");
        
        return role;
    }
    
    private Role createCollaboratorRole(UUID tenantId) {
        Role role = new Role();
        role.name = "Collaborator";
        role.description = "Can read, write, and annotate";
        role.externalTenantId = tenantId;
        role.scope = RoleScope.RESOURCE;
        role.isSystemRole = true;
        role.persist();
        
        addPermissionToRole(role, "read");
        addPermissionToRole(role, "write");
        addPermissionToRole(role, "annotate");
        
        return role;
    }
    
    private Role createModeratorRole(UUID tenantId) {
        Role role = new Role();
        role.name = "Moderator";
        role.description = "Can moderate content and manage permissions";
        role.externalTenantId = tenantId;
        role.scope = RoleScope.RESOURCE;
        role.isSystemRole = true;
        role.persist();
        
        addPermissionToRole(role, "read");
        addPermissionToRole(role, "write");
        addPermissionToRole(role, "annotate");
        addPermissionToRole(role, "moderate");
        addPermissionToRole(role, "share");
        
        return role;
    }
    
    private void addPermissionToRole(Role role, String permissionName) {
        Permission permission = Permission.findByName(permissionName);
        if (permission == null) {
            permission = new Permission();
            permission.name = permissionName;
            permission.description = "Permission to " + permissionName;
            permission.isSystemPermission = true;
            permission.persist();
        }
        
        if (role.permissions == null) {
            role.permissions = new java.util.HashSet<>();
        }
        role.permissions.add(permission);
        role.persist();
    }
}

// Request/Response DTOs for examples

class SharePublicRequest {
    public String resourceId;
    public UUID publishedBy;
    public UUID tenantId;
}

class PublicShareResponse {
    public String publicToken;
    public String message;
    public List<String> permissions;
    
    public PublicShareResponse(String publicToken, String message, List<String> permissions) {
        this.publicToken = publicToken;
        this.message = message;
        this.permissions = permissions;
    }
}

class AssignEditorRequest {
    public UUID userId;
    public UUID tenantId;
    public UUID assignedBy;
}

class AssignRoleResponse {
    public String message;
    public int grantsCreated;
    public String roleName;
    
    public AssignRoleResponse(String message, int grantsCreated, String roleName) {
        this.message = message;
        this.grantsCreated = grantsCreated;
        this.roleName = roleName;
    }
}

class RevokeAnonymousRequest {
    public String resourceId;
    public UUID revokedBy;
    public UUID tenantId;
}

class CreateWorkspaceRequest {
    public String workspaceName;
    public UUID tenantId;
    public UUID createdBy;
}

class WorkspaceCreatedResponse {
    public String workspaceId;
    public String workspaceName;
    public List<String> availableRoles;
    public String sharePolicy;
    
    public WorkspaceCreatedResponse(String workspaceId, String workspaceName, List<String> availableRoles, String sharePolicy) {
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
        this.availableRoles = availableRoles;
        this.sharePolicy = sharePolicy;
    }
}

class GeneratePublicLinkRequest {
    public String resourceId;
    public UUID generatedBy;
    public UUID tenantId;
    public String accessLevel = "READ_ONLY"; // READ_ONLY, COMMENT, EDIT
    public Integer expiresInHours;
}

class PublicLinkResponse {
    public String publicToken;
    public java.time.LocalDateTime expiresAt;
    public List<String> permissions;
    public String message;
    
    public PublicLinkResponse(String publicToken, java.time.LocalDateTime expiresAt, List<String> permissions, String message) {
        this.publicToken = publicToken;
        this.expiresAt = expiresAt;
        this.permissions = permissions;
        this.message = message;
    }
}
