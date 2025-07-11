package com.nuraly.keycloak.client.demo;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import jakarta.ws.rs.core.Application;

/**
 * Main application class for the Keycloak Client Demo API.
 * This class configures the OpenAPI specification and application metadata.
 */
@OpenAPIDefinition(
    info = @Info(
        title = "Keycloak Service Client Demo API",
        version = "1.0.0-SNAPSHOT",
        description = "Demonstration API for the Keycloak Service Client library. " +
                     "This API showcases all available operations including user lookup, " +
                     "email to UUID conversion, health checks, and batch operations.",
        contact = @Contact(
            name = "Nuraly Development Team",
            email = "developer@nuraly.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(url = "http://localhost:7612", description = "Local development server"),
        @Server(url = "http://localhost:7612", description = "Docker development server")
    }
)
public class KeycloakClientDemoApplication extends Application {
}
