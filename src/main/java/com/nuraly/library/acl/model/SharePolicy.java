package com.nuraly.library.acl.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * SharePolicy entity for managing sharing templates and policies
 * Inspired by PowerApps sharing policies like "anyone with the link can view"
 */
@Entity
@Table(name = "acl_share_policies")
public class SharePolicy extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;
    
    @Column(name = "name", nullable = false)
    public String name; // e.g., "Public Read-Only", "Team Collaboration", "Anonymous View"
    
    @Column(name = "description")
    public String description;
    
    @Column(name = "tenant_id")
    public UUID tenantId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false)
    public SharePolicyType policyType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false)
    public AccessLevel accessLevel;
    
    // JSON array of permission names that this policy grants
    @Column(name = "permissions", columnDefinition = "TEXT")
    public String permissions; // e.g., ["read", "annotate"] for anonymous users
    
    @Column(name = "require_authentication")
    public Boolean requireAuthentication = true;
    
    @Column(name = "allow_anonymous")
    public Boolean allowAnonymous = false;
    
    @Column(name = "max_uses")
    public Integer maxUses; // Optional limit on how many times this can be used
    
    @Column(name = "expires_after_hours")
    public Integer expiresAfterHours; // Auto-expiration time
    
    @Column(name = "is_system_policy")
    public Boolean isSystemPolicy = false; // Built-in vs custom policies
    
    @Column(name = "is_active")
    public Boolean isActive = true;
    
    @Column(name = "created_by")
    public UUID createdBy;
    
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
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
    public static SharePolicy findByName(String name) {
        return find("name", name).firstResult();
    }
    
    public static List<SharePolicy> findByTenant(UUID tenantId) {
        return find("tenantId", tenantId).list();
    }
    
    public static List<SharePolicy> findByPolicyType(SharePolicyType policyType) {
        return find("policyType", policyType).list();
    }
    
    public static List<SharePolicy> findSystemPolicies() {
        return find("isSystemPolicy", true).list();
    }
    
    public static List<SharePolicy> findAnonymousPolicies() {
        return find("allowAnonymous", true).list();
    }
    
    public static List<SharePolicy> findByCreator(UUID createdBy) {
        return find("createdBy", createdBy).list();
    }
    
    /**
     * Create default system share policies
     */
    public static void createSystemPolicies(UUID tenantId) {
        // Check if system policies already exist for this tenant
        long existingPolicies = SharePolicy.count("tenantId = ?1 and isSystemPolicy = true", tenantId);
        if (existingPolicies > 0) {
            // System policies already exist for this tenant, skip creation
            return;
        }
        
        // Public Read-Only policy
        SharePolicy publicReadOnly = new SharePolicy();
        publicReadOnly.name = "Public Read-Only";
        publicReadOnly.description = "Anyone with the link can view";
        publicReadOnly.tenantId = tenantId;
        publicReadOnly.policyType = SharePolicyType.PUBLIC_LINK;
        publicReadOnly.accessLevel = AccessLevel.READ_ONLY;
        publicReadOnly.permissions = "[\"read\"]";
        publicReadOnly.requireAuthentication = false;
        publicReadOnly.allowAnonymous = true;
        publicReadOnly.isSystemPolicy = true;
        publicReadOnly.persist();
        
        // Team Collaboration policy
        SharePolicy teamCollab = new SharePolicy();
        teamCollab.name = "Team Collaboration";
        teamCollab.description = "Organization members can edit and share";
        teamCollab.tenantId = tenantId;
        teamCollab.policyType = SharePolicyType.ORGANIZATION;
        teamCollab.accessLevel = AccessLevel.FULL_ACCESS;
        teamCollab.permissions = "[\"read\", \"write\", \"share\", \"annotate\"]";
        teamCollab.requireAuthentication = true;
        teamCollab.allowAnonymous = false;
        teamCollab.isSystemPolicy = true;
        teamCollab.persist();
        
        // Anonymous View policy
        SharePolicy anonymousView = new SharePolicy();
        anonymousView.name = "Anonymous View";
        anonymousView.description = "Anonymous users can view content";
        anonymousView.tenantId = tenantId;
        anonymousView.policyType = SharePolicyType.ANONYMOUS;
        anonymousView.accessLevel = AccessLevel.READ_ONLY;
        anonymousView.permissions = "[\"read\"]";
        anonymousView.requireAuthentication = false;
        anonymousView.allowAnonymous = true;
        anonymousView.expiresAfterHours = 24; // 24 hours default
        anonymousView.isSystemPolicy = true;
        anonymousView.persist();
    }
}
