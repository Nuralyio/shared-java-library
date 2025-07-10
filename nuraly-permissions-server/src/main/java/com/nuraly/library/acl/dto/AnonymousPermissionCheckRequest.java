package com.nuraly.library.acl.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Request to check anonymous access to a resource")
public class AnonymousPermissionCheckRequest {
    @Schema(description = "Resource ID to check permission on", required = true)
    public String resourceId;
    
    @Schema(description = "Permission name to check", required = true, example = "read")
    public String permissionName;
    
    @Schema(description = "Tenant ID for tenant-scoped permissions")
    public UUID tenantId;
}
