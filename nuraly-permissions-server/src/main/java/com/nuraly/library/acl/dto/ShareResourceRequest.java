package com.nuraly.library.acl.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Request to share a resource with another user")
public class ShareResourceRequest {
    @Schema(description = "Resource ID to share", required = true)
    public String resourceId;
    
    @Schema(description = "User ID to share the resource with", required = true)
    public UUID targetUserId;
    
    @Schema(description = "Role ID to assign to the user", required = true)
    public UUID roleId;
}
