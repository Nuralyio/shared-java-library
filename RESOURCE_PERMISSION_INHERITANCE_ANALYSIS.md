# Resource Permission Inheritance Analysis

## Question
Does the server project allow resource permission inheritance?

## **Conclusion: YES** 

The Nuraly Permissions Server **does support resource permission inheritance** through a hierarchical resource structure. Here's the detailed analysis:

## 🔍 Key Findings

### 1. **Hierarchical Resource Structure**
The `Resource` entity supports parent-child relationships through self-referencing:

```java
// Self-referencing relationship for hierarchical resources
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_resource_id")
@JsonIgnore
public Resource parentResource;

@OneToMany(mappedBy = "parentResource", cascade = CascadeType.ALL)
@JsonIgnore
public Set<Resource> childResources;
```

### 2. **Permission Inheritance Implementation**
The permission inheritance is implemented in the `ACLService.checkInheritedPermissions()` method:

```java
private boolean checkInheritedPermissions(UUID userId, Resource resource, Permission permission, UUID tenantId) {
    Resource parent = resource.parentResource;
    if (parent != null) {
        boolean parentResult = checkUserPermission(userId, parent, permission, tenantId);
        return parentResult;
    }
    
    return false;
}
```

### 3. **Integration with Main Permission Check**
The inheritance check is integrated into the main permission evaluation flow in `checkUserPermission()`:

```java
private boolean checkUserPermission(UUID userId, Resource resource, Permission permission, UUID tenantId) {
    // ... owner and direct grant checks ...
    
    // Check role-based access (tenant-scoped)
    List<UserRole> userRoles = UserRole.findActiveByExternalUserAndTenant(userId, tenantId);
    for (UserRole userRole : userRoles) {
        if (userRole.role != null) {
            if (userRole.role.getAllPermissions().contains(permission)) {
                return true;
            }
        }
    }
    
    // Check inherited permissions from parent resources
    boolean inherited = checkInheritedPermissions(userId, resource, permission, tenantId);
    
    return inherited;
}
```

## 📋 How Resource Inheritance Works

### **Permission Resolution Order:**
1. **Owner Check**: Resource owners automatically have all permissions
2. **Direct Grants**: Explicitly granted permissions on the specific resource
3. **Role-based Access**: Permissions granted through user roles within the tenant
4. ****Inherited Permissions**: Permissions inherited from parent resources** ✅

### **Inheritance Mechanism:**
- If a user has permission on a **parent resource**, they automatically inherit that permission on all **child resources**
- The inheritance is **recursive** - it checks the entire parent chain
- Inheritance respects **tenant isolation** - only works within the same tenant

### **Grant Types Support:**
The system tracks different types of grants including inherited ones:
```java
public enum GrantType {
    DIRECT,     // Explicitly granted to user/role
    INHERITED,  // Inherited from parent role or tenant ✅
    DELEGATED   // Delegated by another user
}
```

## 🏗️ Architecture Summary

The Nuraly Permissions Server implements **hierarchical resource permission inheritance** as one of its advanced features, alongside:

- ✅ **Role-based inheritance** (parent roles → child roles)
- ✅ **Resource hierarchy inheritance** (parent resources → child resources) 
- ✅ **Multi-scope permissions** (Application, Tenant, Resource levels)
- ✅ **Anonymous/Public access**
- ✅ **Time-limited permissions**
- ✅ **Audit logging**

## 🎯 Use Cases

Resource inheritance enables scenarios like:
- **Folder-based permissions**: Grant access to a folder and all contained documents inherit the permission
- **Organizational hierarchies**: Department-level permissions cascade to team-level resources
- **Project structures**: Main project permissions automatically apply to sub-projects and tasks

---

**Date:** July 10, 2025  
**Analysis Status:** Complete ✅
