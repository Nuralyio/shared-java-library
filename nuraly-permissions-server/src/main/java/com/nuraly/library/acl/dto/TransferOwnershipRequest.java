package com.nuraly.library.acl.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Request to transfer resource ownership")
public class TransferOwnershipRequest {
    @Schema(description = "New owner user ID", required = true)
    public UUID newOwnerId;
    
    @Schema(description = "Reason for ownership transfer")
    public String reason;
}
