package com.nuraly.keycloak.client;

import com.nuraly.keycloak.client.model.EmailToUuidMapping;
import com.nuraly.keycloak.client.model.EmailToUuidRequest;
import com.nuraly.keycloak.client.model.EmailToUuidResponse;
import com.nuraly.keycloak.client.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Interface for Keycloak user operations.
 * Provides methods for user lookup, email to UUID mapping, and user management.
 * 
 * Implementations can be local (direct Keycloak API calls) or remote (HTTP API).
 */
public interface KeycloakClient {
    
    /**
     * Get a user by their email address
     * 
     * @param email The user's email address
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> getUserByEmail(String email);
    
    /**
     * Get a user by their UUID
     * 
     * @param id The user's UUID
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> getUserById(String id);
    
    /**
     * Get multiple users by their UUIDs
     * 
     * @param ids List of user UUIDs
     * @return List of users that were found
     */
    List<User> getUsersByIds(List<String> ids);
    
    /**
     * Convert a single email address to user UUID
     * 
     * @param email The email address to convert
     * @return Optional containing the UUID if user found, empty otherwise
     */
    Optional<String> getEmailToUuidMapping(String email);
    
    /**
     * Convert multiple email addresses to user UUIDs
     * 
     * @param emails List of email addresses to convert
     * @return List of UUIDs for found users (in same order as input)
     */
    List<String> getUserIdsByEmails(List<String> emails);
    
    /**
     * Get email to UUID mappings for multiple emails
     * 
     * @param emails List of email addresses
     * @return List of EmailToUuidMapping objects for found users
     */
    List<EmailToUuidMapping> getEmailToUuidMappings(List<String> emails);
    
    /**
     * Batch email to UUID conversion with detailed response
     * 
     * @param request Request containing list of emails to convert
     * @return Response with successful mappings and failed emails
     */
    EmailToUuidResponse convertEmailsToUuids(EmailToUuidRequest request);
    
    /**
     * Check if the Keycloak service is available and reachable
     * 
     * @return true if service is healthy, false otherwise
     */
    boolean isServiceHealthy();
}
