package com.nuraly.library.permissions.client.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Response object representing a resource in the permissions system.
 * Used for child resource lists and general resource information.
 */
public class ResourceResponse {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("resourceType")
    private String resourceType;
    
    @JsonProperty("externalId")
    private String externalId;
    
    @JsonProperty("tenantId")
    @JsonAlias("externalTenantId")
    private String tenantId;
    
    @JsonProperty("ownerId")
    private String ownerId;
    
    @JsonProperty("parentResourceId")
    private String parentResourceId;
    
    @JsonProperty("isPublic")
    private Boolean isPublic;
    
    @JsonProperty("isActive")
    private Boolean isActive;
    
    @JsonProperty("createdAt")
    private String createdAt;
    
    @JsonProperty("updatedAt")
    private String updatedAt;
    
    public ResourceResponse() {
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String getExternalId() {
        return externalId;
    }
    
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public String getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    
    public String getParentResourceId() {
        return parentResourceId;
    }
    
    public void setParentResourceId(String parentResourceId) {
        this.parentResourceId = parentResourceId;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceResponse that = (ResourceResponse) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(resourceType, that.resourceType) &&
               Objects.equals(externalId, that.externalId) &&
               Objects.equals(tenantId, that.tenantId) &&
               Objects.equals(ownerId, that.ownerId) &&
               Objects.equals(parentResourceId, that.parentResourceId) &&
               Objects.equals(isPublic, that.isPublic) &&
               Objects.equals(isActive, that.isActive) &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(updatedAt, that.updatedAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, resourceType, externalId, tenantId, 
                          ownerId, parentResourceId, isPublic, isActive, createdAt, updatedAt);
    }
    
    @Override
    public String toString() {
        return "ResourceResponse{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", externalId='" + externalId + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", parentResourceId='" + parentResourceId + '\'' +
                ", isPublic=" + isPublic +
                ", isActive=" + isActive +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
