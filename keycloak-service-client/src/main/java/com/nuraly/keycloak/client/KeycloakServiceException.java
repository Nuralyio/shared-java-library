package com.nuraly.keycloak.client;

/**
 * Exception thrown when Keycloak service operations fail.
 */
public class KeycloakServiceException extends RuntimeException {
    
    private final int statusCode;
    
    public KeycloakServiceException(String message) {
        super(message);
        this.statusCode = -1;
    }
    
    public KeycloakServiceException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }
    
    public KeycloakServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public KeycloakServiceException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public boolean hasStatusCode() {
        return statusCode != -1;
    }
}
