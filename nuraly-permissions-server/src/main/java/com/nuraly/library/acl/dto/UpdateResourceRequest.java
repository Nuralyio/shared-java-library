package com.nuraly.library.acl.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Request to update resource metadata")
public class UpdateResourceRequest {
    @Schema(description = "New resource name")
    public String name;
    
    @Schema(description = "New resource description")
    public String description;
}
