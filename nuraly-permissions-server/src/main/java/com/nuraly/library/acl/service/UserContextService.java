package com.nuraly.library.acl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

import java.util.UUID;

/**
 * Service to extract current user information from request context
 * Provides a centralized way to get authenticated user details
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
    public UUID getCurrentUserId() {
        // Try X-USER header (JSON format)
        String userHeader = requestContext.getHeaderString("X-USER");
        if (userHeader != null && !userHeader.trim().isEmpty()) {
            try {
                JsonNode userNode = objectMapper.readTree(userHeader);
                String uuid = userNode.get("uuid").asText();
                return UUID.fromString(uuid);
            } catch (Exception e) {
                // Invalid JSON or UUID format
            }
        }
        
        return null; // No authenticated user
    }
    
    /**
     * Get the current tenant ID
     * First tries to extract from X-USER header, then falls back to X-TENANT-ID
     */
    public UUID getCurrentTenantId() {
        // Try to get tenant ID from X-USER header first
        String userHeader = requestContext.getHeaderString("X-USER");
        if (userHeader != null && !userHeader.trim().isEmpty()) {
            try {
                JsonNode userNode = objectMapper.readTree(userHeader);
                JsonNode tenantIdNode = userNode.get("tenantId");
                if (tenantIdNode != null && !tenantIdNode.isNull()) {
                    return UUID.fromString(tenantIdNode.asText());
                }
            } catch (Exception e) {
                // Invalid JSON or UUID format, continue to fallback
            }
        }
        
        // Fallback to X-TENANT-ID header for backward compatibility
        String tenantHeader = requestContext.getHeaderString("X-TENANT-ID");
        if (tenantHeader != null && !tenantHeader.trim().isEmpty()) {
            try {
                return UUID.fromString(tenantHeader);
            } catch (Exception e) {
                // Invalid UUID format
            }
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
    
    /**
     * Check if the request is in test mode
     */
    public boolean isTestMode() {
        return "true".equals(requestContext.getHeaderString("X-Test-Mode"));
    }
    
    /**
     * Get public token if present
     */
    public String getPublicToken() {
        return requestContext.getHeaderString("X-PUBLIC-TOKEN");
    }
}
