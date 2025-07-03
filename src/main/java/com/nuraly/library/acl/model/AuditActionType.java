package com.nuraly.library.acl.model;

public enum AuditActionType {
    // Permission actions
    PERMISSION_GRANTED,
    PERMISSION_REVOKED,
    PERMISSION_DELEGATED,
    
    // Role actions
    ROLE_ASSIGNED,
    ROLE_REMOVED,
    ROLE_CREATED,
    ROLE_MODIFIED,
    ROLE_DELETED,
    
    // Resource actions
    RESOURCE_CREATED,
    RESOURCE_DELETED,
    RESOURCE_PUBLISHED,
    RESOURCE_UNPUBLISHED,
    RESOURCE_SHARED,
    
    // Access attempts
    ACCESS_ATTEMPT,
    ACCESS_DENIED,
    ANONYMOUS_ACCESS,
    
    // Organization actions
    ORGANIZATION_CREATED,
    ORGANIZATION_DELETED,
    MEMBER_ADDED,
    MEMBER_REMOVED,
    MEMBER_ROLE_CHANGED,
    
    // Authentication
    LOGIN,
    LOGOUT,
    LOGIN_FAILED
}
