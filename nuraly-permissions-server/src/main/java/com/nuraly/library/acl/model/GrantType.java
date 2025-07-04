package com.nuraly.library.acl.model;

public enum GrantType {
    DIRECT,     // Explicitly granted to user/role
    INHERITED,  // Inherited from parent role or tenant
    DELEGATED   // Delegated by another user
}
