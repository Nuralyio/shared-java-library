package com.nuraly.library.acl.model;

public enum AccessLevel {
    READ_ONLY,         // Can only view
    COMMENT_ONLY,      // Can view and comment/annotate
    EDIT,             // Can view and edit
    FULL_ACCESS,      // Can view, edit, and share
    ADMIN             // Full administrative access
}
