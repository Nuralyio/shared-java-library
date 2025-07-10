package com.nuraly.library.acl.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Error response")
public class ErrorResponse {
    @Schema(description = "Error message")
    public String error;
    
    public ErrorResponse() {}
    
    public ErrorResponse(String error) {
        this.error = error;
    }
}
