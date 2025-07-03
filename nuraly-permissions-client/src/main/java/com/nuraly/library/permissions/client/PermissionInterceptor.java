package com.nuraly.library.permissions.client;

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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight permission interceptor that delegates to PermissionClient.
 * Supports authenticated users, anonymous access, and public tokens.
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class PermissionInterceptor implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    PermissionClient permissionClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Skip permission checks for test mode
        if ("true".equals(requestContext.getHeaderString("X-Test-Mode"))) {
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
        
        // Check for different access types
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
                hasPermission = permissionClient.validatePublicLink(publicTokenHeader, (String) params.get("permissionType"));
                if (!hasPermission) {
                    errorMessage = "Invalid or expired public token";
                }
            } else {
                // Try anonymous access
                hasPermission = permissionClient.hasAnonymousPermission(
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
            JsonNode userNode = objectMapper.readTree(userHeader);
            String userId = userNode.get("uuid").asText();
            
            return permissionClient.hasPermission(
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
     * Supports #{paramName} syntax for path and query parameters.
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
}
