package com.nuraly.library.acl.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Request to unpublish a resource (remove public access)")
public class UnpublishResourceRequest {
    @Schema(description = "Resource ID to unpublish", required = true)
    public String resourceId;
}
