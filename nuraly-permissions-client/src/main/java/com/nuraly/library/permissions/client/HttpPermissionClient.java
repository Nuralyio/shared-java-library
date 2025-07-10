package com.nuraly.library.permissions.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuraly.library.permissions.client.model.AccessibleResourcesResponse;
import com.nuraly.library.permissions.client.model.CreateResourceRequest;
import com.nuraly.library.permissions.client.model.CreateResourceResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.config.ConfigProvider;

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
    
    private HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Inject
    UserContextService userContextService;
    
    // Configuration injected via CDI or loaded programmatically
    @ConfigProperty(name = "nuraly.permissions.service.url", defaultValue = "http://localhost:8080")
    String permissionsServiceUrl;
    
    @ConfigProperty(name = "nuraly.permissions.client.timeout.seconds", defaultValue = "5")
    int timeoutSeconds;
    
    public HttpPermissionClient() {
        this.objectMapper = new ObjectMapper();
        // Initialize configuration programmatically for non-CDI environments
        initializeConfiguration();
        // Initialize HttpClient immediately for non-CDI environments
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(timeoutSeconds))
            .build();
    }
    
    private void initializeConfiguration() {
        try {
            // Try to get config from MicroProfile Config (works in both CDI and non-CDI environments)
            org.eclipse.microprofile.config.Config config = ConfigProvider.getConfig();
            this.permissionsServiceUrl = config.getOptionalValue("nuraly.permissions.service.url", String.class)
                .orElse("http://localhost:8080");
            this.timeoutSeconds = config.getOptionalValue("nuraly.permissions.client.timeout.seconds", Integer.class)
                .orElse(5);
        } catch (Exception e) {
            // Fallback to default values if MicroProfile Config is not available
            this.permissionsServiceUrl = "http://localhost:8080";
            this.timeoutSeconds = 5;
        }
    }
    
    /**
     * Constructor for direct instantiation (non-CDI environments like tests).
     * For CDI environments, use the default constructor and configure via application.properties
     */
    public HttpPermissionClient(String permissionsServiceUrl, int timeoutSeconds) {
        this.permissionsServiceUrl = permissionsServiceUrl;
        this.timeoutSeconds = timeoutSeconds;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(timeoutSeconds))
            .build();
    }
    
    @PostConstruct
    void init() {
        // In CDI environment, configuration will be injected after constructor
        // Check if CDI injection happened (non-null and not default)
        if (this.permissionsServiceUrl != null && !this.permissionsServiceUrl.equals("http://localhost:8080")) {
            // CDI injection worked, use injected values
            // Do nothing - values are already set
        } else {
            // CDI injection didn't work or used defaults, try programmatic config
            initializeConfiguration();
        }
        
        // Initialize HttpClient with the final configuration
        if (this.httpClient == null) {
            this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
        }
    }
    
    @Override
    public boolean hasPermission(String userId, String permissionType, String resourceId, String tenantId) {
        try {
            Map<String, Object> request = createPermissionRequest(userId, permissionType, resourceId, tenantId);
            return makePermissionRequest("/api/v1/acl/check-permission", request);
        } catch (Exception e) {
            return false; // Fail closed - deny access on error
        }
    }
    
    @Override
    public boolean hasPermission(String permissionType, String resourceId) {
        try {
            if (userContextService == null) {
                // Try to get from CDI if not injected (for non-CDI environments)
                try {
                    userContextService = jakarta.enterprise.inject.spi.CDI.current().select(UserContextService.class).get();
                } catch (Exception cdiEx) {
                    return false; // Fail closed - deny access if no user context
                }
            }
            
            String userId = userContextService.getCurrentUserId();
            String tenantId = userContextService.getCurrentTenantId();
            
            if (userId == null) {
                return false; // Fail closed - deny access if no user
            }
            
            return hasPermission(userId, permissionType, resourceId, tenantId);
        } catch (Exception e) {
            return false; // Fail closed - deny access on error
        }
    }
    
    @Override
    public boolean hasAnonymousPermission(String resourceId, String permissionType, String tenantId) {
        try {
            Map<String, Object> request = createAnonymousRequest(resourceId, permissionType, tenantId);
            return makePermissionRequest("/api/v1/acl/check-anonymous-permission", request);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean validatePublicLink(String token, String permissionType) {
        try {
            Map<String, Object> request = createPublicLinkRequest(token, permissionType);
            return makePermissionRequest("/api/v1/acl/validate-public-link", request);
        } catch (Exception e) {
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
            return Collections.emptyList(); // Fail closed - return empty list on error
        }
    }
    
    @Override
    public List<String> getAccessibleResourceIds(String permissionType, String resourceType) {
        try {
            if (userContextService == null) {
                try {
                    userContextService = jakarta.enterprise.inject.spi.CDI.current().select(UserContextService.class).get();
                } catch (Exception cdiEx) {
                    return Collections.emptyList();
                }
            }
            
            String userId = userContextService.getCurrentUserId();
            String tenantId = userContextService.getCurrentTenantId();
            
            if (userId == null) {
                return Collections.emptyList();
            }
            
            return getAccessibleResourceIds(userId, permissionType, resourceType, tenantId);
        } catch (Exception e) {
            return Collections.emptyList();
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
            return new AccessibleResourcesResponse(Collections.emptyList(), permissionType, resourceType, tenantId, 0);
        }
    }
    
    @Override
    public AccessibleResourcesResponse getAccessibleResources(String permissionType, String resourceType, int limit, int offset) {
        try {
            if (userContextService == null) {
                try {
                    userContextService = jakarta.enterprise.inject.spi.CDI.current().select(UserContextService.class).get();
                } catch (Exception cdiEx) {
                    return new AccessibleResourcesResponse(Collections.emptyList(), permissionType, resourceType, null, 0);
                }
            }
            
            String userId = userContextService.getCurrentUserId();
            String tenantId = userContextService.getCurrentTenantId();
            
            if (userId == null) {
                return new AccessibleResourcesResponse(Collections.emptyList(), permissionType, resourceType, tenantId, 0);
            }
            
            return getAccessibleResources(userId, permissionType, resourceType, tenantId, limit, offset);
        } catch (Exception e) {
            return new AccessibleResourcesResponse(Collections.emptyList(), permissionType, resourceType, null, 0);
        }
    }
    
    @Override
    public boolean hasAnyAccessibleResources(String userId, String permissionType, String resourceType, String tenantId) {
        try {
            // Use limit=1 to just check if any resources exist
            AccessibleResourcesResponse response = getAccessibleResources(userId, permissionType, resourceType, tenantId, 1, 0);
            return response.getTotalCount() > 0;
        } catch (Exception e) {
            return false; // Fail closed
        }
    }
    
    @Override
    public boolean hasAnyAccessibleResources(String permissionType, String resourceType) {
        try {
            if (userContextService == null) {
                try {
                    userContextService = jakarta.enterprise.inject.spi.CDI.current().select(UserContextService.class).get();
                } catch (Exception cdiEx) {
                    return false;
                }
            }
            
            String userId = userContextService.getCurrentUserId();
            String tenantId = userContextService.getCurrentTenantId();
            
            if (userId == null) {
                return false;
            }
            
            return hasAnyAccessibleResources(userId, permissionType, resourceType, tenantId);
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean makePermissionRequest(String endpoint, Map<String, Object> requestBody) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        String fullUrl = permissionsServiceUrl + endpoint;
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(fullUrl))
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
        request.put("permissionName", permissionType); // Changed from permissionType to permissionName
        request.put("resourceId", resourceId);
        if (tenantId != null) {
            request.put("tenantId", tenantId);
        }
        return request;
    }
    
    private Map<String, Object> createAnonymousRequest(String resourceId, String permissionType, String tenantId) {
        Map<String, Object> request = new HashMap<>();
        request.put("resourceId", resourceId);
        request.put("permissionName", permissionType); // Changed from permissionType to permissionName
        if (tenantId != null) {
            request.put("tenantId", tenantId);
        }
        return request;
    }
    
    private Map<String, Object> createPublicLinkRequest(String token, String permissionType) {
        Map<String, Object> request = new HashMap<>();
        request.put("token", token);
        request.put("permissionName", permissionType); // Changed from permissionType to permissionName
        return request;
    }
    
    private Map<String, Object> createAccessibleResourcesRequest(String userId, String permissionType, 
                                                               String resourceType, String tenantId, 
                                                               int limit, int offset) {
        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);
        request.put("permission", permissionType); // Changed from permissionType to permission (for query param)
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
    
    @Override
    public CreateResourceResponse createResource(CreateResourceRequest request) {
        try {
            String jsonBody = objectMapper.writeValueAsString(request);
            String url = permissionsServiceUrl + "/api/v1/acl/resources";
            
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
            
            // Add user context headers
            if (request.getOwnerId() != null) {
                String userHeaderValue;
                if (request.getTenantId() != null) {
                    userHeaderValue = "{\"uuid\":\"" + request.getOwnerId() + "\",\"tenantId\":\"" + request.getTenantId() + "\"}";
                } else {
                    userHeaderValue = "{\"uuid\":\"" + request.getOwnerId() + "\"}";
                }
                requestBuilder.header("X-USER", userHeaderValue);
            }
            
            HttpRequest httpRequest = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), CreateResourceResponse.class);
            } else {
                String errorMsg = "Failed to create resource: HTTP " + response.statusCode() + " - " + response.body();
                throw new RuntimeException(errorMsg);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating resource: " + e.getMessage(), e);
        }
    }
}
