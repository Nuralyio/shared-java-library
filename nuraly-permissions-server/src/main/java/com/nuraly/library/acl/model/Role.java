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
 * Role entity representing reusable permission bundles
 * Supports inheritance through parent-child relationships
 */
@Entity
@Table(name = "acl_roles")
public class Role extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;
    
    @NotBlank
    @Column(unique = true)
    public String name;
    
    public String description;
    
    @Column(name = "tenant_id")
    public UUID tenantId;
    
    @Enumerated(EnumType.STRING)
    public RoleScope scope; // APPLICATION, ORGANIZATION, RESOURCE
    
    @Column(name = "is_system_role")
    public Boolean isSystemRole = false; // Built-in roles vs custom roles
    
    @Column(name = "is_active")
    public Boolean isActive = true;
    
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    // Self-referencing relationship for role inheritance
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_role_id")
    public Role parentRole;
    
    @OneToMany(mappedBy = "parentRole", cascade = CascadeType.ALL)
    @JsonIgnore
    public Set<Role> childRoles;
    
    // Many-to-many relationship with permissions
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "acl_role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @JsonIgnore
    public Set<Permission> permissions;
    
    // Many-to-many relationship with users
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @JsonIgnore
    public Set<User> users;
    
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
    public static Role findByName(String name) {
        return find("name", name).firstResult();
    }
    
    public static List<Role> findByScope(RoleScope scope) {
        return find("scope", scope).list();
    }
    
    public static List<Role> findByTenant(UUID tenantId) {
        return find("tenantId", tenantId).list();
    }
    
    public static List<Role> findSystemRoles() {
        return find("isSystemRole", true).list();
    }
    
    /**
     * Get all permissions including inherited from parent roles
     */
    public Set<Permission> getAllPermissions() {
        Set<Permission> allPermissions = new java.util.HashSet<>(permissions);
        if (parentRole != null) {
            allPermissions.addAll(parentRole.getAllPermissions());
        }
        return allPermissions;
    }
}
