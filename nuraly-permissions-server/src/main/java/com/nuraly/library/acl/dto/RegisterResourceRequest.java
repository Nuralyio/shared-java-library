package com.nuraly.library.acl.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

@Schema(description = "Request to register a new resource with the ACL system")
public class RegisterResourceRequest {
    @Schema(description = "Resource name", required = true)
    @NotBlank(message = "Resource name is required and cannot be blank")
    public String name;
    
    @Schema(description = "Resource description")
    public String description;
    
    @Schema(description = "Resource type (e.g., 'document', 'dashboard', 'function')", required = true)
    @NotBlank(message = "Resource type is required and cannot be blank")
    public String resourceType;
    
    @Schema(description = "External resource ID - reference to the actual resource in other systems", required = true)
    @NotBlank(message = "External ID is required and cannot be blank")
    public String externalId;
    
    @Schema(description = "Owner user ID - if not specified, current user becomes owner")
    public UUID ownerId;
}
