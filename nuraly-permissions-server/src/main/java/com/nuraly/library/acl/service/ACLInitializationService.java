package com.nuraly.library.acl.service;

import com.nuraly.library.acl.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import io.quarkus.runtime.StartupEvent;
import java.util.UUID;

/**
 * Service to initialize default ACL system data
 * Creates system permissions, roles, and policies on startup
 */
@ApplicationScoped
public class ACLInitializationService {
    
    @Transactional
    public void onStart(@Observes StartupEvent ev) {
        System.out.println("Initializing ACL system...");
        
        try {
            // Create default system permissions
            createSystemPermissions();
            
            // Create default system roles
            createSystemRoles();
            
            System.out.println("ACL system initialization completed successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize ACL system: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create system-wide permissions that are available across all tenants
     */
    private void createSystemPermissions() {
        createPermissionIfNotExists("read", "Permission to read/view content", null, true);
        createPermissionIfNotExists("write", "Permission to write/edit content", null, true);
        createPermissionIfNotExists("delete", "Permission to delete content", null, true);
        createPermissionIfNotExists("share", "Permission to share content with others", null, true);
        createPermissionIfNotExists("publish", "Permission to publish/unpublish content", null, true);
        createPermissionIfNotExists("annotate", "Permission to annotate/comment on content", null, true);
        createPermissionIfNotExists("moderate", "Permission to moderate content and users", null, true);
        createPermissionIfNotExists("admin", "Administrative permissions", null, true);
        
        // Document-specific permissions
        createPermissionIfNotExists("read", "Read documents", "document", true);
        createPermissionIfNotExists("write", "Edit documents", "document", true);
        createPermissionIfNotExists("annotate", "Comment on documents", "document", true);
        createPermissionIfNotExists("share", "Share documents", "document", true);
        createPermissionIfNotExists("publish", "Publish documents", "document", true);
        
        // Dashboard-specific permissions
        createPermissionIfNotExists("read", "View dashboards", "dashboard", true);
        createPermissionIfNotExists("write", "Edit dashboards", "dashboard", true);
        createPermissionIfNotExists("share", "Share dashboards", "dashboard", true);
        createPermissionIfNotExists("publish", "Publish dashboards", "dashboard", true);
        
        // Function-specific permissions
        createPermissionIfNotExists("read", "View functions", "function", true);
        createPermissionIfNotExists("write", "Edit functions", "function", true);
        createPermissionIfNotExists("execute", "Execute functions", "function", true);
        createPermissionIfNotExists("deploy", "Deploy functions", "function", true);
        
        // Organization-specific permissions
        createPermissionIfNotExists("read", "View organization details", "organization", true);
        createPermissionIfNotExists("write", "Edit organization settings", "organization", true);
        createPermissionIfNotExists("admin", "Administer organization", "organization", true);
        createPermissionIfNotExists("invite", "Invite members to organization", "organization", true);
        createPermissionIfNotExists("remove", "Remove members from organization", "organization", true);
        
        System.out.println("System permissions created successfully");
    }
    
    /**
     * Create system-wide roles that are available across all tenants
     */
    private void createSystemRoles() {
        // Application-level roles
        Role superAdmin = createRoleIfNotExists("Super Admin", "Full system administration", 
                                               null, RoleScope.APPLICATION, true);
        if (superAdmin != null) {
            addPermissionsToRole(superAdmin, "admin", "read", "write", "delete", "share", "publish", "moderate");
        }
        
        Role platformUser = createRoleIfNotExists("Platform User", "Basic platform access", 
                                                 null, RoleScope.APPLICATION, true);
        if (platformUser != null) {
            addPermissionsToRole(platformUser, "read");
        }
        
        // Organization-level roles
        Role orgOwner = createRoleIfNotExists("Organization Owner", "Organization owner with full access", 
                                             null, RoleScope.ORGANIZATION, true);
        if (orgOwner != null) {
            addPermissionsToRole(orgOwner, "admin", "read", "write", "delete", "share", "publish", "moderate", "invite", "remove");
        }
        
        Role orgAdmin = createRoleIfNotExists("Organization Admin", "Organization administrator", 
                                             null, RoleScope.ORGANIZATION, true);
        if (orgAdmin != null) {
            addPermissionsToRole(orgAdmin, "read", "write", "share", "publish", "moderate", "invite");
        }
        
        Role orgMember = createRoleIfNotExists("Organization Member", "Regular organization member", 
                                              null, RoleScope.ORGANIZATION, true);
        if (orgMember != null) {
            addPermissionsToRole(orgMember, "read", "write", "share");
        }
        
        Role orgGuest = createRoleIfNotExists("Organization Guest", "Guest with limited access", 
                                             null, RoleScope.ORGANIZATION, true);
        if (orgGuest != null) {
            addPermissionsToRole(orgGuest, "read");
        }
        
        // Resource-level roles
        Role viewer = createRoleIfNotExists("Viewer", "Can only view content", 
                                           null, RoleScope.RESOURCE, true);
        if (viewer != null) {
            addPermissionsToRole(viewer, "read");
        }
        
        Role editor = createRoleIfNotExists("Editor", "Can edit and share content", 
                                           null, RoleScope.RESOURCE, true);
        if (editor != null) {
            addPermissionsToRole(editor, "read", "write", "annotate", "share");
        }
        
        Role publisher = createRoleIfNotExists("Publisher", "Can publish and manage content", 
                                              null, RoleScope.RESOURCE, true);
        if (publisher != null) {
            addPermissionsToRole(publisher, "read", "write", "annotate", "share", "publish");
        }
        
        Role moderator = createRoleIfNotExists("Moderator", "Can moderate content and users", 
                                              null, RoleScope.RESOURCE, true);
        if (moderator != null) {
            addPermissionsToRole(moderator, "read", "write", "annotate", "share", "publish", "moderate");
        }
        
        System.out.println("System roles created successfully");
    }
    
    /**
     * Initialize tenant-specific data
     */
    @Transactional
    public void initializeTenant(UUID tenantId) {
        try {
            // Create default share policies for the tenant
            SharePolicy.createSystemPolicies(tenantId);
            
            // Create tenant-specific permissions if needed
            createTenantSpecificPermissions(tenantId);
            
            System.out.println("Tenant " + tenantId + " initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize tenant " + tenantId + ": " + e.getMessage());
            throw e;
        }
    }
    
    private void createTenantSpecificPermissions(UUID tenantId) {
        // Create tenant-specific custom permissions if needed
        // This can be extended based on specific tenant requirements
    }
    
    private Permission createPermissionIfNotExists(String name, String description, 
                                                  String resourceType, boolean isSystemPermission) {
        String uniqueName = resourceType != null ? resourceType + ":" + name : name;
        Permission existing = Permission.find("name = ?1 and resourceType = ?2", uniqueName, resourceType).firstResult();
        
        if (existing == null) {
            Permission permission = new Permission();
            permission.name = uniqueName;
            permission.description = description;
            permission.resourceType = resourceType;
            permission.isSystemPermission = isSystemPermission;
            permission.persist();
            return permission;
        }
        
        return existing;
    }
    
    private Role createRoleIfNotExists(String name, String description, UUID tenantId, 
                                      RoleScope scope, boolean isSystemRole) {
        Role existing = Role.find("name = ?1 and tenantId = ?2", name, tenantId).firstResult();
        
        if (existing == null) {
            Role role = new Role();
            role.name = name;
            role.description = description;
            role.tenantId = tenantId;
            role.scope = scope;
            role.isSystemRole = isSystemRole;
            role.permissions = new java.util.HashSet<>();
            role.persist();
            return role;
        }
        
        return null; // Already exists
    }
    
    private void addPermissionsToRole(Role role, String... permissionNames) {
        for (String permissionName : permissionNames) {
            Permission permission = Permission.findByName(permissionName);
            if (permission != null && role.permissions != null) {
                role.permissions.add(permission);
            }
        }
        role.persist();
    }
}
