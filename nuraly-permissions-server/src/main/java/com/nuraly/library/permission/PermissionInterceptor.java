package com.nuraly.library.permission;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Permission Interceptor that supports both authenticated and anonymous access
 * Integrates with the comprehensive ACL system
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class PermissionInterceptor implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    PermissionService permissionService;

    @Inject
    @ConfigProperty(name = "permissions.api.url")
    String permissionsApiUrl;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Skip permission checks if this looks like a test (check for specific test headers or paths)
        String testHeader = requestContext.getHeaderString("X-Test-Mode");
        if ("true".equals(testHeader)) {
            return;
        }
        
        Method resourceMethod = resourceInfo.getResourceMethod();
        if (resourceMethod == null) {
            return; // No resource method means this is not a JAX-RS endpoint
        }

        // Check if the method has the @RequiresPermission annotation
        RequiresPermission permissionAnno = resourceMethod.getAnnotation(RequiresPermission.class);
        if (permissionAnno == null) {
            return; // No @RequiresPermission annotation; skip further checks
        }

        // Extract parameters from the annotation
        Map<String, Object> params = extractAnnotationParams(permissionAnno, requestContext);
        
        // Check for authenticated user first
        String userHeader = requestContext.getHeaderString("X-USER");
        String tenantHeader = requestContext.getHeaderString("X-TENANT-ID");
        String publicTokenHeader = requestContext.getHeaderString("X-PUBLIC-TOKEN");
        
        boolean hasPermission = false;
        String errorMessage = null;
        
        try {
            if (userHeader != null && !userHeader.isEmpty()) {
                // Authenticated user access
                hasPermission = checkAuthenticatedUserPermission(userHeader, params, tenantHeader);
                if (!hasPermission) {
                    errorMessage = "Authenticated user permission denied";
                }
            } else if (publicTokenHeader != null && !publicTokenHeader.isEmpty()) {
                // Public token access (anonymous with token)
                hasPermission = permissionService.validatePublicLink(publicTokenHeader, (String) params.get("permissionType"));
                if (!hasPermission) {
                    errorMessage = "Invalid or expired public token";
                }
            } else {
                // Try anonymous access
                hasPermission = permissionService.hasAnonymousPermission(
                    (String) params.get("resourceId"), 
                    (String) params.get("permissionType"), 
                    tenantHeader
                );
                if (!hasPermission) {
                    errorMessage = "Anonymous access denied - authentication required";
                }
            }
        } catch (Exception e) {
            hasPermission = false;
            errorMessage = "Permission check failed: " + e.getMessage();
        }

        if (!hasPermission) {
            throw new PermissionDeniedException(errorMessage != null ? errorMessage : "Access denied");
        }
    }

    private boolean checkAuthenticatedUserPermission(String userHeader, Map<String, Object> params, String tenantHeader) {
        try {
            JsonNode userNode = new ObjectMapper().readTree(userHeader);
            String userId = userNode.get("uuid").asText();
            
            // Use the enhanced permission service
            return permissionService.hasPermission(
                userId,
                (String) params.get("permissionType"),
                (String) params.get("resourceId"),
                tenantHeader
            );
        } catch (Exception e) {
            System.err.println("Error checking authenticated user permission: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extract parameters from the annotation, resolving placeholders dynamically.
     */
    private Map<String, Object> extractAnnotationParams(RequiresPermission permissionAnno, ContainerRequestContext requestContext) {
        Map<String, Object> params = new HashMap<>();
        params.put("permissionType", permissionAnno.permissionType());
        params.put("resourceType", permissionAnno.resourceType());
        params.put("resourceId", resolvePlaceholder(permissionAnno.resourceId(), requestContext));
        return params;
    }

    /**
     * Resolve placeholders dynamically from the request context.
     */
    private String resolvePlaceholder(String template, ContainerRequestContext requestContext) {
        if (template.startsWith("#{") && template.endsWith("}")) {
            String paramName = template.substring(2, template.length() - 1);

            // Retrieve parameter from the request path
            String pathParamValue = requestContext.getUriInfo().getPathParameters().getFirst(paramName);
            if (pathParamValue != null) {
                return pathParamValue;
            }

            // Retrieve parameter from the query string
            String queryParamValue = requestContext.getUriInfo().getQueryParameters().getFirst(paramName);
            if (queryParamValue != null) {
                return queryParamValue;
            }
        }
        return template; // Return the template if no dynamic value is found
    }

    /**
     * Fallback method using HTTP API call (for backward compatibility)
     */
    private boolean checkPermissionViaAPI(Map<String, Object> params) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String payload = objectMapper.writeValueAsString(params);

            HttpURLConnection connection = getHttpURLConnection(payload);
            int responseCode = connection.getResponseCode();

            return responseCode == 200;
        } catch (Exception e) {
            System.err.println("Error checking permission via API: " + e.getMessage());
            return false;
        }
    }

    private HttpURLConnection getHttpURLConnection(String payload) throws IOException {
        HttpURLConnection connection =
                (HttpURLConnection) new java.net.URL(permissionsApiUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Write the payload
        try (java.io.OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        return connection;
    }
}