package com.nuraly.keycloak.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Simplified User representation for Keycloak client operations.
 * Contains essential user information needed by client applications.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("firstName")
    private String firstName;
    
    @JsonProperty("lastName")
    private String lastName;
    
    @JsonProperty("enabled")
    private Boolean enabled;
    
    @JsonProperty("emailVerified")
    private Boolean emailVerified;
    
    @JsonProperty("attributes")
    private Map<String, List<String>> attributes;
    
    public User() {
    }
    
    public User(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public User withId(String id) {
        this.id = id;
        return this;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public User withUsername(String username) {
        this.username = username;
        return this;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public User withEmail(String email) {
        this.email = email;
        return this;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public User withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public User withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public User withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public User withEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
        return this;
    }
    
    public Map<String, List<String>> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }
    
    public User withAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
        return this;
    }
    
    /**
     * Get the full name by combining first and last name
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return username;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", enabled=" + enabled +
                ", emailVerified=" + emailVerified +
                '}';
    }
}
