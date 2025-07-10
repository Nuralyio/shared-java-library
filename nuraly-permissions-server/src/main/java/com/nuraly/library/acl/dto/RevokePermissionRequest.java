package com.nuraly.library.acl.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Request to revoke a permission from a user on a resource")
public class RevokePermissionRequest {
    @Schema(description = "User ID to revoke permission from", required = true)
    public UUID userId;
    
    @Schema(description = "Resource ID to revoke permission on", required = true)
    public String resourceId;
    
    @Schema(description = "Permission ID to revoke", required = true)
    public UUID permissionId;
    
    @Schema(description = "Reason for revoking the permission")
    public String reason;
}
