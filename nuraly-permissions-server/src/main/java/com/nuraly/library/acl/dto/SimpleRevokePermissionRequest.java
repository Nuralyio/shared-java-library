package com.nuraly.library.acl.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Simplified request to revoke a permission using current user context")
public class SimpleRevokePermissionRequest {
    @Schema(description = "User ID to revoke permission from", required = true)
    public UUID targetUserId;
    
    @Schema(description = "Resource ID to revoke permission on", required = true)
    public String resourceId;
    
    @Schema(description = "Permission ID to revoke", required = true)
    public UUID permissionId;
    
    @Schema(description = "Reason for revoking the permission")
    public String reason;
}
