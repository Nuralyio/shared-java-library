package com.nuraly.library.permissions.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermissionDeniedExceptionTest {

    @Test
    void testConstructor_WithMessage() {
        // Given
        String message = "Access denied";

        // When
        PermissionDeniedException exception = new PermissionDeniedException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructor_WithMessageAndCause() {
        // Given
        String message = "Access denied";
        Throwable cause = new RuntimeException("Root cause");

        // When
        PermissionDeniedException exception = new PermissionDeniedException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructor_WithNullMessage() {
        // When
        PermissionDeniedException exception = new PermissionDeniedException(null);

        // Then
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructor_WithNullCause() {
        // Given
        String message = "Access denied";

        // When
        PermissionDeniedException exception = new PermissionDeniedException(message, null);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testIsRuntimeException() {
        // Given
        PermissionDeniedException exception = new PermissionDeniedException("test");

        // Then
        assertTrue(exception instanceof RuntimeException);
    }
}
