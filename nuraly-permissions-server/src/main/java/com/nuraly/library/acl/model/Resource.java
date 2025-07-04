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
 * Resource entity representing any resource that can have permissions applied
 * Supports hierarchical resources and public/anonymous access
 */
@Entity
@Table(name = "acl_resources")
public class Resource extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;
    
    @NotBlank
    public String name;
    
    public String description;
    
    @NotBlank
    @Column(name = "resource_type")
    public String resourceType; // e.g., "document", "dashboard", "function", "organization"
    
    @Column(name = "external_id")
    public String externalId; // Reference to the actual resource in other systems
    
    @Column(name = "external_tenant_id")
    public UUID externalTenantId;
    
    @Column(name = "owner_id")
    public UUID ownerId; // External User ID who owns this resource
    
    @Column(name = "organization_id")
    public UUID organizationId; // Organization this resource belongs to
    
    // Self-referencing relationship for hierarchical resources
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_resource_id")
    @JsonIgnore
    public Resource parentResource;
    
    @OneToMany(mappedBy = "parentResource", cascade = CascadeType.ALL)
    @JsonIgnore
    public Set<Resource> childResources;
    
    // Public access configuration
    @Column(name = "is_public")
    public Boolean isPublic = false;
    
    @Column(name = "public_permissions")
    public String publicPermissions; // JSON array of permission names for anonymous users
    
    @Column(name = "public_link_token")
    public String publicLinkToken; // Unique token for public access links
    
    @Column(name = "public_link_expires_at")
    public LocalDateTime publicLinkExpiresAt;
    
    @Column(name = "is_active")
    public Boolean isActive = true;
    
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    // One-to-many relationship with resource grants
    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    public Set<ResourceGrant> resourceGrants;
    
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
    public static Resource findByExternalId(String externalId) {
        return find("externalId", externalId).firstResult();
    }
    
    public static List<Resource> findByResourceType(String resourceType) {
        return find("resourceType", resourceType).list();
    }
    
    public static List<Resource> findByOwner(UUID ownerId) {
        return find("ownerId", ownerId).list();
    }
    
    public static List<Resource> findByOrganization(UUID organizationId) {
        return find("organizationId", organizationId).list();
    }
    
    public static List<Resource> findByTenant(UUID externalTenantId) {
        return find("externalTenantId", externalTenantId).list();
    }
    
    public static List<Resource> findPublicResources() {
        return find("isPublic", true).list();
    }
    
    public static Resource findByPublicToken(String token) {
        return find("publicLinkToken", token).firstResult();
    }
    
    /**
     * Generate a unique public link token
     */
    public void generatePublicLinkToken() {
        this.publicLinkToken = UUID.randomUUID().toString();
    }
    
    /**
     * Check if public link is valid (not expired)
     */
    public boolean isPublicLinkValid() {
        return publicLinkToken != null && 
               (publicLinkExpiresAt == null || publicLinkExpiresAt.isAfter(LocalDateTime.now()));
    }
}
