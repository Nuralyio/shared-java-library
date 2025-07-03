package com.nuraly.library.permissions.client;

import com.nuraly.library.permissions.client.model.AccessibleResourcesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for HttpPermissionClient.
 * Tests the client functionality without mocking final classes.
 */
class HttpPermissionClientTest {

    private HttpPermissionClient permissionClient;

    @BeforeEach
    void setUp() {
        // Create HttpPermissionClient with test configuration
        permissionClient = new HttpPermissionClient();
    }

    @Test
    void testClientInitialization() {
        // Test that the client can be instantiated
        assertNotNull(permissionClient);
    }

    @Test
    void testConfigurationReading() {
        // Test that constructor properly accepts configuration
        HttpPermissionClient configuredClient = new HttpPermissionClient("http://test-url:8080", 10);
        assertNotNull(configuredClient);
    }

    @Test
    void testHasPermissionWithInvalidUrl() {
        // Use an invalid URL to test error handling
        HttpPermissionClient clientWithInvalidUrl = new HttpPermissionClient("http://invalid-host:9999", 1);
        
        // Should return false (fail closed) when service is unreachable
        boolean hasPermission = clientWithInvalidUrl.hasPermission("user123", "read", "resource456", "tenant789");
        assertFalse(hasPermission, "Should return false when service is unreachable");
    }

    @Test
    void testHasAnonymousPermissionWithInvalidUrl() {
        HttpPermissionClient clientWithInvalidUrl = new HttpPermissionClient("http://invalid-host:9999", 1);
        
        boolean hasPermission = clientWithInvalidUrl.hasAnonymousPermission("resource123", "read", "tenant456");
        assertFalse(hasPermission, "Should return false when service is unreachable");
    }

    @Test
    void testValidatePublicLinkWithInvalidUrl() {
        HttpPermissionClient clientWithInvalidUrl = new HttpPermissionClient("http://invalid-host:9999", 1);
        
        boolean isValid = clientWithInvalidUrl.validatePublicLink("token123", "read");
        assertFalse(isValid, "Should return false when service is unreachable");
    }

    @Test
    void testIsHealthyWithInvalidUrl() {
        HttpPermissionClient clientWithInvalidUrl = new HttpPermissionClient("http://invalid-host:9999", 1);
        
        boolean isHealthy = clientWithInvalidUrl.isHealthy();
        assertFalse(isHealthy, "Should return false when service is unreachable");
    }

    @Test
    void testFailClosedBehavior() {
        // Test that errors result in permission denial (fail closed)
        HttpPermissionClient clientWithInvalidUrl = new HttpPermissionClient("http://invalid-host:9999", 1);
        
        // All permission checks should fail closed
        assertFalse(clientWithInvalidUrl.hasPermission("user", "read", "resource", null));
        assertFalse(clientWithInvalidUrl.hasAnonymousPermission("resource", "read", null));
        assertFalse(clientWithInvalidUrl.validatePublicLink("token", "read"));
        assertFalse(clientWithInvalidUrl.isHealthy());
    }

    @Test
    void testValidParameterHandling() {
        // Test parameter validation - null parameters should be handled gracefully
        assertDoesNotThrow(() -> {
            permissionClient.hasPermission("user", "read", "resource", null);
            permissionClient.hasAnonymousPermission("resource", "read", null);
            permissionClient.validatePublicLink("token", "read");
        });
    }

    @Test
    void testConstructorOverloads() {
        // Test default constructor
        HttpPermissionClient defaultClient = new HttpPermissionClient();
        assertNotNull(defaultClient);
        
        // Test parameterized constructor
        HttpPermissionClient configuredClient = new HttpPermissionClient("http://custom-url:9090", 30);
        assertNotNull(configuredClient);
    }

    @Test
    void testTimeoutConfiguration() {
        // Test that timeout is configurable
        assertDoesNotThrow(() -> {
            new HttpPermissionClient("http://localhost:8080", 1);
            new HttpPermissionClient("http://localhost:8080", 60);
        });
    }

    @Test
    void testResourceIdHandling() {
        // Test that various resource ID formats are handled
        assertDoesNotThrow(() -> {
            permissionClient.hasPermission("user1", "read", "simple-resource", "tenant1");
            permissionClient.hasPermission("user2", "write", "resource/with/slashes", "tenant2");
            permissionClient.hasPermission("user3", "delete", "resource-with-dashes", null);
        });
    }

    @Test
    void testPermissionTypeHandling() {
        // Test that various permission types are handled
        assertDoesNotThrow(() -> {
            permissionClient.hasPermission("user", "read", "resource", "tenant");
            permissionClient.hasPermission("user", "write", "resource", "tenant");
            permissionClient.hasPermission("user", "delete", "resource", "tenant");
            permissionClient.hasPermission("user", "share", "resource", "tenant");
            permissionClient.hasPermission("user", "custom-permission", "resource", "tenant");
        });
    }

    @Test
    void testDefaultMethodBehavior() {
        // Test that the default isHealthy method behavior is preserved
        PermissionClient mockClient = new PermissionClient() {
            @Override
            public boolean hasPermission(String userId, String permissionType, String resourceId, String tenantId) {
                return false;
            }

            @Override
            public boolean hasAnonymousPermission(String resourceId, String permissionType, String tenantId) {
                return false;
            }

            @Override
            public boolean validatePublicLink(String token, String permissionType) {
                return false;
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
                return false;
            }
        };
        
        assertTrue(mockClient.isHealthy(), "Default isHealthy should return true");
    }
}
