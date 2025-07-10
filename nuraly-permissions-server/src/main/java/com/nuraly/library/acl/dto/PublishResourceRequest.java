package com.nuraly.library.acl.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.List;

@Schema(description = "Request to publish a resource for public access")
public class PublishResourceRequest {
    @Schema(description = "Resource ID to publish", required = true)
    public String resourceId;
    
    @Schema(description = "List of permission names to allow for public access", required = true)
    public List<String> permissionNames;
}
