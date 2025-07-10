package com.nuraly.library.acl.service;

import com.nuraly.library.acl.dto.*;
import com.nuraly.library.acl.model.ResourceGrant;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing permission operations
 */
@ApplicationScoped
public class PermissionManagementService {
    
    @Inject
    ACLService aclService;
    
    /**
     * Grant permission to a user on a resource
     */
    public ResourceGrant grantPermission(SimpleGrantPermissionRequest request, UUID currentUserId, UUID currentTenantId) {
        return aclService.grantPermission(
            request.targetUserId,
            request.resourceId,
            request.permissionId,
            currentUserId,
            currentTenantId
        );
    }
    
    /**
     * Grant permission to a user on a resource (legacy method)
     */
    public ResourceGrant grantPermission(GrantPermissionRequest request, UUID currentUserId, UUID currentTenantId) {
        return aclService.grantPermission(
            request.userId,
            request.resourceId,
            request.permissionId,
            currentUserId,
            currentTenantId
        );
    }
    
    /**
     * Grant role-based permission on a resource
     */
    public ResourceGrant grantRolePermission(GrantRolePermissionRequest request, UUID currentUserId, UUID currentTenantId) {
        return aclService.grantRolePermission(
            request.roleId,
            request.resourceId,
            request.permissionId,
            currentUserId,
            currentTenantId
        );
    }
    
    /**
     * Revoke permission from a user on a resource
     */
    public boolean revokePermission(SimpleRevokePermissionRequest request, UUID currentUserId, UUID currentTenantId) {
        return aclService.revokePermission(
            request.targetUserId,
            request.resourceId,
            request.permissionId,
            currentUserId,
            request.reason,
            currentTenantId
        );
    }
    
    /**
     * Revoke permission from a user on a resource (legacy method)
     */
    public boolean revokePermission(RevokePermissionRequest request, UUID currentUserId, UUID currentTenantId) {
        return aclService.revokePermission(
            request.userId,
            request.resourceId,
            request.permissionId,
            currentUserId,
            request.reason,
            currentTenantId
        );
    }
    
    /**
     * Share a resource with another user by granting them a specific role
     */
    public List<ResourceGrant> shareResource(ShareResourceRequest request, UUID currentUserId, UUID currentTenantId) {
        return aclService.shareResource(
            request.resourceId,
            request.targetUserId,
            request.roleId,
            currentUserId,
            currentTenantId
        );
    }
}
