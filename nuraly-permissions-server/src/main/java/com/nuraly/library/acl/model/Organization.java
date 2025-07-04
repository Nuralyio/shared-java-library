package com.nuraly.library.acl.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;
import java.util.UUID;

/**
 * Organization entity for multi-tenant support
 * Supports hierarchical organizations and team-based permissions
 */
@Entity
@Table(name = "acl_organizations")
public class Organization extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;
    
    @NotBlank
    @Column(unique = true)
    public String name;
    
    public String description;
    
    @Column(name = "external_tenant_id")
    public UUID externalTenantId;
    
    @Column(name = "owner_id")
    public UUID ownerId; // External User ID who owns this organization
    
    // Self-referencing relationship for hierarchical organizations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_organization_id")
    public Organization parentOrganization;
     @OneToMany(mappedBy = "parentOrganization", cascade = CascadeType.ALL)
    @JsonIgnore
    public Set<Organization> childOrganizations;

    @Column(name = "is_active")
    public Boolean isActive = true;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    // One-to-many relationship with organization memberships
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    public Set<OrganizationMembership> memberships;
    
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
    public static Organization findByName(String name) {
        return find("name", name).firstResult();
    }
    
    public static List<Organization> findByOwner(UUID ownerId) {
        return find("ownerId", ownerId).list();
    }
    
    public static List<Organization> findByTenant(UUID externalTenantId) {
        return find("externalTenantId", externalTenantId).list();
    }
    
    public static List<Organization> findByParent(UUID parentId) {
        return find("parentOrganization.id", parentId).list();
    }
}
