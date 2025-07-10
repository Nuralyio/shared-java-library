package com.nuraly.library.permissions.client.model;

import java.util.Objects;

/**
 * Request model for setting a parent resource for an existing resource.
 * Used specifically for parent relationship management operations.
 */
public class SetParentResourceRequest {
    
    private String parentResourceId;
    private String reason; // Optional reason for the change
    
    public SetParentResourceRequest() {
    }
    
    public SetParentResourceRequest(String parentResourceId) {
        this.parentResourceId = parentResourceId;
    }
    
    public SetParentResourceRequest(String parentResourceId, String reason) {
        this.parentResourceId = parentResourceId;
        this.reason = reason;
    }
    
    public String getParentResourceId() {
        return parentResourceId;
    }
    
    public void setParentResourceId(String parentResourceId) {
        this.parentResourceId = parentResourceId;
    }
    
    public SetParentResourceRequest withParentResourceId(String parentResourceId) {
        this.parentResourceId = parentResourceId;
        return this;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public SetParentResourceRequest withReason(String reason) {
        this.reason = reason;
        return this;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetParentResourceRequest that = (SetParentResourceRequest) o;
        return Objects.equals(parentResourceId, that.parentResourceId) &&
               Objects.equals(reason, that.reason);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(parentResourceId, reason);
    }
    
    @Override
    public String toString() {
        return "SetParentResourceRequest{" +
                "parentResourceId='" + parentResourceId + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}
