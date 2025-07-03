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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;
    
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
    public UUID invitedBy;
    
    @Column(name = "is_active")
    public Boolean isActive = true;
    
    @Column(name = "joined_at")
    public LocalDateTime joinedAt;
    
    @Column(name = "invited_at")
    public LocalDateTime invitedAt;
    
    @Column(name = "expires_at")
    public LocalDateTime expiresAt; // Optional membership expiration
    
    @Column(name = "tenant_id")
    public UUID tenantId;
    
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
    public static List<OrganizationMembership> findByUser(UUID userId) {
        return find("user.id", userId).list();
    }
    
    public static List<OrganizationMembership> findByOrganization(UUID organizationId) {
        return find("organization.id", organizationId).list();
    }
    
    public static List<OrganizationMembership> findByUserAndOrganization(UUID userId, UUID organizationId) {
        return find("user.id = ?1 and organization.id = ?2", userId, organizationId).list();
    }
    
    public static List<OrganizationMembership> findActiveByUser(UUID userId) {
        return find("user.id = ?1 and isActive = true and (expiresAt is null or expiresAt > ?2)", 
                   userId, LocalDateTime.now()).list();
    }
    
    public static List<OrganizationMembership> findByTenant(UUID tenantId) {
        return find("tenantId", tenantId).list();
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
