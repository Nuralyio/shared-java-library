package com.nuraly.library.permissions.client.model;

import com.nuraly.library.permissions.client.UserContextService;
import jakarta.enterprise.inject.spi.CDI;
import java.util.Map;
import java.util.Objects;

/**
 * Request model for creating a new resource in the permissions system.
 * Used when registering resources after they are created in the application.
 * Automatically extracts user context from HTTP request headers.
 */
public class CreateResourceRequest {
    
    private String resourceId;
    private String name;
    private String resourceType;
    private String externalId; // Maps to the server's externalId field
    private String tenantId;
    private String ownerId;
    private String parentResourceId; // For hierarchical resources
    private Map<String, Object> metadata;
    
    public CreateResourceRequest() {
    }
    
    public CreateResourceRequest(String resourceId, String resourceType, String tenantId, String ownerId) {
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.tenantId = tenantId;
        this.ownerId = ownerId;
        this.externalId = resourceId; // Default to using resourceId as externalId for backward compatibility
    }
    
    public CreateResourceRequest(String resourceId, String name, String resourceType, String tenantId, String ownerId) {
        this.resourceId = resourceId;
        this.name = name;
        this.resourceType = resourceType;
        this.tenantId = tenantId;
        this.ownerId = ownerId;
        this.externalId = resourceId; // Default to using resourceId as externalId for backward compatibility
    }
    
    public CreateResourceRequest(String resourceId, String name, String resourceType, String externalId, String tenantId, String ownerId) {
        this.resourceId = resourceId;
        this.name = name;
        this.resourceType = resourceType;
        this.externalId = externalId;
        this.tenantId = tenantId;
        this.ownerId = ownerId;
    }
    
    public CreateResourceRequest(String resourceId, String name, String resourceType, String externalId, String tenantId, String ownerId, String parentResourceId) {
        this.resourceId = resourceId;
        this.name = name;
        this.resourceType = resourceType;
        this.externalId = externalId;
        this.tenantId = tenantId;
        this.ownerId = ownerId;
        this.parentResourceId = parentResourceId;
    }
    
    /**
     * Create a new CreateResourceRequest that automatically extracts user context
     * from the current HTTP request headers
     */
    public static CreateResourceRequest forResource(String resourceId, String name, String resourceType) {
        try {
            UserContextService userContext = CDI.current().select(UserContextService.class).get();
            String userId = userContext.getCurrentUserId();
            String tenantId = userContext.getCurrentTenantId();
            
            if (userId == null) {
                throw new IllegalStateException("No authenticated user found in request context");
            }
            
            return new CreateResourceRequest()
                .withResourceId(resourceId)
                .withName(name)
                .withResourceType(resourceType)
                .withExternalId(resourceId) // Default to using resourceId as externalId
                .withOwnerId(userId)
                .withTenantId(tenantId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract user context from request", e);
        }
    }
    
    /**
     * Create a new CreateResourceRequest extracting user context from UserContextService
     * This method automatically gets the current user and tenant from the request context
     */
    public static CreateResourceRequest fromCurrentUser(String resourceId, String name, String resourceType, UserContextService userContextService) {
        String userId = userContextService.getCurrentUserId();
        String tenantId = userContextService.getCurrentTenantId();
        
        if (userId == null) {
            throw new IllegalStateException("No authenticated user found in request context");
        }
        
        return new CreateResourceRequest()
            .withResourceId(resourceId)
            .withName(name)
            .withResourceType(resourceType)
            .withExternalId(resourceId) // Default to using resourceId as externalId
            .withOwnerId(userId)
            .withTenantId(tenantId);
    }
    
    public String getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    
    public CreateResourceRequest withResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public CreateResourceRequest withName(String name) {
        this.name = name;
        return this;
    }
    
    public String getExternalId() {
        return externalId;
    }
    
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    
    public CreateResourceRequest withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public CreateResourceRequest withResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public CreateResourceRequest withTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }
    
    public String getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    
    public CreateResourceRequest withOwnerId(String ownerId) {
        this.ownerId = ownerId;
        return this;
    }
    
    public String getParentResourceId() {
        return parentResourceId;
    }
    
    public void setParentResourceId(String parentResourceId) {
        this.parentResourceId = parentResourceId;
    }
    
    public CreateResourceRequest withParentResourceId(String parentResourceId) {
        this.parentResourceId = parentResourceId;
        return this;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public CreateResourceRequest withMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }
    
    /**
     * Sets both ownerId and tenantId from the UserContextService
     */
    public CreateResourceRequest withCurrentUser(UserContextService userContextService) {
        this.ownerId = userContextService.getCurrentUserId();
        this.tenantId = userContextService.getCurrentTenantId();
        
        if (this.ownerId == null) {
            throw new IllegalStateException("No authenticated user found in request context");
        }
        
        return this;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateResourceRequest that = (CreateResourceRequest) o;
        return Objects.equals(resourceId, that.resourceId) &&
               Objects.equals(name, that.name) &&
               Objects.equals(resourceType, that.resourceType) &&
               Objects.equals(externalId, that.externalId) &&
               Objects.equals(tenantId, that.tenantId) &&
               Objects.equals(ownerId, that.ownerId) &&
               Objects.equals(parentResourceId, that.parentResourceId) &&
               Objects.equals(metadata, that.metadata);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(resourceId, name, resourceType, externalId, tenantId, ownerId, parentResourceId, metadata);
    }
    
    @Override
    public String toString() {
        return "CreateResourceRequest{" +
                "resourceId='" + resourceId + '\'' +
                ", name='" + name + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", externalId='" + externalId + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", parentResourceId='" + parentResourceId + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
