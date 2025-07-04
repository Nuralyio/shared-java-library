package com.nuraly.library.acl.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * UserRole entity representing user-role assignments
 * Links external users to ACL roles
 */
@Entity
@Table(name = "acl_user_roles")
public class UserRole extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;
    
    @Column(name = "external_user_id", nullable = false)
    public UUID externalUserId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    public Role role;
    
    @Column(name = "assigned_by")
    public UUID assignedBy; // External User ID who assigned this role
    
    @Column(name = "is_active")
    public Boolean isActive = true;
    
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
    public static List<UserRole> findByExternalUser(UUID externalUserId) {
        return find("externalUserId", externalUserId).list();
    }
    
    public static List<UserRole> findByRole(UUID roleId) {
        return find("role.id", roleId).list();
    }
    
    public static List<UserRole> findActiveByExternalUser(UUID externalUserId) {
        return find("externalUserId = ?1 and isActive = true", externalUserId).list();
    }
    
    public static List<UserRole> findByExternalUserAndRole(UUID externalUserId, UUID roleId) {
        return find("externalUserId = ?1 and role.id = ?2", externalUserId, roleId).list();
    }
}
