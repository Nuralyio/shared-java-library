package com.nuraly.library.acl;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Main application class for Nuraly Permissions API
 * Provides OpenAPI documentation and configuration
 */
@ApplicationPath("/")
@OpenAPIDefinition(
    info = @Info(
        title = "Nuraly Permissions API",
        version = "1.0.0",
        description = "Comprehensive ACL (Access Control List) system for managing permissions, roles, and resource sharing. " +
                     "This API provides a tenant-based permission model that supports:\n\n" +
                     "• **User permissions**: Direct permission grants to users on resources\n" +
                     "• **Role-based access**: Permission inheritance through roles\n" +
                     "• **Public resources**: Anonymous access to published resources\n" +
                     "• **Resource sharing**: Share resources with other users via roles\n" +
                     "• **Tenant isolation**: All permissions are scoped to tenants\n" +
                     "• **Audit trail**: Complete tracking of permission changes\n\n" +
                     "The system has been refactored to remove organization-based logic and now operates " +
                     "on a purely tenant-scoped model for better scalability and clarity.",
        contact = @Contact(
            name = "Nuraly Development Team",
            email = "dev@nuraly.com"
        ),
        license = @License(
            name = "Proprietary",
            url = "https://nuraly.com/license"
        )
    ),
    servers = {
        @Server(url = "http://localhost:6998", description = "Development server"),
        @Server(url = "https://api.nuraly.com", description = "Production server")
    },
    tags = {
        @Tag(
            name = "ACL Management",
            description = "Core access control operations for permissions, roles, and resource sharing"
        ),
        @Tag(
            name = "Public Access",
            description = "Operations for public resource access and anonymous permissions"
        ),
        @Tag(
            name = "Resource Management",
            description = "Operations for managing resource access and sharing"
        )
    }
)
public class NuralyPermissionsApplication extends Application {
    // Quarkus automatically discovers and registers JAX-RS resources
    // No explicit configuration needed here
}
