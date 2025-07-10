package com.nuraly.library.acl.service;

import com.nuraly.library.acl.dto.*;
import com.nuraly.library.acl.model.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing public resource operations
 */
@ApplicationScoped
public class PublicResourceService {
    
    @Inject
    ACLService aclService;
    
    /**
     * Make a resource publicly accessible with specified permissions
     */
    public void publishResource(PublishResourceRequest request, UUID currentUserId, UUID currentTenantId) {
        aclService.publishResource(
            request.resourceId,
            request.permissionNames,
            currentUserId,
            currentTenantId
        );
    }
    
    /**
     * Remove public access from a resource
     */
    public void unpublishResource(UnpublishResourceRequest request, UUID currentUserId, UUID currentTenantId) {
        aclService.unpublishResource(
            request.resourceId,
            currentUserId,
            currentTenantId
        );
    }
    
    /**
     * Get all resources that are publicly accessible
     */
    public List<Resource> getPublicResources() {
        return aclService.getPublicResources();
    }
    
    /**
     * Validate if a public link token is valid for accessing a resource with specific permissions
     */
    public boolean validatePublicLink(String token, String permissionName) {
        return aclService.validatePublicLink(token, permissionName);
    }
    
    /**
     * Retrieve a resource using a public access token for anonymous access
     */
    public Resource getPublicResource(String token) {
        Resource resource = Resource.findByPublicToken(token);
        if (resource != null && resource.isPublicLinkValid()) {
            return resource;
        }
        return null;
    }
}
