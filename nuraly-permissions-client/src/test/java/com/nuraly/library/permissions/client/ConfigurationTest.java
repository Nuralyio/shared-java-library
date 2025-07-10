package com.nuraly.library.permissions.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify configuration injection works correctly
 */
public class ConfigurationTest {

    @Test
    public void testConfigurationWithSystemProperty() {
        // Set system property
        System.setProperty("nuraly.permissions.service.url", "http://test-config:9999");
        System.setProperty("nuraly.permissions.client.timeout.seconds", "15");
        
        try {
            // Create client - this should pick up the configuration
            HttpPermissionClient client = new HttpPermissionClient();
            
            // Verify the configuration was loaded
            assertEquals("http://test-config:9999", client.permissionsServiceUrl);
            assertEquals(15, client.timeoutSeconds);
            
        } finally {
            // Clean up
            System.clearProperty("nuraly.permissions.service.url");
            System.clearProperty("nuraly.permissions.client.timeout.seconds");
        }
    }
    
    @Test
    public void testDefaultConfiguration() {
        // Ensure no system properties are set
        System.clearProperty("nuraly.permissions.service.url");
        System.clearProperty("nuraly.permissions.client.timeout.seconds");
        
        // Create client - this should use defaults
        HttpPermissionClient client = new HttpPermissionClient();
        
        // Verify defaults
        assertEquals("http://localhost:8080", client.permissionsServiceUrl);
        assertEquals(5, client.timeoutSeconds);
    }
    
    @Test
    public void testParametrizedConstructor() {
        // This should work regardless of configuration
        HttpPermissionClient client = new HttpPermissionClient("http://direct-config:7011", 20);
        
        assertEquals("http://direct-config:7011", client.permissionsServiceUrl);
        assertEquals(20, client.timeoutSeconds);
        // Test that the client can perform a health check (verifies httpClient is initialized)
        // This will fail to connect but should not throw NPE
        assertFalse(client.isHealthy()); // Will be false due to connection failure, but no NPE
    }
}
