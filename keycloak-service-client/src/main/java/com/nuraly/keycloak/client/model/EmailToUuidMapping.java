package com.nuraly.keycloak.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Email to UUID mapping model for user lookup operations.
 * Used when converting email addresses to Keycloak user UUIDs.
 */
public class EmailToUuidMapping {
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("uuid")
    private String uuid;
    
    public EmailToUuidMapping() {
    }
    
    public EmailToUuidMapping(String email, String uuid) {
        this.email = email;
        this.uuid = uuid;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public EmailToUuidMapping withEmail(String email) {
        this.email = email;
        return this;
    }
    
    public String getUuid() {
        return uuid;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public EmailToUuidMapping withUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailToUuidMapping that = (EmailToUuidMapping) o;
        return Objects.equals(email, that.email) && Objects.equals(uuid, that.uuid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(email, uuid);
    }
    
    @Override
    public String toString() {
        return "EmailToUuidMapping{" +
                "email='" + email + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
