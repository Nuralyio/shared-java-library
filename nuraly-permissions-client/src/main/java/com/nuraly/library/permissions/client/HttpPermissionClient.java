package com.nuraly.library.permissions.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nuraly.library.permissions.client.model.AccessibleResourcesResponse;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

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
            return makePermissionRequest("/api/v1/acl/check-permission", request);
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
            return makePermissionRequest("/api/v1/acl/check-anonymous-permission", request);
        } catch (Exception e) {
            System.err.println("Anonymous permission check failed: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean validatePublicLink(String token, String permissionType) {
        try {
            Map<String, Object> request = createPublicLinkRequest(token, permissionType);
            return makePermissionRequest("/api/v1/acl/validate-public-link", request);
        } catch (Exception e) {
            System.err.println("Public link validation failed: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean isHealthy() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(permissionsServiceUrl + "/api/v1/acl/public-resources"))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .GET()
                .build();
                
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public List<String> getAccessibleResourceIds(String userId, String permissionType, String resourceType, String tenantId) {
        try {
            AccessibleResourcesResponse response = getAccessibleResources(userId, permissionType, resourceType, tenantId, 0, 0);
            return response.getResourceIds();
        } catch (Exception e) {
            System.err.println("Failed to get accessible resource IDs: " + e.getMessage());
            return Collections.emptyList(); // Fail closed - return empty list on error
        }
    }
    
    @Override
    public AccessibleResourcesResponse getAccessibleResources(String userId, String permissionType, 
                                                             String resourceType, String tenantId, 
                                                             int limit, int offset) {
        try {
            Map<String, Object> request = createAccessibleResourcesRequest(userId, permissionType, resourceType, tenantId, limit, offset);
            return makeAccessibleResourcesRequest("/api/v1/acl/accessible-resources", request);
        } catch (Exception e) {
            System.err.println("Failed to get accessible resources: " + e.getMessage());
            return new AccessibleResourcesResponse(Collections.emptyList(), permissionType, resourceType, tenantId, 0);
        }
    }
    
    @Override
    public boolean hasAnyAccessibleResources(String userId, String permissionType, String resourceType, String tenantId) {
        try {
            // Use limit=1 to just check if any resources exist
            AccessibleResourcesResponse response = getAccessibleResources(userId, permissionType, resourceType, tenantId, 1, 0);
            return response.getTotalCount() > 0;
        } catch (Exception e) {
            System.err.println("Failed to check for accessible resources: " + e.getMessage());
            return false; // Fail closed
        }
    }
    
    private boolean makePermissionRequest(String endpoint, Map<String, Object> requestBody) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(permissionsServiceUrl + endpoint))
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        
        // Add X-USER header if userId is present in the request body
        Object userId = requestBody.get("userId");
        Object tenantId = requestBody.get("tenantId");
        if (userId != null) {
            String userHeaderValue;
            if (tenantId != null) {
                userHeaderValue = "{\"uuid\":\"" + userId.toString() + "\",\"tenantId\":\"" + tenantId.toString() + "\"}";
            } else {
                userHeaderValue = "{\"uuid\":\"" + userId.toString() + "\"}";
            }
            requestBuilder.header("X-USER", userHeaderValue);
        }
        
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // 200 = permission granted, 403 = permission denied, others = error
        return response.statusCode() == 200;
    }
    
    private AccessibleResourcesResponse makeAccessibleResourcesRequest(String endpoint, Map<String, Object> requestBody) throws Exception {
        // Build query parameters for GET request
        StringBuilder queryParams = new StringBuilder();
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : requestBody.entrySet()) {
            // Skip null values
            if (entry.getValue() == null) {
                continue;
            }
            
            if (!first) {
                queryParams.append("&");
            }
            queryParams.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                       .append("=")
                       .append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
            first = false;
        }
        
        String url = permissionsServiceUrl + endpoint;
        if (queryParams.length() > 0) {
            url += "?" + queryParams.toString();
        }
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .GET();
        
        // Add X-USER header if userId is present in the request body
        Object userId = requestBody.get("userId");
        Object tenantId = requestBody.get("tenantId");
        if (userId != null) {
            String userHeaderValue;
            if (tenantId != null) {
                userHeaderValue = "{\"uuid\":\"" + userId.toString() + "\",\"tenantId\":\"" + tenantId.toString() + "\"}";
            } else {
                userHeaderValue = "{\"uuid\":\"" + userId.toString() + "\"}";
            }
            requestBuilder.header("X-USER", userHeaderValue);
        }
        
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), AccessibleResourcesResponse.class);
        } else {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }
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
    
    private Map<String, Object> createAccessibleResourcesRequest(String userId, String permissionType, 
                                                               String resourceType, String tenantId, 
                                                               int limit, int offset) {
        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);
        request.put("permissionType", permissionType);
        if (resourceType != null) {
            request.put("resourceType", resourceType);
        }
        if (tenantId != null) {
            request.put("tenantId", tenantId);
        }
        if (limit > 0) {
            request.put("limit", limit);
        }
        if (offset > 0) {
            request.put("offset", offset);
        }
        return request;
    }
}
