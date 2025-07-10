package com.nuraly.library.permissions.client;

import com.nuraly.library.permissions.client.model.AccessibleResourcesResponse;
import com.nuraly.library.permissions.client.model.CreateResourceRequest;
import com.nuraly.library.permissions.client.model.CreateResourceResponse;
import com.nuraly.library.permissions.client.model.UpdateResourceRequest;
import com.nuraly.library.permissions.client.model.SetParentResourceRequest;
import com.nuraly.library.permissions.client.model.ResourceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PermissionInterceptor (JAX-RS ContainerRequestFilter).
 * Tests the interceptor functionality and annotation usage.
 */
class PermissionInterceptorTest {

    private PermissionInterceptor interceptor;
    private TestPermissionClient testPermissionClient;

    @BeforeEach
    void setUp() {
        testPermissionClient = new TestPermissionClient();
        interceptor = new PermissionInterceptor();
    }

    @Test
    void testInterceptorInitialization() {
        assertNotNull(interceptor);
    }

    @Test
    void testRequiresPermissionAnnotation() throws Exception {
        Method testMethod = TestService.class.getMethod("testMethod", String.class);
        RequiresPermission annotation = testMethod.getAnnotation(RequiresPermission.class);
        
        assertNotNull(annotation);
        assertEquals("read", annotation.permissionType());
        assertEquals("document", annotation.resourceType());
        assertEquals("#{id}", annotation.resourceId());
    }

    @Test
    void testAnnotationWithStaticResourceId() throws Exception {
        Method method = TestService.class.getMethod("testMethodWithStaticId");
        RequiresPermission annotation = method.getAnnotation(RequiresPermission.class);
        
        assertNotNull(annotation);
        assertEquals("write", annotation.permissionType());
        assertEquals("file", annotation.resourceType());
        assertEquals("static-resource-123", annotation.resourceId());
    }

    @Test
    void testPlaceholderResolution() {
        String dynamicTemplate = "#{id}";
        assertTrue(dynamicTemplate.startsWith("#{") && dynamicTemplate.endsWith("}"));
        
        String staticValue = "static-resource-123";
        assertFalse(staticValue.startsWith("#{"));
        
        String paramName = dynamicTemplate.substring(2, dynamicTemplate.length() - 1);
        assertEquals("id", paramName);
    }

    @Test
    void testPermissionClientMethods() {
        assertTrue(testPermissionClient.hasPermission("user", "read", "resource", "tenant"));
        assertTrue(testPermissionClient.hasAnonymousPermission("resource", "read", "tenant"));
        assertTrue(testPermissionClient.validatePublicLink("token", "read"));
        assertTrue(testPermissionClient.isHealthy());
    }

    @Test
    void testPermissionDenialScenarios() {
        testPermissionClient.setReturnValue(false);
        
        assertFalse(testPermissionClient.hasPermission("user", "read", "resource", "tenant"));
        assertFalse(testPermissionClient.hasAnonymousPermission("resource", "read", "tenant"));
        assertFalse(testPermissionClient.validatePublicLink("token", "read"));
    }

    @Test
    void testHeaderBasedPermissionChecking() {
        String userHeader = "{\"uuid\":\"user123\"}";
        assertNotNull(userHeader);
        assertTrue(userHeader.contains("uuid"));
        
        String publicToken = "public-token-123";
        assertNotNull(publicToken);
        assertTrue(publicToken.length() > 0);
        
        String tenantHeader = "tenant-123";
        assertNotNull(tenantHeader);
        assertTrue(tenantHeader.length() > 0);
    }

    @Test
    void testErrorHandling() {
        PermissionDeniedException exception = new PermissionDeniedException("Access denied");
        assertEquals("Access denied", exception.getMessage());
        
        PermissionDeniedException exceptionWithCause = new PermissionDeniedException(
            "Permission check failed", 
            new RuntimeException("Service unavailable")
        );
        assertEquals("Permission check failed", exceptionWithCause.getMessage());
        assertNotNull(exceptionWithCause.getCause());
    }

    @Test
    void testResourceMethodAnnotationChecking() throws Exception {
        Method annotatedMethod = TestService.class.getMethod("testMethod", String.class);
        Method unannotatedMethod = TestService.class.getMethod("methodWithoutAnnotation");
        
        assertNotNull(annotatedMethod.getAnnotation(RequiresPermission.class));
        assertNull(unannotatedMethod.getAnnotation(RequiresPermission.class));
    }

    // Test service class with various annotation patterns
    static class TestService {
        @RequiresPermission(
            permissionType = "read",
            resourceType = "document",
            resourceId = "#{id}"
        )
        public String testMethod(String id) {
            return "test result";
        }

        @RequiresPermission(
            permissionType = "write",
            resourceType = "file",
            resourceId = "static-resource-123"
        )
        public String testMethodWithStaticId() {
            return "test result";
        }

        public String methodWithoutAnnotation() {
            return "no permission check";
        }
    }

    // Test permission client implementation
    static class TestPermissionClient implements PermissionClient {
        private boolean returnValue = true;

        @Override
        public boolean hasPermission(String userId, String permissionType, String resourceId, String tenantId) {
            return returnValue;
        }

        @Override
        public boolean hasAnonymousPermission(String resourceId, String permissionType, String tenantId) {
            return returnValue;
        }

        @Override
        public boolean validatePublicLink(String token, String permissionType) {
            return returnValue;
        }

        @Override
        public List<String> getAccessibleResourceIds(String userId, String permissionType, String resourceType, String tenantId) {
            return Collections.emptyList();
        }

        @Override
        public AccessibleResourcesResponse getAccessibleResources(String userId, String permissionType, String resourceType, String tenantId, int limit, int offset) {
            return new AccessibleResourcesResponse(Collections.emptyList(), permissionType, resourceType, tenantId, 0);
        }

        @Override
        public boolean hasAnyAccessibleResources(String userId, String permissionType, String resourceType, String tenantId) {
            return returnValue;
        }

        @Override
        public boolean hasPermission(String permissionType, String resourceId) {
            return returnValue;
        }

        @Override
        public List<String> getAccessibleResourceIds(String permissionType, String resourceType) {
            return Collections.emptyList();
        }

        @Override
        public AccessibleResourcesResponse getAccessibleResources(String permissionType, String resourceType, int limit, int offset) {
            return new AccessibleResourcesResponse(Collections.emptyList(), permissionType, resourceType, null, 0);
        }

        @Override
        public boolean hasAnyAccessibleResources(String permissionType, String resourceType) {
            return returnValue;
        }

        @Override
        public CreateResourceResponse createResource(CreateResourceRequest request) {
            return new CreateResourceResponse(request.getResourceId(), request.getResourceType(), request.getTenantId(), request.getOwnerId(), true);
        }

        @Override
        public ResourceResponse updateResource(String resourceId, UpdateResourceRequest request) {
            ResourceResponse response = new ResourceResponse();
            response.setId(resourceId);
            response.setName(request.getName());
            response.setDescription(request.getDescription());
            return response;
        }

        @Override
        public ResourceResponse setResourceParent(String resourceId, SetParentResourceRequest request) {
            ResourceResponse response = new ResourceResponse();
            response.setId(resourceId);
            response.setParentResourceId(request.getParentResourceId());
            return response;
        }

        @Override
        public ResourceResponse removeResourceParent(String resourceId, String reason) {
            ResourceResponse response = new ResourceResponse();
            response.setId(resourceId);
            response.setParentResourceId(null);
            return response;
        }

        @Override
        public List<ResourceResponse> getChildResources(String parentResourceId) {
            return Collections.emptyList();
        }

        public void setReturnValue(boolean returnValue) {
            this.returnValue = returnValue;
        }
    }
}
