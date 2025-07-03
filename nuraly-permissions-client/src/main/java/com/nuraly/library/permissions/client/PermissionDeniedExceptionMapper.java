package com.nuraly.library.permissions.client;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps PermissionDeniedException to HTTP 403 Forbidden response.
 */
@Provider
public class PermissionDeniedExceptionMapper implements ExceptionMapper<PermissionDeniedException> {

    @Override
    public Response toResponse(PermissionDeniedException exception) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity("{\"error\": \"" + exception.getMessage() + "\", \"type\": \"PERMISSION_DENIED\"}")
                .header("Content-Type", "application/json")
                .build();
    }
}
