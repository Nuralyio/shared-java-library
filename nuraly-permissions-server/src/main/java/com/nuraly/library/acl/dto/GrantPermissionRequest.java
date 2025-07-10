package com.nuraly.library.acl.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Request to grant a permission to a user on a resource")
public class GrantPermissionRequest {
    @Schema(description = "User ID to grant permission to", required = true)
    public UUID userId;
    
    @Schema(description = "Resource ID to grant permission on", required = true)
    public String resourceId;
    
    @Schema(description = "Permission ID to grant", required = true)
    public UUID permissionId;
    
    @Schema(description = "Optional expiration date for the permission")
    public LocalDateTime expiresAt;
}
