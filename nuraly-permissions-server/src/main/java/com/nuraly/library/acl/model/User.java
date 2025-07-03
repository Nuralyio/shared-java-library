package com.nuraly.library.acl.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;
import java.util.UUID;

/**
 * User entity representing authenticated users in the system
 */
@Entity
@Table(name = "acl_users")
public class User extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;
    
    @NotBlank
    @Column(unique = true)
    public String username;
    
    @Email
    @Column(unique = true)
    public String email;
    
    @Column(name = "display_name")
    public String displayName;
    
    @Column(name = "tenant_id")
    public UUID tenantId;
    
    @Column(name = "is_active")
    public Boolean isActive = true;
    
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    // Many-to-many relationship with roles through user_roles table
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "acl_user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @JsonIgnore
    public Set<Role> roles;
    
    // One-to-many relationship with resource grants
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
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
    public static User findByUsername(String username) {
        return find("username", username).firstResult();
    }
    
    public static User findByEmail(String email) {
        return find("email", email).firstResult();
    }
    
    public static List<User> findByTenant(UUID tenantId) {
        return find("tenantId", tenantId).list();
    }
}
