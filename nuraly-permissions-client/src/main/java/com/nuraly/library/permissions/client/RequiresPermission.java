package com.nuraly.library.permissions.client;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Annotation for declarative permission checking on methods.
 * 
 * Usage:
 * <pre>
 * &#64;RequiresPermission(
 *     permissionType = "read",
 *     resourceType = "document", 
 *     resourceId = "#{id}"
 * )
 * public Response getDocument(@PathParam("id") String id) {
 *     // Method body - permission already checked
 * }
 * </pre>
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequiresPermission {
    /**
     * The permission type required (e.g., "read", "write", "delete", "share")
     */
    String permissionType();
    
    /**
     * The type of resource being accessed (e.g., "document", "function", "api")
     */
    String resourceType();
    
    /**
     * The resource identifier. Supports dynamic resolution with #{paramName} syntax
     * Examples:
     * - Static: "resource123"
     * - Dynamic: "#{id}" (resolves from path parameter)
     * - Dynamic: "#{documentId}" (resolves from query parameter)
     */
    String resourceId();
}
