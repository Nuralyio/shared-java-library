package com.nuraly.keycloak.rest;

import com.nuraly.keycloak.dto.EmailToUuidMapping;
import com.nuraly.keycloak.service.KeycloakService;
import org.keycloak.representations.idm.UserRepresentation;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;

@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User Management", description = "Keycloak user lookup and management operations")
public class UserResource {

    private static final Logger LOG = Logger.getLogger(UserResource.class);

    @Inject
    KeycloakService keycloakService;

    @GET
    @Path("/by-email/{email}")
    @Operation(
        summary = "Get user by email address",
        description = "Retrieve a Keycloak user by their email address. Uses caching for improved performance."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User found successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = UserRepresentation.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
        )
    })
    public Response getUserByEmail(
            @Parameter(description = "Email address of the user", required = true, example = "user@example.com")
            @PathParam("email") String email) {
        try {
            Optional<UserRepresentation> user = keycloakService.getUserByEmail(email);
            
            if (user.isPresent()) {
                return Response.ok(user.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"User not found\"}")
                        .build();
            }
        } catch (Exception e) {
            LOG.error("Error retrieving user by email: " + email, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error\"}")
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Operation(
        summary = "Get user by ID",
        description = "Retrieve a Keycloak user by their unique identifier (UUID). Uses caching for improved performance."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User found successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = UserRepresentation.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
        )
    })
    public Response getUserById(
            @Parameter(description = "Unique identifier (UUID) of the user", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathParam("id") String id) {
        try {
            Optional<UserRepresentation> user = keycloakService.getUserById(id);
            
            if (user.isPresent()) {
                return Response.ok(user.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"User not found\"}")
                        .build();
            }
        } catch (Exception e) {
            LOG.error("Error retrieving user by ID: " + id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error\"}")
                    .build();
        }
    }

    @GET
    @Path("/email-to-uuid/{email}")
    @Operation(
        summary = "Get UUID for email address",
        description = "Get the UUID (unique identifier) for a user by their email address. Useful for email-to-UUID mapping operations."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Email to UUID mapping found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found for the provided email",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
        )
    })
    public Response getEmailToUuidMapping(
            @Parameter(description = "Email address to lookup", required = true, example = "user@example.com")
            @PathParam("email") String email) {
        try {
            Optional<String> uuid = keycloakService.getEmailToUuidMapping(email);
            
            if (uuid.isPresent()) {
                return Response.ok("{\"email\": \"" + email + "\", \"uuid\": \"" + uuid.get() + "\"}").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"User not found\"}")
                        .build();
            }
        } catch (Exception e) {
            LOG.error("Error mapping email to UUID: " + email, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error\"}")
                    .build();
        }
    }

    @POST
    @Path("/by-ids")
    @Operation(
        summary = "Get multiple users by IDs",
        description = "Retrieve multiple Keycloak users by providing a list of their unique identifiers (UUIDs). Uses caching for improved performance."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Users retrieved successfully (may include partial results if some IDs are not found)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = UserRepresentation.class)
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
        )
    })
    public Response getUsersByIds(
            @Parameter(description = "List of user UUIDs to retrieve", required = true)
            List<String> ids) {
        try {
            List<UserRepresentation> users = keycloakService.getUsersByIds(ids);
            return Response.ok(users).build();
        } catch (Exception e) {
            LOG.error("Error retrieving users by IDs", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error\"}")
                    .build();
        }
    }

    @POST
    @Path("/emails-to-uuids")
    @Operation(
        summary = "Get UUIDs for multiple email addresses",
        description = "Get the UUIDs (unique identifiers) for multiple users by their email addresses. Returns email-to-UUID mappings for emails that correspond to existing users."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Email to UUID mappings retrieved successfully (may include partial results if some emails are not found)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = EmailToUuidMapping.class)
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
        )
    })
    public Response getUserIdsByEmails(
            @Parameter(description = "List of email addresses to lookup", required = true)
            List<String> emails) {
        try {
            List<EmailToUuidMapping> mappings = keycloakService.getEmailToUuidMappings(emails);
            return Response.ok(mappings).build();
        } catch (Exception e) {
            LOG.error("Error mapping emails to UUIDs", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error\"}")
                    .build();
        }
    }
}
