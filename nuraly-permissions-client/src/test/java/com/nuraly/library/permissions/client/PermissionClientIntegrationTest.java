package com.nuraly.library.permissions.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the entire permission client library.
 * Tests the interaction between components without external dependencies.
 */
class PermissionClientIntegrationTest {

    @Test
    void testRequiresPermissionAnnotation() throws NoSuchMethodException {
        // Given - annotation on a test method
        RequiresPermission annotation = TestResource.class
            .getMethod("testMethod")
            .getAnnotation(RequiresPermission.class);

        // Then
        assertNotNull(annotation);
        assertEquals("read", annotation.permissionType());
        assertEquals("document", annotation.resourceType());
        assertEquals("#{id}", annotation.resourceId());
    }

    @Test
    void testPermissionDeniedExceptionFlow() {
        // Given
        PermissionDeniedException exception = new PermissionDeniedException("Test error");
        PermissionDeniedExceptionMapper mapper = new PermissionDeniedExceptionMapper();

        // When
        var response = mapper.toResponse(exception);

        // Then
        assertEquals(403, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Test error"));
        assertTrue(response.getEntity().toString().contains("PERMISSION_DENIED"));
    }

    @Test
    void testHttpPermissionClientCreation() {
        // When
        HttpPermissionClient client = new HttpPermissionClient("http://test:8080", 10);

        // Then
        assertNotNull(client);
        // Client should be unhealthy when service is not available
        assertFalse(client.isHealthy());
    }

    @Test
    void testPermissionInterceptorCreation() {
        // When
        PermissionInterceptor interceptor = new PermissionInterceptor();

        // Then
        assertNotNull(interceptor);
    }

    // Test resource class for annotation testing
    static class TestResource {
        @RequiresPermission(
            permissionType = "read",
            resourceType = "document",
            resourceId = "#{id}"
        )
        public void testMethod() {
            // Test method
        }
    }
}
