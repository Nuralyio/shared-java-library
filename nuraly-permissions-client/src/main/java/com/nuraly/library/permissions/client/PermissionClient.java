package com.nuraly.library.permissions.client;

/**
 * Interface for permission checking operations.
 * Implementations can be local (direct database) or remote (HTTP API).
 */
public interface PermissionClient {
    
    /**
     * Check if user has permission on a resource
     * 
     * @param userId User identifier
     * @param permissionType Permission type (read, write, delete, etc.)
     * @param resourceId Resource identifier 
     * @param tenantId Tenant identifier (optional, can be null)
     * @return true if user has permission, false otherwise
     */
    boolean hasPermission(String userId, String permissionType, String resourceId, String tenantId);
    
    /**
     * Check if anonymous access is allowed for a resource
     * 
     * @param resourceId Resource identifier
     * @param permissionType Permission type
     * @param tenantId Tenant identifier (optional, can be null)
     * @return true if anonymous access allowed, false otherwise
     */
    boolean hasAnonymousPermission(String resourceId, String permissionType, String tenantId);
    
    /**
     * Validate public link access token
     * 
     * @param token Public access token
     * @param permissionType Required permission type
     * @return true if token is valid for the permission, false otherwise
     */
    boolean validatePublicLink(String token, String permissionType);
    
    /**
     * Health check for the permission service
     * 
     * @return true if permission service is available, false otherwise
     */
    default boolean isHealthy() {
        return true;
    }
}
