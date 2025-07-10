package com.nuraly.library.acl.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Response containing permission check result")
public class PermissionCheckResponse {
    @Schema(description = "Whether the user has the requested permission")
    public boolean hasPermission;
    
    @Schema(description = "Error message if permission check failed")
    public String errorMessage;
    
    public PermissionCheckResponse() {}
    
    public PermissionCheckResponse(boolean hasPermission, String errorMessage) {
        this.hasPermission = hasPermission;
        this.errorMessage = errorMessage;
    }
}
