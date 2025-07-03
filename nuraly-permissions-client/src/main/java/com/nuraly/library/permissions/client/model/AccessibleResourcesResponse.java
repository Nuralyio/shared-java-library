package com.nuraly.library.permissions.client.model;

import java.util.List;
import java.util.Objects;

/**
 * Response containing accessible resource IDs for a user.
 * Used when querying which resources a user has specific permissions for.
 */
public class AccessibleResourcesResponse {
    
    private List<String> resourceIds;
    private String permissionType;
    private String resourceType;
    private String tenantId;
    private int totalCount;
    
    public AccessibleResourcesResponse() {
    }
    
    public AccessibleResourcesResponse(List<String> resourceIds, String permissionType, 
                                     String resourceType, String tenantId, int totalCount) {
        this.resourceIds = resourceIds;
        this.permissionType = permissionType;
        this.resourceType = resourceType;
        this.tenantId = tenantId;
        this.totalCount = totalCount;
    }
    
    public List<String> getResourceIds() {
        return resourceIds;
    }
    
    public void setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds;
    }
    
    public String getPermissionType() {
        return permissionType;
    }
    
    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public int getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessibleResourcesResponse that = (AccessibleResourcesResponse) o;
        return totalCount == that.totalCount && 
               Objects.equals(resourceIds, that.resourceIds) &&
               Objects.equals(permissionType, that.permissionType) &&
               Objects.equals(resourceType, that.resourceType) &&
               Objects.equals(tenantId, that.tenantId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(resourceIds, permissionType, resourceType, tenantId, totalCount);
    }
    
    @Override
    public String toString() {
        return "AccessibleResourcesResponse{" +
                "resourceIds=" + resourceIds +
                ", permissionType='" + permissionType + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", totalCount=" + totalCount +
                '}';
    }
}
