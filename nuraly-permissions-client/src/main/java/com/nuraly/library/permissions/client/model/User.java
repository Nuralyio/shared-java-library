package com.nuraly.library.permissions.client.model;

import java.util.Objects;

/**
 * User model for permission operations.
 * Represents a user with their ID and tenant context.
 */
public class User {
    
    private String uuid;
    private String tenantId;
    private String name;
    
    public User() {
    }
    
    public User(String uuid, String tenantId) {
        this.uuid = uuid;
        this.tenantId = tenantId;
    }
    
    public User(String uuid, String tenantId, String name) {
        this.uuid = uuid;
        this.tenantId = tenantId;
        this.name = name;
    }
    
    public String getUuid() {
        return uuid;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(uuid, user.uuid) &&
               Objects.equals(tenantId, user.tenantId) &&
               Objects.equals(name, user.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uuid, tenantId, name);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "uuid='" + uuid + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
