package com.nuraly.library.permissions.client;

import com.nuraly.library.permissions.client.model.AccessibleResourcesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for accessible resources functionality in PermissionClient.
 * Tests the new methods for retrieving resource IDs that users have access to.
 */
class AccessibleResourcesTest {
    
    private HttpPermissionClient client;
    
    @BeforeEach
    void setUp() {
        // Use a test service URL that will fail - we're testing fail-closed behavior
        client = new HttpPermissionClient("http://nonexistent-test-service:9999", 1);
    }
    
    @Test
    @DisplayName("getAccessibleResourceIds should return empty list on service failure")
    void testGetAccessibleResourceIds_ServiceFailure() {
        List<String> resourceIds = client.getAccessibleResourceIds(
            "user-123", 
            "read", 
            "document", 
            "tenant-456"
        );
        
        assertThat(resourceIds).isNotNull();
        assertThat(resourceIds).isEmpty();
    }
    
    @Test
    @DisplayName("getAccessibleResourceIds should handle null parameters")
    void testGetAccessibleResourceIds_NullParameters() {
        List<String> resourceIds = client.getAccessibleResourceIds(
            "user-123", 
            "read", 
            null,  // resourceType can be null
            null   // tenantId can be null
        );
        
        assertThat(resourceIds).isNotNull();
        assertThat(resourceIds).isEmpty();
    }
    
    @Test
    @DisplayName("getAccessibleResources should return empty response on service failure")
    void testGetAccessibleResources_ServiceFailure() {
        AccessibleResourcesResponse response = client.getAccessibleResources(
            "user-123", 
            "read", 
            "document", 
            "tenant-456",
            10,  // limit
            0    // offset
        );
        
        assertThat(response).isNotNull();
        assertThat(response.getResourceIds()).isNotNull();
        assertThat(response.getResourceIds()).isEmpty();
        assertThat(response.getTotalCount()).isEqualTo(0);
        assertThat(response.getPermissionType()).isEqualTo("read");
        assertThat(response.getResourceType()).isEqualTo("document");
        assertThat(response.getTenantId()).isEqualTo("tenant-456");
    }
    
    @Test
    @DisplayName("getAccessibleResources should handle pagination parameters")
    void testGetAccessibleResources_PaginationParameters() {
        AccessibleResourcesResponse response = client.getAccessibleResources(
            "user-123", 
            "write", 
            "file", 
            "tenant-789",
            50,  // limit
            100  // offset
        );
        
        assertThat(response).isNotNull();
        assertThat(response.getResourceIds()).isEmpty(); // Service failure
        assertThat(response.getPermissionType()).isEqualTo("write");
        assertThat(response.getResourceType()).isEqualTo("file");
        assertThat(response.getTenantId()).isEqualTo("tenant-789");
    }
    
    @Test
    @DisplayName("getAccessibleResources should handle zero limit and offset")
    void testGetAccessibleResources_ZeroLimitOffset() {
        AccessibleResourcesResponse response = client.getAccessibleResources(
            "user-123", 
            "delete", 
            "image", 
            "tenant-abc",
            0,  // no limit
            0   // no offset
        );
        
        assertThat(response).isNotNull();
        assertThat(response.getResourceIds()).isEmpty();
        assertThat(response.getPermissionType()).isEqualTo("delete");
    }
    
    @Test
    @DisplayName("hasAnyAccessibleResources should return false on service failure")
    void testHasAnyAccessibleResources_ServiceFailure() {
        boolean hasAny = client.hasAnyAccessibleResources(
            "user-123", 
            "read", 
            "document", 
            "tenant-456"
        );
        
        assertThat(hasAny).isFalse();
    }
    
    @Test
    @DisplayName("hasAnyAccessibleResources should handle null parameters")
    void testHasAnyAccessibleResources_NullParameters() {
        boolean hasAny = client.hasAnyAccessibleResources(
            "user-123", 
            "read", 
            null,  // resourceType can be null
            null   // tenantId can be null
        );
        
        assertThat(hasAny).isFalse();
    }
    
    @Test
    @DisplayName("Accessible resources methods should fail closed with invalid user ID")
    void testAccessibleResources_InvalidUserId() {
        // Test with null user ID
        List<String> resourceIds = client.getAccessibleResourceIds(null, "read", "document", "tenant-123");
        assertThat(resourceIds).isEmpty();
        
        // Test with empty user ID
        resourceIds = client.getAccessibleResourceIds("", "read", "document", "tenant-123");
        assertThat(resourceIds).isEmpty();
        
        // Test hasAny with null user ID
        boolean hasAny = client.hasAnyAccessibleResources(null, "read", "document", "tenant-123");
        assertThat(hasAny).isFalse();
    }
    
    @Test
    @DisplayName("Accessible resources methods should fail closed with invalid permission type")
    void testAccessibleResources_InvalidPermissionType() {
        // Test with null permission type
        List<String> resourceIds = client.getAccessibleResourceIds("user-123", null, "document", "tenant-123");
        assertThat(resourceIds).isEmpty();
        
        // Test with empty permission type
        resourceIds = client.getAccessibleResourceIds("user-123", "", "document", "tenant-123");
        assertThat(resourceIds).isEmpty();
        
        // Test hasAny with null permission type
        boolean hasAny = client.hasAnyAccessibleResources("user-123", null, "document", "tenant-123");
        assertThat(hasAny).isFalse();
    }
    
    @Test
    @DisplayName("AccessibleResourcesResponse should have proper equals and hashCode")
    void testAccessibleResourcesResponse_EqualsAndHashCode() {
        List<String> resourceIds = Arrays.asList("res-1", "res-2", "res-3");
        
        AccessibleResourcesResponse response1 = new AccessibleResourcesResponse(
            resourceIds, "read", "document", "tenant-123", 3
        );
        
        AccessibleResourcesResponse response2 = new AccessibleResourcesResponse(
            resourceIds, "read", "document", "tenant-123", 3
        );
        
        AccessibleResourcesResponse response3 = new AccessibleResourcesResponse(
            resourceIds, "write", "document", "tenant-123", 3  // Different permission
        );
        
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        assertThat(response1).isNotEqualTo(response3);
        assertThat(response1.toString()).contains("read", "document", "tenant-123");
    }
    
    @Test
    @DisplayName("AccessibleResourcesResponse should handle null values properly")
    void testAccessibleResourcesResponse_NullValues() {
        AccessibleResourcesResponse response = new AccessibleResourcesResponse();
        
        assertThat(response.getResourceIds()).isNull();
        assertThat(response.getPermissionType()).isNull();
        assertThat(response.getResourceType()).isNull();
        assertThat(response.getTenantId()).isNull();
        assertThat(response.getTotalCount()).isEqualTo(0);
        
        // Test setters
        response.setResourceIds(Arrays.asList("res-1"));
        response.setPermissionType("read");
        response.setResourceType("document");
        response.setTenantId("tenant-123");
        response.setTotalCount(1);
        
        assertThat(response.getResourceIds()).hasSize(1);
        assertThat(response.getPermissionType()).isEqualTo("read");
        assertThat(response.getResourceType()).isEqualTo("document");
        assertThat(response.getTenantId()).isEqualTo("tenant-123");
        assertThat(response.getTotalCount()).isEqualTo(1);
    }
}
