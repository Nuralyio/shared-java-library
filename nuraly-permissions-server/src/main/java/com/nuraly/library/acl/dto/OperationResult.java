package com.nuraly.library.acl.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Generic operation result")
public class OperationResult {
    @Schema(description = "Whether the operation was successful")
    public boolean success;
    
    @Schema(description = "Message describing the operation result")
    public String message;
    
    public OperationResult() {}
    
    public OperationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
