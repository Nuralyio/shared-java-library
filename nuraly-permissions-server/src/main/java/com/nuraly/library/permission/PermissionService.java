package com.nuraly.library.permission;

import com.nuraly.library.acl.service.ACLService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

/**
 * Enhanced Permission Service that integrates with the ACL system
 * Provides backward compatibility while leveraging the new ACL features
 */
@ApplicationScoped
public class PermissionService {
    
    @Inject
    ACLService aclService;

    /**
     * Check if user has permission (enhanced version)
     */
    public boolean hasPermission(String userId, String permissionName, String resourceId, String tenantId) {
        try {
            UUID userUUID = UUID.fromString(userId);
            UUID resourceUUID = UUID.fromString(resourceId);
            UUID tenantUUID = tenantId != null ? UUID.fromString(tenantId) : null;
            
            return aclService.hasPermission(userUUID, resourceUUID, permissionName, tenantUUID);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if anonymous access is allowed
     */
    public boolean hasAnonymousPermission(String resourceId, String permissionName, String tenantId) {
        try {
            UUID resourceUUID = UUID.fromString(resourceId);
            UUID tenantUUID = tenantId != null ? UUID.fromString(tenantId) : null;
            
            return aclService.hasAnonymousPermission(resourceUUID, permissionName, tenantUUID);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validate public link access
     */
    public boolean validatePublicLink(String token, String permissionName) {
        try {
            return aclService.validatePublicLink(token, permissionName);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Legacy method - kept for backward compatibility
     */
    @Deprecated
    public boolean hasPermission(String userId, String permission) {
        // This method should be updated to include resource and tenant context
        return false;
    }
}