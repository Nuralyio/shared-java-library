package com.nuraly.library.acl.service;

import com.nuraly.library.acl.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core ACL service providing comprehensive access control functionality
 * Supports user-based, role-based, and anonymous access control
 */
@ApplicationScoped
public class ACLService {
    
    @Inject
    AuditService auditService;
    
    /**
     * Check if a user has a specific permission on a resource
     * This is the main access control entry point
     */
    public boolean hasPermission(UUID userId, UUID resourceId, String permissionName, UUID tenantId) {
        try {
            User user = User.findById(userId);
            Resource resource = Resource.findById(resourceId);
            Permission permission = Permission.findByName(permissionName);
            
            if (user == null || resource == null || permission == null) {
                auditService.logAccessAttempt(tenantId, userId, resourceId, 
                    permission != null ? permission.id : null, false, "User, resource, or permission not found");
                return false;
            }
            
            boolean hasAccess = checkUserPermission(user, resource, permission, tenantId);
            
            auditService.logAccessAttempt(tenantId, userId, resourceId, permission.id, hasAccess, 
                hasAccess ? null : "Permission denied");
            
            return hasAccess;
        } catch (Exception e) {
            auditService.logAccessAttempt(tenantId, userId, resourceId, null, false, e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if anonymous access is allowed for a resource with specific permission
     */
    public boolean hasAnonymousPermission(UUID resourceId, String permissionName, UUID tenantId) {
        try {
            Resource resource = Resource.findById(resourceId);
            Permission permission = Permission.findByName(permissionName);
            
            if (resource == null || permission == null) {
                auditService.logAnonymousAccess(tenantId, resourceId, 
                    permission != null ? permission.id : null, false, "Resource or permission not found");
                return false;
            }
            
            boolean hasAccess = checkAnonymousPermission(resource, permission);
            
            auditService.logAnonymousAccess(tenantId, resourceId, permission.id, hasAccess, 
                hasAccess ? null : "Anonymous access denied");
            
            return hasAccess;
        } catch (Exception e) {
            auditService.logAnonymousAccess(tenantId, resourceId, null, false, e.getMessage());
            return false;
        }
    }
    
    /**
     * Grant a permission to a user on a resource
     */
    @Transactional
    public ResourceGrant grantPermission(UUID userId, UUID resourceId, UUID permissionId, 
                                       UUID grantedBy, UUID tenantId) {
        return grantPermission(userId, null, resourceId, permissionId, grantedBy, tenantId, null);
    }
    
    /**
     * Grant a role-based permission on a resource
     */
    @Transactional
    public ResourceGrant grantRolePermission(UUID roleId, UUID resourceId, UUID permissionId, 
                                           UUID grantedBy, UUID tenantId) {
        return grantPermission(null, roleId, resourceId, permissionId, grantedBy, tenantId, null);
    }
    
    /**
     * Grant a permission with expiration
     */
    @Transactional
    public ResourceGrant grantPermission(UUID userId, UUID roleId, UUID resourceId, UUID permissionId, 
                                       UUID grantedBy, UUID tenantId, LocalDateTime expiresAt) {
        // Validate inputs
        Resource resource = Resource.findById(resourceId);
        Permission permission = Permission.findById(permissionId);
        
        if (resource == null || permission == null) {
            throw new IllegalArgumentException("Resource or permission not found");
        }
        
        if (userId != null && User.findById(userId) == null) {
            throw new IllegalArgumentException("User not found");
        }
        
        if (roleId != null && Role.findById(roleId) == null) {
            throw new IllegalArgumentException("Role not found");
        }
        
        // Check if grant already exists
        List<ResourceGrant> existingGrants;
        if (userId != null) {
            existingGrants = ResourceGrant.findByUserAndResource(userId, resourceId);
        } else {
            existingGrants = ResourceGrant.findByRole(roleId);
        }
        
        Optional<ResourceGrant> existing = existingGrants.stream()
            .filter(g -> g.permission.id.equals(permissionId) && g.isValid())
            .findFirst();
        
        if (existing.isPresent()) {
            return existing.get(); // Already granted
        }
        
        // Create new grant
        ResourceGrant grant = new ResourceGrant();
        grant.resource = resource;
        grant.permission = permission;
        grant.grantedBy = grantedBy;
        grant.tenantId = tenantId;
        grant.expiresAt = expiresAt;
        grant.grantType = GrantType.DIRECT;
        
        if (userId != null) {
            grant.user = User.findById(userId);
        } else {
            grant.role = Role.findById(roleId);
        }
        
        grant.persist();
        
        // Audit the grant
        auditService.logPermissionGranted(tenantId, grantedBy, userId, resourceId, permissionId);
        
        return grant;
    }
    
    /**
     * Revoke a permission from a user on a resource
     */
    @Transactional
    public boolean revokePermission(UUID userId, UUID resourceId, UUID permissionId, 
                                  UUID revokedBy, String reason, UUID tenantId) {
        List<ResourceGrant> grants = ResourceGrant.findByUserAndResource(userId, resourceId);
        
        boolean revoked = false;
        for (ResourceGrant grant : grants) {
            if (grant.permission.id.equals(permissionId) && grant.isValid()) {
                grant.revoke(revokedBy, reason);
                grant.persist();
                revoked = true;
                
                auditService.logPermissionRevoked(tenantId, revokedBy, userId, resourceId, permissionId, reason);
            }
        }
        
        return revoked;
    }
    
    /**
     * Share a resource with another user using a specific role
     */
    @Transactional
    public List<ResourceGrant> shareResource(UUID resourceId, UUID targetUserId, UUID roleId, 
                                           UUID sharedBy, UUID tenantId) {
        Role role = Role.findById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("Role not found");
        }
        
        List<ResourceGrant> grants = new ArrayList<>();
        
        // Grant all permissions from the role
        for (Permission permission : role.getAllPermissions()) {
            ResourceGrant grant = grantPermission(targetUserId, resourceId, permission.id, sharedBy, tenantId);
            grant.grantType = GrantType.DELEGATED;
            grant.persist();
            grants.add(grant);
        }
        
        auditService.logResourceShared(tenantId, sharedBy, targetUserId, resourceId, roleId);
        
        return grants;
    }
    
    /**
     * Publish a resource for public access
     */
    @Transactional
    public void publishResource(UUID resourceId, List<String> permissionNames, UUID publishedBy, UUID tenantId) {
        Resource resource = Resource.findById(resourceId);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found");
        }
        
        resource.isPublic = true;
        
        // Convert permission names to JSON array
        String permissionsJson = "[" + 
            permissionNames.stream()
                .map(name -> "\"" + name + "\"")
                .collect(Collectors.joining(", ")) + 
            "]";
        resource.publicPermissions = permissionsJson;
        
        // Generate public link token if not exists
        if (resource.publicLinkToken == null) {
            resource.generatePublicLinkToken();
        }
        
        resource.persist();
        
        auditService.logResourcePublished(tenantId, publishedBy, resourceId);
    }
    
    /**
     * Unpublish a resource (remove public access)
     */
    @Transactional
    public void unpublishResource(UUID resourceId, UUID unpublishedBy, UUID tenantId) {
        Resource resource = Resource.findById(resourceId);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found");
        }
        
        resource.isPublic = false;
        resource.publicPermissions = null;
        resource.publicLinkToken = null;
        resource.publicLinkExpiresAt = null;
        resource.persist();
        
        auditService.logResourceUnpublished(tenantId, unpublishedBy, resourceId);
    }
    
    /**
     * Get all resources accessible by a user
     */
    public List<Resource> getAccessibleResources(UUID userId, UUID tenantId) {
        User user = User.findById(userId);
        if (user == null) {
            return Collections.emptyList();
        }
        
        Set<UUID> accessibleResourceIds = new HashSet<>();
        
        // Resources owned by the user
        List<Resource> ownedResources = Resource.findByOwner(userId);
        accessibleResourceIds.addAll(ownedResources.stream().map(r -> r.id).collect(Collectors.toSet()));
        
        // Resources with direct grants
        List<ResourceGrant> userGrants = ResourceGrant.findByUser(userId);
        accessibleResourceIds.addAll(userGrants.stream()
            .filter(ResourceGrant::isValid)
            .map(g -> g.resource.id)
            .collect(Collectors.toSet()));
        
        // Resources through role grants
        if (user.roles != null) {
            for (Role role : user.roles) {
                List<ResourceGrant> roleGrants = ResourceGrant.findByRole(role.id);
                accessibleResourceIds.addAll(roleGrants.stream()
                    .filter(ResourceGrant::isValid)
                    .map(g -> g.resource.id)
                    .collect(Collectors.toSet()));
            }
        }
        
        // Resources through organization membership
        List<OrganizationMembership> memberships = OrganizationMembership.findActiveByUser(userId);
        for (OrganizationMembership membership : memberships) {
            List<Resource> orgResources = Resource.findByOrganization(membership.organization.id);
            accessibleResourceIds.addAll(orgResources.stream().map(r -> r.id).collect(Collectors.toSet()));
        }
        
        // Fetch all accessible resources
        return accessibleResourceIds.stream()
            .map(id -> Resource.<Resource>findById(id))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all public resources
     */
    public List<Resource> getPublicResources() {
        return Resource.findPublicResources();
    }
    
    /**
     * Validate access using public link token
     */
    public boolean validatePublicLink(String token, String permissionName) {
        Resource resource = Resource.findByPublicToken(token);
        if (resource == null || !resource.isPublicLinkValid()) {
            return false;
        }
        
        Permission permission = Permission.findByName(permissionName);
        if (permission == null) {
            return false;
        }
        
        return checkAnonymousPermission(resource, permission);
    }
    
    // Private helper methods
    
    private boolean checkUserPermission(User user, Resource resource, Permission permission, UUID tenantId) {
        // Ensure tenant isolation - both user and resource must be in the same tenant
        if (!user.tenantId.equals(tenantId) || !resource.tenantId.equals(tenantId)) {
            return false;
        }
        
        // Check if user is the owner
        if (user.id.equals(resource.ownerId)) {
            return true;
        }
        
        // Check direct grants
        List<ResourceGrant> userGrants = ResourceGrant.findByUserAndResource(user.id, resource.id);
        for (ResourceGrant grant : userGrants) {
            if (grant.permission.id.equals(permission.id) && grant.isValid()) {
                return true;
            }
        }
        
        // Check role-based grants
        if (user.roles != null) {
            for (Role role : user.roles) {
                if (role.getAllPermissions().contains(permission)) {
                    List<ResourceGrant> roleGrants = ResourceGrant.findByRole(role.id);
                    for (ResourceGrant grant : roleGrants) {
                        if (grant.resource.id.equals(resource.id) && grant.isValid()) {
                            return true;
                        }
                    }
                }
            }
        }
        
        // Check organization-level access
        List<OrganizationMembership> memberships = OrganizationMembership.findActiveByUser(user.id);
        for (OrganizationMembership membership : memberships) {
            if (membership.organization.id.equals(resource.organizationId)) {
                if (membership.role != null && membership.role.getAllPermissions().contains(permission)) {
                    return true;
                }
            }
        }
        
        // Check inherited permissions from parent resources
        return checkInheritedPermissions(user, resource, permission);
    }
    
    private boolean checkAnonymousPermission(Resource resource, Permission permission) {
        if (!resource.isPublic) {
            return false;
        }
        
        if (resource.publicPermissions == null) {
            return false;
        }
        
        // Parse public permissions JSON and check if permission is included
        try {
            String permissionsJson = resource.publicPermissions;
            return permissionsJson.contains("\"" + permission.name + "\"");
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean checkInheritedPermissions(User user, Resource resource, Permission permission) {
        Resource parent = resource.parentResource;
        if (parent != null) {
            return checkUserPermission(user, parent, permission, user.tenantId);
        }
        return false;
    }
}
