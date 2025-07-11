package com.nuraly.keycloak.client.model;

import java.util.List;
import java.util.Objects;

/**
 * Request model for batch email to UUID lookup operations.
 * Used when needing to convert multiple email addresses to user UUIDs.
 */
public class EmailToUuidRequest {
    
    private List<String> emails;
    
    public EmailToUuidRequest() {
    }
    
    public EmailToUuidRequest(List<String> emails) {
        this.emails = emails;
    }
    
    public List<String> getEmails() {
        return emails;
    }
    
    public void setEmails(List<String> emails) {
        this.emails = emails;
    }
    
    public EmailToUuidRequest withEmails(List<String> emails) {
        this.emails = emails;
        return this;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailToUuidRequest that = (EmailToUuidRequest) o;
        return Objects.equals(emails, that.emails);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(emails);
    }
    
    @Override
    public String toString() {
        return "EmailToUuidRequest{" +
                "emails=" + emails +
                '}';
    }
}
