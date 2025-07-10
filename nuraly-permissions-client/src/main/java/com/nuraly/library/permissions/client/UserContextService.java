package com.nuraly.library.permissions.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

/**
 * Service to extract current user information from request context in client applications
 * Provides a centralized way to get authenticated user details from HTTP headers
 */
@RequestScoped
public class UserContextService {
    
    @Context
    private ContainerRequestContext requestContext;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Get the current authenticated user ID
     * Uses X-USER header (JSON format with uuid field)
     */
    public String getCurrentUserId() {
        String userHeader = requestContext.getHeaderString("X-USER");
        if (userHeader != null && !userHeader.trim().isEmpty()) {
            try {
                JsonNode userNode = objectMapper.readTree(userHeader);
                return userNode.get("uuid").asText();
            } catch (Exception e) {
                // Invalid JSON format
            }
        }
        return null;
    }
    
    /**
     * Get the current tenant ID
     * First tries to extract from X-USER header, then falls back to X-TENANT-ID
     */
    public String getCurrentTenantId() {
        // Try to get tenant ID from X-USER header first
        String userHeader = requestContext.getHeaderString("X-USER");
        if (userHeader != null && !userHeader.trim().isEmpty()) {
            try {
                JsonNode userNode = objectMapper.readTree(userHeader);
                JsonNode tenantIdNode = userNode.get("tenantId");
                if (tenantIdNode != null && !tenantIdNode.isNull()) {
                    return tenantIdNode.asText();
                }
            } catch (Exception e) {
                // Invalid JSON format, continue to fallback
            }
        }
        
        // Fallback to X-TENANT-ID header
        String tenantHeader = requestContext.getHeaderString("X-TENANT-ID");
        if (tenantHeader != null && !tenantHeader.trim().isEmpty()) {
            return tenantHeader;
        }
        return null;
    }
    
    /**
     * Get the current user information as JSON
     */
    public JsonNode getCurrentUser() {
        String userHeader = requestContext.getHeaderString("X-USER");
        if (userHeader != null && !userHeader.trim().isEmpty()) {
            try {
                return objectMapper.readTree(userHeader);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Check if the current request is authenticated
     */
    public boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }
}
