package com.nuraly.keycloak.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import com.nuraly.keycloak.service.KeycloakService;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Health Check", description = "Service health and readiness endpoints")
public class HealthResource {

    private static final Logger LOG = Logger.getLogger(HealthResource.class);

    @Inject
    KeycloakService keycloakService;

    @GET
    @Operation(
        summary = "Health check",
        description = "Check if the Keycloak service is up and running"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Service is healthy",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
        )
    })
    public Response health() {
        return Response.ok("{\"status\": \"UP\", \"service\": \"keycloak-service\"}").build();
    }

    @GET
    @Path("/ready")
    @Operation(
        summary = "Readiness check",
        description = "Check if the Keycloak service is ready to accept requests"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Service is ready",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
        )
    })
    public Response ready() {
        return Response.ok("{\"status\": \"READY\", \"service\": \"keycloak-service\"}").build();
    }

    @GET
    @Path("/keycloak")
    @Operation(
        summary = "Keycloak connectivity check",
        description = "Check if the service can connect to Keycloak"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Keycloak is accessible",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
        ),
        @APIResponse(
            responseCode = "503",
            description = "Keycloak is not accessible",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
        )
    })
    public Response keycloakHealth() {
        try {
            // Try to get a token to verify connectivity
            String token = keycloakService.getKeycloak().tokenManager().getAccessTokenString();
            
            return Response.ok(String.format(
                "{\"status\": \"UP\", \"keycloak\": \"CONNECTED\", \"timestamp\": %d, \"hasToken\": %b}",
                System.currentTimeMillis(),
                token != null && !token.isEmpty()
            )).build();
        } catch (Exception e) {
            LOG.error("Keycloak health check failed", e);
            return Response.status(503).entity(String.format(
                "{\"status\": \"DOWN\", \"keycloak\": \"DISCONNECTED\", \"timestamp\": %d, \"error\": \"%s\"}",
                System.currentTimeMillis(),
                e.getMessage().replace("\"", "\\\"")
            )).build();
        }
    }
}
