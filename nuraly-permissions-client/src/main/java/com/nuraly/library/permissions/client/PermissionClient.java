package com.nuraly.library.permissions.client;

import com.nuraly.library.permissions.client.model.AccessibleResourcesResponse;
import java.util.List;

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
    
    /**
     * Get list of resource IDs that a user has specific permission for
     * 
     * @param userId User identifier
     * @param permissionType Permission type (read, write, delete, etc.)
     * @param resourceType Resource type filter (optional, can be null for all types)
     * @param tenantId Tenant identifier (optional, can be null)
     * @return List of resource IDs the user has access to
     */
    List<String> getAccessibleResourceIds(String userId, String permissionType, String resourceType, String tenantId);
    
    /**
     * Get detailed information about accessible resources for a user
     * 
     * @param userId User identifier
     * @param permissionType Permission type (read, write, delete, etc.)
     * @param resourceType Resource type filter (optional, can be null for all types)
     * @param tenantId Tenant identifier (optional, can be null)
     * @param limit Maximum number of results to return (optional, 0 for no limit)
     * @param offset Number of results to skip for pagination (optional, 0 for start)
     * @return AccessibleResourcesResponse with resource IDs and metadata
     */
    AccessibleResourcesResponse getAccessibleResources(String userId, String permissionType, 
                                                     String resourceType, String tenantId, 
                                                     int limit, int offset);
    
    /**
     * Check if user has any resources with the specified permission
     * 
     * @param userId User identifier
     * @param permissionType Permission type (read, write, delete, etc.)
     * @param resourceType Resource type filter (optional, can be null for all types)
     * @param tenantId Tenant identifier (optional, can be null)
     * @return true if user has at least one resource with the permission, false otherwise
     */
    boolean hasAnyAccessibleResources(String userId, String permissionType, String resourceType, String tenantId);
}
