package com.nuraly.library.permissions.client.model;

import java.util.Objects;

/**
 * Request model for updating an existing resource in the permissions system.
 * Used to update resource properties including name, description, and parent relationships.
 */
public class UpdateResourceRequest {
    
    private String name;
    private String description;
    private String parentResourceId; // For changing parent relationships
    
    public UpdateResourceRequest() {
    }
    
    public UpdateResourceRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public UpdateResourceRequest(String name, String description, String parentResourceId) {
        this.name = name;
        this.description = description;
        this.parentResourceId = parentResourceId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public UpdateResourceRequest withName(String name) {
        this.name = name;
        return this;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public UpdateResourceRequest withDescription(String description) {
        this.description = description;
        return this;
    }
    
    public String getParentResourceId() {
        return parentResourceId;
    }
    
    public void setParentResourceId(String parentResourceId) {
        this.parentResourceId = parentResourceId;
    }
    
    public UpdateResourceRequest withParentResourceId(String parentResourceId) {
        this.parentResourceId = parentResourceId;
        return this;
    }
    
    /**
     * Helper method to clear the parent resource (set to null)
     */
    public UpdateResourceRequest withoutParent() {
        this.parentResourceId = null;
        return this;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateResourceRequest that = (UpdateResourceRequest) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(parentResourceId, that.parentResourceId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, description, parentResourceId);
    }
    
    @Override
    public String toString() {
        return "UpdateResourceRequest{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", parentResourceId='" + parentResourceId + '\'' +
                '}';
    }
}
