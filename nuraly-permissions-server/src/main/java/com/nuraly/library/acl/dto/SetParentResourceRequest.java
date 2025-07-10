package com.nuraly.library.acl.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Request to set or update parent resource for hierarchical relationships")
public class SetParentResourceRequest {
    @Schema(description = "Parent resource ID - set to null to remove parent relationship")
    public String parentResourceId;
    
    @Schema(description = "Reason for changing parent relationship")
    public String reason;
}
