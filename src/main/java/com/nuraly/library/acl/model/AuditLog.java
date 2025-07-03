package com.nuraly.library.acl.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AuditLog entity for tracking all permission changes and access attempts
 * Essential for compliance and security monitoring
 */
@Entity
@Table(name = "acl_audit_logs")
public class AuditLog extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;
    
    @Column(name = "tenant_id")
    public UUID tenantId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    public AuditActionType actionType;
    
    @Column(name = "user_id")
    public UUID userId; // Can be null for anonymous actions
    
    @Column(name = "resource_id")
    public UUID resourceId;
    
    @Column(name = "permission_id")
    public UUID permissionId;
    
    @Column(name = "role_id")
    public UUID roleId;
    
    @Column(name = "target_user_id")
    public UUID targetUserId; // User affected by the action
    
    @Column(name = "ip_address")
    public String ipAddress;
    
    @Column(name = "user_agent")
    public String userAgent;
    
    @Column(name = "details", length = 1000)
    public String details; // JSON or descriptive text
    
    @Column(name = "success")
    public Boolean success = true;
    
    @Column(name = "error_message")
    public String errorMessage;
    
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Finder methods
    public static List<AuditLog> findByUser(UUID userId) {
        return find("userId", userId).list();
    }
    
    public static List<AuditLog> findByResource(UUID resourceId) {
        return find("resourceId", resourceId).list();
    }
    
    public static List<AuditLog> findByActionType(AuditActionType actionType) {
        return find("actionType", actionType).list();
    }
    
    public static List<AuditLog> findByTenant(UUID tenantId) {
        return find("tenantId", tenantId).list();
    }
    
    public static List<AuditLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return find("createdAt >= ?1 and createdAt <= ?2", startDate, endDate).list();
    }
    
    public static List<AuditLog> findFailedActions() {
        return find("success", false).list();
    }
    
    // Factory methods for common audit events
    public static AuditLog createPermissionGranted(UUID tenantId, UUID grantedBy, UUID targetUserId, 
                                                  UUID resourceId, UUID permissionId) {
        AuditLog log = new AuditLog();
        log.tenantId = tenantId;
        log.actionType = AuditActionType.PERMISSION_GRANTED;
        log.userId = grantedBy;
        log.targetUserId = targetUserId;
        log.resourceId = resourceId;
        log.permissionId = permissionId;
        log.success = true;
        return log;
    }
    
    public static AuditLog createPermissionRevoked(UUID tenantId, UUID revokedBy, UUID targetUserId, 
                                                  UUID resourceId, UUID permissionId, String reason) {
        AuditLog log = new AuditLog();
        log.tenantId = tenantId;
        log.actionType = AuditActionType.PERMISSION_REVOKED;
        log.userId = revokedBy;
        log.targetUserId = targetUserId;
        log.resourceId = resourceId;
        log.permissionId = permissionId;
        log.details = reason;
        log.success = true;
        return log;
    }
    
    public static AuditLog createAccessAttempt(UUID tenantId, UUID userId, UUID resourceId, 
                                              UUID permissionId, boolean success, String errorMessage) {
        AuditLog log = new AuditLog();
        log.tenantId = tenantId;
        log.actionType = AuditActionType.ACCESS_ATTEMPT;
        log.userId = userId;
        log.resourceId = resourceId;
        log.permissionId = permissionId;
        log.success = success;
        log.errorMessage = errorMessage;
        return log;
    }
    
    public static AuditLog createResourcePublished(UUID tenantId, UUID userId, UUID resourceId) {
        AuditLog log = new AuditLog();
        log.tenantId = tenantId;
        log.actionType = AuditActionType.RESOURCE_PUBLISHED;
        log.userId = userId;
        log.resourceId = resourceId;
        log.success = true;
        return log;
    }
    
    public static AuditLog createResourceUnpublished(UUID tenantId, UUID userId, UUID resourceId) {
        AuditLog log = new AuditLog();
        log.tenantId = tenantId;
        log.actionType = AuditActionType.RESOURCE_UNPUBLISHED;
        log.userId = userId;
        log.resourceId = resourceId;
        log.success = true;
        return log;
    }
}
