package com.nuraly.library.acl.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * OrganizationMembership entity representing user membership in organizations
 * Supports different membership types and roles within organizations
 */
@Entity
@Table(name = "acl_organization_memberships")
public class OrganizationMembership extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;
    
    @Column(name = "external_user_id", nullable = false)
    public UUID externalUserId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    public Organization organization;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    public Role role; // Organization-specific role
    
    @Enumerated(EnumType.STRING)
    @Column(name = "membership_type")
    public MembershipType membershipType = MembershipType.MEMBER;
    
    @Column(name = "invited_by")
    public UUID invitedBy; // External User ID who invited this member
    
    @Column(name = "is_active")
    public Boolean isActive = true;
    
    @Column(name = "joined_at")
    public LocalDateTime joinedAt;
    
    @Column(name = "invited_at")
    public LocalDateTime invitedAt;
    
    @Column(name = "expires_at")
    public LocalDateTime expiresAt; // Optional membership expiration
    
    @Column(name = "external_tenant_id")
    public UUID externalTenantId;
    
    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
        if (invitedAt == null) {
            invitedAt = LocalDateTime.now();
        }
    }
    
    // Finder methods
    public static List<OrganizationMembership> findByExternalUser(UUID externalUserId) {
        return find("externalUserId", externalUserId).list();
    }
    
    public static List<OrganizationMembership> findByOrganization(UUID organizationId) {
        return find("organization.id", organizationId).list();
    }
    
    public static List<OrganizationMembership> findByExternalUserAndOrganization(UUID externalUserId, UUID organizationId) {
        return find("externalUserId = ?1 and organization.id = ?2", externalUserId, organizationId).list();
    }
    
    public static List<OrganizationMembership> findActiveByExternalUser(UUID externalUserId) {
        return find("externalUserId = ?1 and isActive = true and (expiresAt is null or expiresAt > ?2)", 
                   externalUserId, LocalDateTime.now()).list();
    }
    
    public static List<OrganizationMembership> findByTenant(UUID externalTenantId) {
        return find("externalTenantId", externalTenantId).list();
    }
    
    /**
     * Check if this membership is currently valid
     */
    public boolean isValid() {
        return isActive && (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }
    
    /**
     * Deactivate this membership
     */
    public void deactivate() {
        this.isActive = false;
    }
}
