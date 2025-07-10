package com.nuraly.library.permissions.client;

import com.nuraly.library.permissions.client.model.AccessibleResourcesResponse;
import com.nuraly.library.permissions.client.model.CreateResourceRequest;
import com.nuraly.library.permissions.client.model.CreateResourceResponse;
import java.util.List;

/**
 * Interface for permission checking and resource management operations.
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
     * Check if the current authenticated user has permission on a resource.
     * Automatically extracts user and tenant context from the current request.
     * 
     * @param permissionType Permission type (read, write, delete, etc.)
     * @param resourceId Resource identifier 
     * @return true if current user has permission, false otherwise
     */
    boolean hasPermission(String permissionType, String resourceId);
    
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
     * Get list of resource IDs that the current authenticated user has specific permission for.
     * Automatically extracts user and tenant context from the current request.
     * 
     * @param permissionType Permission type (read, write, delete, etc.)
     * @param resourceType Resource type filter (optional, can be null for all types)
     * @return List of resource IDs the current user has access to
     */
    List<String> getAccessibleResourceIds(String permissionType, String resourceType);
    
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
     * Get detailed information about accessible resources for the current authenticated user.
     * Automatically extracts user and tenant context from the current request.
     * 
     * @param permissionType Permission type (read, write, delete, etc.)
     * @param resourceType Resource type filter (optional, can be null for all types)
     * @param limit Maximum number of results to return (optional, 0 for no limit)
     * @param offset Number of results to skip for pagination (optional, 0 for start)
     * @return AccessibleResourcesResponse with resource IDs and metadata
     */
    AccessibleResourcesResponse getAccessibleResources(String permissionType, String resourceType, int limit, int offset);
    
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
    
    /**
     * Check if the current authenticated user has any resources with the specified permission.
     * Automatically extracts user and tenant context from the current request.
     * 
     * @param permissionType Permission type (read, write, delete, etc.)
     * @param resourceType Resource type filter (optional, can be null for all types)
     * @return true if current user has at least one resource with the permission, false otherwise
     */
    boolean hasAnyAccessibleResources(String permissionType, String resourceType);
    
    /**
     * Create a new resource with automatic owner permissions.
     * 
     * NOTE: This method is currently not supported by the ACL server implementation.
     * The ACL server is designed to manage permissions for resources that are created
     * and managed by external systems. Resources should be registered with the ACL
     * system after being created in your application.
     * 
     * @param request CreateResourceRequest containing resource details and context
     * @return CreateResourceResponse with the created resource information
     * @throws UnsupportedOperationException for HTTP-based implementations
     * @throws RuntimeException if resource creation fails (for other implementations)
     */
    CreateResourceResponse createResource(CreateResourceRequest request);
}
