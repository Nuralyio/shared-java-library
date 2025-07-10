package com.nuraly.library.acl.service;

import jakarta.ws.rs.core.Response;

/**
 * Result class for validation operations
 */
public class ValidationResult {
    private final boolean valid;
    private final Response errorResponse;
    
    private ValidationResult(boolean valid, Response errorResponse) {
        this.valid = valid;
        this.errorResponse = errorResponse;
    }
    
    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }
    
    public static ValidationResult error(Response response) {
        return new ValidationResult(false, response);
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public Response getErrorResponse() {
        return errorResponse;
    }
}
