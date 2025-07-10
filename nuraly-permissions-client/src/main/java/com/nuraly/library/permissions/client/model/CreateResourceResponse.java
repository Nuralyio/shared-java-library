package com.nuraly.library.permissions.client.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;

/**
 * Response object for resource creation operations.
 * Contains the created resource information and metadata.
 */
public class CreateResourceResponse {
    
    @JsonProperty("resourceId")
    @JsonAlias("id")
    private String resourceId;
    
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
    
    @JsonProperty("isPublic")
    private Boolean isPublic;
    
    @JsonProperty("publicPermissions")
    private String publicPermissions;
    
    @JsonProperty("publicLinkToken")
    private String publicLinkToken;
    
    @JsonProperty("publicLinkExpiresAt")
    private String publicLinkExpiresAt;
    
    @JsonProperty("publicLinkValid")
    private Boolean publicLinkValid;
    
    @JsonProperty("isActive")
    private Boolean isActive;
    
    @JsonProperty("createdAt")
    private String createdAt;
    
    @JsonProperty("updatedAt")
    private String updatedAt;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("created")
    private boolean created;
    
    @JsonProperty("message")
    private String message;
    
    // Default constructor for JSON deserialization
    public CreateResourceResponse() {
    }
    
    public CreateResourceResponse(String resourceId, String resourceType, String tenantId, String ownerId, boolean created) {
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.tenantId = tenantId;
        this.ownerId = ownerId;
        this.created = created;
    }
    
    public CreateResourceResponse(String resourceId, String name, String resourceType, String tenantId, String ownerId, boolean created) {
        this.resourceId = resourceId;
        this.name = name;
        this.resourceType = resourceType;
        this.tenantId = tenantId;
        this.ownerId = ownerId;
        this.created = created;
    }
    
    public CreateResourceResponse(String resourceId, String name, String description, String resourceType, 
                                String externalId, String tenantId, String ownerId, Boolean isPublic, 
                                Boolean isActive, boolean created) {
        this.resourceId = resourceId;
        this.name = name;
        this.description = description;
        this.resourceType = resourceType;
        this.externalId = externalId;
        this.tenantId = tenantId;
        this.ownerId = ownerId;
        this.isPublic = isPublic;
        this.isActive = isActive;
        this.created = created;
    }
    
    // Getters and setters
    public String getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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
    
    public String getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getExternalId() {
        return externalId;
    }
    
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public String getPublicPermissions() {
        return publicPermissions;
    }
    
    public void setPublicPermissions(String publicPermissions) {
        this.publicPermissions = publicPermissions;
    }
    
    public String getPublicLinkToken() {
        return publicLinkToken;
    }
    
    public void setPublicLinkToken(String publicLinkToken) {
        this.publicLinkToken = publicLinkToken;
    }
    
    public String getPublicLinkExpiresAt() {
        return publicLinkExpiresAt;
    }
    
    public void setPublicLinkExpiresAt(String publicLinkExpiresAt) {
        this.publicLinkExpiresAt = publicLinkExpiresAt;
    }
    
    public Boolean getPublicLinkValid() {
        return publicLinkValid;
    }
    
    public void setPublicLinkValid(Boolean publicLinkValid) {
        this.publicLinkValid = publicLinkValid;
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
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public boolean isCreated() {
        return created;
    }
    
    public void setCreated(boolean created) {
        this.created = created;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CreateResourceResponse)) return false;
        CreateResourceResponse that = (CreateResourceResponse) o;
        return created == that.created &&
               Objects.equals(resourceId, that.resourceId) &&
               Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(resourceType, that.resourceType) &&
               Objects.equals(externalId, that.externalId) &&
               Objects.equals(tenantId, that.tenantId) &&
               Objects.equals(ownerId, that.ownerId) &&
               Objects.equals(isPublic, that.isPublic) &&
               Objects.equals(publicPermissions, that.publicPermissions) &&
               Objects.equals(publicLinkToken, that.publicLinkToken) &&
               Objects.equals(publicLinkExpiresAt, that.publicLinkExpiresAt) &&
               Objects.equals(publicLinkValid, that.publicLinkValid) &&
               Objects.equals(isActive, that.isActive) &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(updatedAt, that.updatedAt) &&
               Objects.equals(metadata, that.metadata) &&
               Objects.equals(message, that.message);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(resourceId, name, description, resourceType, externalId, tenantId, ownerId, 
                          isPublic, publicPermissions, publicLinkToken, publicLinkExpiresAt, publicLinkValid, 
                          isActive, createdAt, updatedAt, metadata, created, message);
    }
    
    @Override
    public String toString() {
        return "CreateResourceResponse{" +
               "resourceId='" + resourceId + '\'' +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", resourceType='" + resourceType + '\'' +
               ", externalId='" + externalId + '\'' +
               ", tenantId='" + tenantId + '\'' +
               ", ownerId='" + ownerId + '\'' +
               ", isPublic=" + isPublic +
               ", publicPermissions='" + publicPermissions + '\'' +
               ", publicLinkToken='" + publicLinkToken + '\'' +
               ", publicLinkExpiresAt='" + publicLinkExpiresAt + '\'' +
               ", publicLinkValid=" + publicLinkValid +
               ", isActive=" + isActive +
               ", createdAt='" + createdAt + '\'' +
               ", updatedAt='" + updatedAt + '\'' +
               ", metadata=" + metadata +
               ", created=" + created +
               ", message='" + message + '\'' +
               '}';
    }
}
