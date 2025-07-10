package com.nuraly.library.acl.util;

import com.nuraly.library.acl.dto.ErrorResponse;
import com.nuraly.library.acl.service.ValidationResult;
import jakarta.ws.rs.core.Response;
import java.util.function.Supplier;

/**
 * Utility class for handling validation chains and error responses
 */
public class ResponseUtil {
    
    /**
     * Execute an operation with a chain of validations
     * @param operation The operation to execute if all validations pass
     * @param validations The validation functions to execute in order
     * @return Response from operation or first validation error
     */
    @SafeVarargs
    public static Response executeWithValidation(
            Supplier<Response> operation,
            Supplier<ValidationResult>... validations) {
        
        // Execute all validations first
        for (Supplier<ValidationResult> validation : validations) {
            ValidationResult result = validation.get();
            if (!result.isValid()) {
                return result.getErrorResponse();
            }
        }
        
        // If all validations pass, execute the operation
        try {
            return operation.get();
        } catch (IllegalArgumentException e) {
            return createBadRequestResponse(e.getMessage());
        } catch (SecurityException e) {
            return createForbiddenResponse(e.getMessage());
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
    
    /**
     * Creates a standardized error response for exceptions
     */
    public static Response createErrorResponse(Exception e) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(new ErrorResponse(e.getMessage()))
            .build();
    }
    
    /**
     * Creates a standardized bad request response
     */
    public static Response createBadRequestResponse(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(new ErrorResponse(message))
            .build();
    }
    
    /**
     * Creates a standardized forbidden response
     */
    public static Response createForbiddenResponse(String message) {
        return Response.status(Response.Status.FORBIDDEN)
            .entity(new ErrorResponse(message))
            .build();
    }
    
    /**
     * Creates a standardized unauthorized response
     */
    public static Response createUnauthorizedResponse(String message) {
        return Response.status(Response.Status.UNAUTHORIZED)
            .entity(new ErrorResponse(message))
            .build();
    }
    
    /**
     * Creates a standardized not found response
     */
    public static Response createNotFoundResponse(String message) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity(new ErrorResponse(message))
            .build();
    }
}
