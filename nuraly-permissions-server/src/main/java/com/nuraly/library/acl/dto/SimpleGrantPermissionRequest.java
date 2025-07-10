package com.nuraly.library.acl.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Simplified request to grant a permission using current user context")
public class SimpleGrantPermissionRequest {
    @Schema(description = "User ID to grant permission to", required = true)
    public UUID targetUserId;
    
    @Schema(description = "Resource ID to grant permission on", required = true)
    public String resourceId;
    
    @Schema(description = "Permission ID to grant", required = true)
    public UUID permissionId;
    
    @Schema(description = "Optional expiration date for the permission")
    public LocalDateTime expiresAt;
}
