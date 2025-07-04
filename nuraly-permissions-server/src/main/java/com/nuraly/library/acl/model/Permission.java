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
 * Permission entity representing atomic permissions in the system
 * Supports custom fine-grained permissions beyond basic CRUD
 */
@Entity
@Table(name = "acl_permissions")
public class Permission extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;
    
    @NotBlank
    @Column(unique = true)
    public String name; // e.g., "read", "write", "publish", "share", "moderate", "annotate"
    
    public String description;
    
    @Column(name = "resource_type")
    public String resourceType; // e.g., "document", "dashboard", "function", "organization"
    
    @Column(name = "is_system_permission")
    public Boolean isSystemPermission = false; // Built-in vs custom permissions
    
    @Column(name = "is_active")
    public Boolean isActive = true;
    
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    // Many-to-many relationship with roles
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @JsonIgnore
    public Set<Role> roles;
    
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
    public static Permission findByName(String name) {
        return find("name", name).firstResult();
    }
    
    public static List<Permission> findByResourceType(String resourceType) {
        return find("resourceType", resourceType).list();
    }
    
    public static List<Permission> findSystemPermissions() {
        return find("isSystemPermission", true).list();
    }
}
