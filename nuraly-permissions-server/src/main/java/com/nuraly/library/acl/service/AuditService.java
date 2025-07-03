package com.nuraly.library.acl.service;

import com.nuraly.library.acl.model.AuditLog;
import com.nuraly.library.acl.model.AuditActionType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.UUID;

/**
 * Service for handling audit logging of all ACL operations
 */
@ApplicationScoped
public class AuditService {
    
    @Transactional
    public void logAccessAttempt(UUID tenantId, UUID userId, UUID resourceId, UUID permissionId, 
                               boolean success, String errorMessage) {
        AuditLog log = AuditLog.createAccessAttempt(tenantId, userId, resourceId, permissionId, success, errorMessage);
        log.persist();
    }
    
    @Transactional
    public void logAnonymousAccess(UUID tenantId, UUID resourceId, UUID permissionId, 
                                 boolean success, String errorMessage) {
        AuditLog log = AuditLog.createAccessAttempt(tenantId, null, resourceId, permissionId, success, errorMessage);
        log.actionType = AuditActionType.ANONYMOUS_ACCESS;
        log.persist();
    }
    
    @Transactional
    public void logPermissionGranted(UUID tenantId, UUID grantedBy, UUID targetUserId, 
                                   UUID resourceId, UUID permissionId) {
        AuditLog log = AuditLog.createPermissionGranted(tenantId, grantedBy, targetUserId, resourceId, permissionId);
        log.persist();
    }
    
    @Transactional
    public void logPermissionRevoked(UUID tenantId, UUID revokedBy, UUID targetUserId, 
                                   UUID resourceId, UUID permissionId, String reason) {
        AuditLog log = AuditLog.createPermissionRevoked(tenantId, revokedBy, targetUserId, resourceId, permissionId, reason);
        log.persist();
    }
    
    @Transactional
    public void logResourcePublished(UUID tenantId, UUID userId, UUID resourceId) {
        AuditLog log = AuditLog.createResourcePublished(tenantId, userId, resourceId);
        log.persist();
    }
    
    @Transactional
    public void logResourceUnpublished(UUID tenantId, UUID userId, UUID resourceId) {
        AuditLog log = AuditLog.createResourceUnpublished(tenantId, userId, resourceId);
        log.persist();
    }
    
    @Transactional
    public void logResourceShared(UUID tenantId, UUID sharedBy, UUID targetUserId, UUID resourceId, UUID roleId) {
        AuditLog log = new AuditLog();
        log.tenantId = tenantId;
        log.actionType = AuditActionType.RESOURCE_SHARED;
        log.userId = sharedBy;
        log.targetUserId = targetUserId;
        log.resourceId = resourceId;
        log.roleId = roleId;
        log.success = true;
        log.persist();
    }
}
