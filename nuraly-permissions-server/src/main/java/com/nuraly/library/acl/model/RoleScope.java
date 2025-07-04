package com.nuraly.library.acl.model;

public enum RoleScope {
    APPLICATION,    // Global application-level permissions
    TENANT,         // Tenant-scoped permissions (replaces organization-scoped)
    RESOURCE       // Resource-specific permissions
}
