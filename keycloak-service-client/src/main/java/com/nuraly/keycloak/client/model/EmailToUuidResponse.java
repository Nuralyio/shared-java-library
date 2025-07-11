package com.nuraly.keycloak.client.model;

import java.util.List;
import java.util.Objects;

/**
 * Response model for batch email to UUID lookup operations.
 * Contains the list of successful mappings and any failed emails.
 */
public class EmailToUuidResponse {
    
    private List<EmailToUuidMapping> mappings;
    private List<String> failedEmails;
    
    public EmailToUuidResponse() {
    }
    
    public EmailToUuidResponse(List<EmailToUuidMapping> mappings, List<String> failedEmails) {
        this.mappings = mappings;
        this.failedEmails = failedEmails;
    }
    
    public List<EmailToUuidMapping> getMappings() {
        return mappings;
    }
    
    public void setMappings(List<EmailToUuidMapping> mappings) {
        this.mappings = mappings;
    }
    
    public EmailToUuidResponse withMappings(List<EmailToUuidMapping> mappings) {
        this.mappings = mappings;
        return this;
    }
    
    public List<String> getFailedEmails() {
        return failedEmails;
    }
    
    public void setFailedEmails(List<String> failedEmails) {
        this.failedEmails = failedEmails;
    }
    
    public EmailToUuidResponse withFailedEmails(List<String> failedEmails) {
        this.failedEmails = failedEmails;
        return this;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailToUuidResponse that = (EmailToUuidResponse) o;
        return Objects.equals(mappings, that.mappings) && Objects.equals(failedEmails, that.failedEmails);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(mappings, failedEmails);
    }
    
    @Override
    public String toString() {
        return "EmailToUuidResponse{" +
                "mappings=" + mappings +
                ", failedEmails=" + failedEmails +
                '}';
    }
}
