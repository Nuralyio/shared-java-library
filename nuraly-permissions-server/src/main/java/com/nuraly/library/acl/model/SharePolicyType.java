package com.nuraly.library.acl.model;

public enum SharePolicyType {
    PUBLIC_LINK,        // Shareable link access
    TENANT,            // Tenant-wide access
    TEAM,              // Team-based access
    INDIVIDUAL,        // Individual user access
    ANONYMOUS,         // Anonymous access
    TIME_LIMITED,      // Time-limited access
    USAGE_LIMITED      // Usage-limited access
}
