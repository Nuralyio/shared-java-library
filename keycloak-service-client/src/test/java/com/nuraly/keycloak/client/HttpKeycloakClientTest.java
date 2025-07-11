package com.nuraly.keycloak.client;

import com.nuraly.keycloak.client.model.EmailToUuidMapping;
import com.nuraly.keycloak.client.model.EmailToUuidRequest;
import com.nuraly.keycloak.client.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Unit tests for HttpKeycloakClient
 */
public class HttpKeycloakClientTest {
    
    private HttpKeycloakClient client;
    
    @BeforeEach
    void setUp() {
        // For testing, we'll use a mock base URL
        client = new HttpKeycloakClient("http://localhost:7012");
    }
    
    @Test
    void testClientCreation() {
        assertNotNull(client);
    }
    
    @Test
    void testEmailToUuidMappingModel() {
        EmailToUuidMapping mapping = new EmailToUuidMapping("test@example.com", "test-uuid");
        
        assertEquals("test@example.com", mapping.getEmail());
        assertEquals("test-uuid", mapping.getUuid());
        
        // Test fluent API
        mapping.withEmail("new@example.com").withUuid("new-uuid");
        assertEquals("new@example.com", mapping.getEmail());
        assertEquals("new-uuid", mapping.getUuid());
    }
    
    @Test
    void testUserModel() {
        User user = new User("test-id", "testuser", "test@example.com");
        
        assertEquals("test-id", user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        
        // Test fluent API
        user.withFirstName("John").withLastName("Doe");
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("John Doe", user.getFullName());
    }
    
    @Test
    void testEmailToUuidRequest() {
        List<String> emails = Arrays.asList("user1@example.com", "user2@example.com");
        EmailToUuidRequest request = new EmailToUuidRequest(emails);
        
        assertEquals(emails, request.getEmails());
        
        // Test fluent API
        List<String> newEmails = Arrays.asList("user3@example.com");
        request.withEmails(newEmails);
        assertEquals(newEmails, request.getEmails());
    }
    
    @Test
    void testUserFullNameGeneration() {
        User user = new User();
        
        // Test with both first and last name
        user.withFirstName("John").withLastName("Doe");
        assertEquals("John Doe", user.getFullName());
        
        // Test with only first name
        user.withFirstName("John").withLastName(null);
        assertEquals("John", user.getFullName());
        
        // Test with only last name
        user.withFirstName(null).withLastName("Doe");
        assertEquals("Doe", user.getFullName());
        
        // Test with username fallback
        user.withFirstName(null).withLastName(null).withUsername("johndoe");
        assertEquals("johndoe", user.getFullName());
    }
    
    @Test
    void testFactoryMethods() {
        HttpKeycloakClient factoryClient = HttpKeycloakClient.create("http://test:7012");
        assertNotNull(factoryClient);
        
        // Test config-based creation (will use defaults since no config is present)
        KeycloakClient configClient = HttpKeycloakClient.createFromConfig();
        assertNotNull(configClient);
    }
    
    // Note: Integration tests that actually call the service would require
    // a running Keycloak service instance and would be better suited for
    // integration test suites rather than unit tests.
}
