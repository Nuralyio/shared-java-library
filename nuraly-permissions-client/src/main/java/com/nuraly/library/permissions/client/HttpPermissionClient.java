package com.nuraly.library.permissions.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP-based implementation of PermissionClient.
 * Makes REST API calls to the permissions service.
 */
@ApplicationScoped
public class HttpPermissionClient implements PermissionClient {
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    // Configuration - can be injected or set via system properties
    private final String permissionsServiceUrl;
    private final int timeoutSeconds;
    
    public HttpPermissionClient() {
        this(
            System.getProperty("nuraly.permissions.service.url", "http://localhost:8080"),
            Integer.parseInt(System.getProperty("nuraly.permissions.client.timeout.seconds", "5"))
        );
    }
    
    public HttpPermissionClient(String permissionsServiceUrl, int timeoutSeconds) {
        this.permissionsServiceUrl = permissionsServiceUrl;
        this.timeoutSeconds = timeoutSeconds;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(timeoutSeconds))
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public boolean hasPermission(String userId, String permissionType, String resourceId, String tenantId) {
        try {
            Map<String, Object> request = createPermissionRequest(userId, permissionType, resourceId, tenantId);
            return makePermissionRequest("/api/permissions/check", request);
        } catch (Exception e) {
            // Log error in production
            System.err.println("Permission check failed: " + e.getMessage());
            return false; // Fail closed - deny access on error
        }
    }
    
    @Override
    public boolean hasAnonymousPermission(String resourceId, String permissionType, String tenantId) {
        try {
            Map<String, Object> request = createAnonymousRequest(resourceId, permissionType, tenantId);
            return makePermissionRequest("/api/permissions/check-anonymous", request);
        } catch (Exception e) {
            System.err.println("Anonymous permission check failed: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean validatePublicLink(String token, String permissionType) {
        try {
            Map<String, Object> request = createPublicLinkRequest(token, permissionType);
            return makePermissionRequest("/api/permissions/validate-public-link", request);
        } catch (Exception e) {
            System.err.println("Public link validation failed: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean isHealthy() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(permissionsServiceUrl + "/api/permissions/health"))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .GET()
                .build();
                
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean makePermissionRequest(String endpoint, Map<String, Object> requestBody) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(permissionsServiceUrl + endpoint))
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
            
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // 200 = permission granted, 403 = permission denied, others = error
        return response.statusCode() == 200;
    }
    
    private Map<String, Object> createPermissionRequest(String userId, String permissionType, String resourceId, String tenantId) {
        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);
        request.put("permissionType", permissionType);
        request.put("resourceId", resourceId);
        if (tenantId != null) {
            request.put("tenantId", tenantId);
        }
        return request;
    }
    
    private Map<String, Object> createAnonymousRequest(String resourceId, String permissionType, String tenantId) {
        Map<String, Object> request = new HashMap<>();
        request.put("resourceId", resourceId);
        request.put("permissionType", permissionType);
        if (tenantId != null) {
            request.put("tenantId", tenantId);
        }
        return request;
    }
    
    private Map<String, Object> createPublicLinkRequest(String token, String permissionType) {
        Map<String, Object> request = new HashMap<>();
        request.put("token", token);
        request.put("permissionType", permissionType);
        return request;
    }
}
