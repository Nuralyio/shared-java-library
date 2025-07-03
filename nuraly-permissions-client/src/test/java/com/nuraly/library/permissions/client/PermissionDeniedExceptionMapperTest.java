package com.nuraly.library.permissions.client;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PermissionDeniedExceptionMapper.
 * Uses a simplified approach that doesn't require a full JAX-RS runtime.
 */
class PermissionDeniedExceptionMapperTest {

    private PermissionDeniedExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PermissionDeniedExceptionMapper();
    }

    @Test
    void testMapException() {
        // Create a test exception
        String testMessage = "Access denied to resource 'document' for action 'READ'";
        PermissionDeniedException exception = new PermissionDeniedException(testMessage);
        
        // Map the exception to a response
        Response response = mapper.toResponse(exception);
        
        // Verify the response
        assertNotNull(response);
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    }

    @Test
    void testMapExceptionWithNullMessage() {
        // Test with null message
        PermissionDeniedException exception = new PermissionDeniedException(null);
        Response response = mapper.toResponse(exception);
        
        assertNotNull(response);
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    }

    @Test
    void testMapExceptionWithEmptyMessage() {
        // Test with empty message
        PermissionDeniedException exception = new PermissionDeniedException("");
        Response response = mapper.toResponse(exception);
        
        assertNotNull(response);
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    }

    @Test
    void testMapExceptionWithLongMessage() {
        // Test with a long message
        String longMessage = "This is a very long error message that describes " +
            "a complex permission denial scenario with multiple resources and actions involved";
        PermissionDeniedException exception = new PermissionDeniedException(longMessage);
        Response response = mapper.toResponse(exception);
        
        assertNotNull(response);
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    }

    @Test
    void testExceptionMapperCreation() {
        // Test that the mapper can be created without issues
        assertNotNull(mapper);
        assertNotNull(new PermissionDeniedExceptionMapper());
    }

    @Test
    void testResponseStatusCode() {
        // Verify that all responses have the correct status code
        PermissionDeniedException exception = new PermissionDeniedException("test");
        Response response = mapper.toResponse(exception);
        
        assertEquals(403, response.getStatus());
        assertEquals(Response.Status.FORBIDDEN, Response.Status.fromStatusCode(response.getStatus()));
    }

    @Test
    void testMultipleExceptionMappings() {
        // Test mapping multiple different exceptions
        String[] messages = {
            "Access denied to user data",
            "Insufficient permissions for admin panel",
            "Resource not accessible",
            null,
            ""
        };
        
        for (String message : messages) {
            PermissionDeniedException exception = new PermissionDeniedException(message);
            Response response = mapper.toResponse(exception);
            
            assertNotNull(response, "Response should not be null for message: " + message);
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus(),
                "Status should be 403 for message: " + message);
        }
    }
}
