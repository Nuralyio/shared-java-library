package com.nuraly.keycloak.client.demo;

import com.nuraly.keycloak.client.KeycloakClient;
import com.nuraly.keycloak.client.model.EmailToUuidMapping;
import com.nuraly.keycloak.client.model.EmailToUuidRequest;
import com.nuraly.keycloak.client.model.EmailToUuidResponse;
import com.nuraly.keycloak.client.model.User;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
import java.util.Optional;

/**
 * Demo REST API for testing Keycloak Service Client functionality.
 * This resource demonstrates all available operations of the KeycloakClient.
 */
@Path("/api/v1/keycloak-demo")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Keycloak Client Demo", description = "Test API for demonstrating Keycloak Service Client functionality")
public class KeycloakDemoResource {

    @Inject
    KeycloakClient keycloakClient;

    @GET
    @Path("/health")
    @Operation(
        summary = "Check Keycloak service health",
        description = "Verifies if the Keycloak service is available and responding"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Service is healthy",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
        ),
        @APIResponse(
            responseCode = "503",
            description = "Service is unavailable",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
        )
    })
    public Response checkHealth() {
        boolean isHealthy = keycloakClient.isServiceHealthy();
        
        HealthCheckResponse response = new HealthCheckResponse(
            isHealthy ? "UP" : "DOWN",
            "keycloak-service",
            isHealthy ? "Service is responding" : "Service is not responding"
        );
        
        return Response
            .status(isHealthy ? 200 : 503)
            .entity(response)
            .build();
    }

    @GET
    @Path("/users/by-email/{email}")
    @Operation(
        summary = "Get user by email",
        description = "Retrieve a user by their email address"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = User.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    public Response getUserByEmail(
        @Parameter(description = "User email address", required = true)
        @PathParam("email") String email) {
        
        Optional<User> user = keycloakClient.getUserByEmail(email);
        
        if (user.isPresent()) {
            return Response.ok(user.get()).build();
        } else {
            return Response.status(404)
                .entity(new ErrorResponse("User not found with email: " + email))
                .build();
        }
    }

    @GET
    @Path("/users/by-id/{id}")
    @Operation(
        summary = "Get user by UUID",
        description = "Retrieve a user by their UUID"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = User.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    public Response getUserById(
        @Parameter(description = "User UUID", required = true)
        @PathParam("id") String id) {
        
        Optional<User> user = keycloakClient.getUserById(id);
        
        if (user.isPresent()) {
            return Response.ok(user.get()).build();
        } else {
            return Response.status(404)
                .entity(new ErrorResponse("User not found with ID: " + id))
                .build();
        }
    }

    @POST
    @Path("/users/by-ids")
    @Operation(
        summary = "Get multiple users by UUIDs",
        description = "Retrieve multiple users by their UUIDs in a single request"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Users retrieved (may be empty if none found)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = User.class, type = org.eclipse.microprofile.openapi.annotations.enums.SchemaType.ARRAY)
            )
        )
    })
    public Response getUsersByIds(
        @Parameter(description = "List of user UUIDs", required = true)
        List<String> userIds) {
        
        List<User> users = keycloakClient.getUsersByIds(userIds);
        return Response.ok(users).build();
    }

    @GET
    @Path("/email-to-uuid/{email}")
    @Operation(
        summary = "Convert email to UUID",
        description = "Get the UUID for a user given their email address"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "UUID found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
        ),
        @APIResponse(
            responseCode = "404",
            description = "Email not found"
        )
    })
    public Response getEmailToUuidMapping(
        @Parameter(description = "User email address", required = true)
        @PathParam("email") String email) {
        
        Optional<String> uuid = keycloakClient.getEmailToUuidMapping(email);
        
        if (uuid.isPresent()) {
            EmailToUuidMapping mapping = new EmailToUuidMapping(email, uuid.get());
            return Response.ok(mapping).build();
        } else {
            return Response.status(404)
                .entity(new ErrorResponse("No UUID found for email: " + email))
                .build();
        }
    }

    @POST
    @Path("/emails-to-uuids")
    @Operation(
        summary = "Convert multiple emails to UUIDs",
        description = "Get UUIDs for multiple email addresses with detailed success/failure information"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Conversion completed (check response for individual results)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = EmailToUuidResponse.class)
            )
        )
    })
    public Response convertEmailsToUuids(
        @Parameter(description = "Email to UUID conversion request", required = true)
        EmailToUuidRequest request) {
        
        EmailToUuidResponse response = keycloakClient.convertEmailsToUuids(request);
        return Response.ok(response).build();
    }

    @POST
    @Path("/emails-to-uuids/simple")
    @Operation(
        summary = "Get UUIDs for emails (simple)",
        description = "Get UUIDs for multiple email addresses as a simple list"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "UUIDs retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
        )
    })
    public Response getUserIdsByEmails(
        @Parameter(description = "List of email addresses", required = true)
        List<String> emails) {
        
        List<String> uuids = keycloakClient.getUserIdsByEmails(emails);
        return Response.ok(uuids).build();
    }

    @POST
    @Path("/emails-to-uuids/detailed")
    @Operation(
        summary = "Get detailed email to UUID mappings",
        description = "Get detailed email to UUID mappings with email-UUID pairs"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Mappings retrieved",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = EmailToUuidMapping.class, type = org.eclipse.microprofile.openapi.annotations.enums.SchemaType.ARRAY)
            )
        )
    })
    public Response getEmailToUuidMappings(
        @Parameter(description = "List of email addresses", required = true)
        List<String> emails) {
        
        List<EmailToUuidMapping> mappings = keycloakClient.getEmailToUuidMappings(emails);
        return Response.ok(mappings).build();
    }

    // Helper classes for responses
    public static class HealthCheckResponse {
        public String status;
        public String service;
        public String message;

        public HealthCheckResponse() {}

        public HealthCheckResponse(String status, String service, String message) {
            this.status = status;
            this.service = service;
            this.message = message;
        }

        // Getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getService() { return service; }
        public void setService(String service) { this.service = service; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ErrorResponse {
        public String error;
        public long timestamp;

        public ErrorResponse() {
            this.timestamp = System.currentTimeMillis();
        }

        public ErrorResponse(String error) {
            this.error = error;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters and setters
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
