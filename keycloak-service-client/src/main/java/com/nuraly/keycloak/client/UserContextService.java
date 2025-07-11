package com.nuraly.keycloak.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service to extract current user information from request context in client applications.
 * Provides a centralized way to get authenticated user details from HTTP headers.
 */
@RequestScoped
public class UserContextService {
    
    private static final Logger LOG = Logger.getLogger(UserContextService.class.getName());
    
    @Context
    private ContainerRequestContext requestContext;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Get the current authenticated user ID from X-USER header
     * Expected format: {"uuid": "user-uuid", "email": "user@example.com", ...}
     */
    public Optional<String> getCurrentUserId() {
        try {
            String userHeader = requestContext.getHeaderString("X-USER");
            if (userHeader == null || userHeader.trim().isEmpty()) {
                LOG.fine("No X-USER header found in request");
                return Optional.empty();
            }
            
            JsonNode userNode = objectMapper.readTree(userHeader);
            JsonNode uuidNode = userNode.get("uuid");
            
            if (uuidNode != null && !uuidNode.isNull()) {
                String uuid = uuidNode.asText();
                LOG.fine("Extracted user ID from header: " + uuid);
                return Optional.of(uuid);
            }
            
            LOG.warning("X-USER header present but missing 'uuid' field");
            return Optional.empty();
        } catch (Exception e) {
            LOG.warning("Failed to parse X-USER header: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Get the current authenticated user email from X-USER header
     */
    public Optional<String> getCurrentUserEmail() {
        try {
            String userHeader = requestContext.getHeaderString("X-USER");
            if (userHeader == null || userHeader.trim().isEmpty()) {
                return Optional.empty();
            }
            
            JsonNode userNode = objectMapper.readTree(userHeader);
            JsonNode emailNode = userNode.get("email");
            
            if (emailNode != null && !emailNode.isNull()) {
                String email = emailNode.asText();
                LOG.fine("Extracted user email from header: " + email);
                return Optional.of(email);
            }
            
            return Optional.empty();
        } catch (Exception e) {
            LOG.warning("Failed to parse X-USER header for email: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Get the current tenant ID from X-TENANT header
     */
    public Optional<String> getCurrentTenantId() {
        try {
            String tenantHeader = requestContext.getHeaderString("X-TENANT");
            if (tenantHeader == null || tenantHeader.trim().isEmpty()) {
                LOG.fine("No X-TENANT header found in request");
                return Optional.empty();
            }
            
            LOG.fine("Extracted tenant ID from header: " + tenantHeader);
            return Optional.of(tenantHeader.trim());
        } catch (Exception e) {
            LOG.warning("Failed to get X-TENANT header: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Check if the current request is from an authenticated user
     */
    public boolean isAuthenticated() {
        return getCurrentUserId().isPresent();
    }
    
    /**
     * Get the raw X-USER header content
     */
    public Optional<String> getRawUserHeader() {
        String userHeader = requestContext.getHeaderString("X-USER");
        return Optional.ofNullable(userHeader);
    }
    
    /**
     * Get a custom field from the X-USER header JSON
     */
    public Optional<String> getUserHeaderField(String fieldName) {
        try {
            String userHeader = requestContext.getHeaderString("X-USER");
            if (userHeader == null || userHeader.trim().isEmpty()) {
                return Optional.empty();
            }
            
            JsonNode userNode = objectMapper.readTree(userHeader);
            JsonNode fieldNode = userNode.get(fieldName);
            
            if (fieldNode != null && !fieldNode.isNull()) {
                return Optional.of(fieldNode.asText());
            }
            
            return Optional.empty();
        } catch (Exception e) {
            LOG.warning("Failed to get field '" + fieldName + "' from X-USER header: " + e.getMessage());
            return Optional.empty();
        }
    }
}
