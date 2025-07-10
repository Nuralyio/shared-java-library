package com.nuraly.library.acl.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ResourceGrant entity representing specific permission grants on resources
 * Supports delegation, expiration, and audit tracking
 */
@Entity
@Table(name = "acl_resource_grants")
public class ResourceGrant extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    public Resource resource;
    
    @Column(name = "external_user_id")
    public UUID externalUserId; // Can be null for role-based grants
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    public Role role; // Can be null for user-specific grants
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    public Permission permission;
    
    @Enumerated(EnumType.STRING)
    public GrantType grantType; // DIRECT, INHERITED, DELEGATED
    
    @Column(name = "granted_by")
    public UUID grantedBy; // External User ID who granted this permission
    
    @Column(name = "external_tenant_id")
    public UUID externalTenantId;
    
    @Column(name = "expires_at")
    public LocalDateTime expiresAt; // Optional expiration
    
    @Column(name = "is_active")
    public Boolean isActive = true;
    
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    // Audit fields
    @Column(name = "revoked_at")
    public LocalDateTime revokedAt;
    
    @Column(name = "revoked_by")
    public UUID revokedBy; // External User ID who revoked this permission
    
    @Column(name = "revocation_reason")
    public String revocationReason;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Finder methods
    public static List<ResourceGrant> findByResource(UUID resourceId) {
        return find("resource.id", resourceId).list();
    }
    
    public static List<ResourceGrant> findByExternalUser(UUID externalUserId) {
        return find("externalUserId", externalUserId).list();
    }
    
    public static List<ResourceGrant> findByRole(UUID roleId) {
        return find("role.id", roleId).list();
    }
    
    public static List<ResourceGrant> findByExternalUserAndResource(UUID externalUserId, String resourceId) {
        return find("externalUserId = ?1 and resource.id = ?2", externalUserId, resourceId).list();
    }
    
    public static List<ResourceGrant> findByTenant(UUID externalTenantId) {
        return find("externalTenantId", externalTenantId).list();
    }
    
    public static List<ResourceGrant> findActiveGrants() {
        return find("isActive = true and (expiresAt is null or expiresAt > ?1)", LocalDateTime.now()).list();
    }
    
    /**
     * Check if this grant is currently valid
     */
    public boolean isValid() {
        return isActive && 
               revokedAt == null && 
               (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }
    
    /**
     * Revoke this grant
     */
    public void revoke(UUID revokedBy, String reason) {
        this.isActive = false;
        this.revokedAt = LocalDateTime.now();
        this.revokedBy = revokedBy;
        this.revocationReason = reason;
    }
}
