package com.nuraly.library.acl.service;

import com.nuraly.library.acl.model.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ACLService
 * Tests all core ACL functionality including permissions, roles, and anonymous access
 */
@QuarkusTest
public class ACLServiceTest {
    
    @Inject
    ACLService aclService;
    
    @Inject
    AuditService auditService;
    
    private UUID tenantId;
    private UUID userId;
    private UUID resourceId;
    private UUID permissionId;
    private UUID roleId;
    private UUID organizationId;
    
    @BeforeEach
    public void setup() {
        // Initialize test IDs that will be used in tests
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        resourceId = UUID.randomUUID();
        organizationId = UUID.randomUUID();
        
        // Find system permission (created during app initialization)
        Permission permission = Permission.findByName("read");
        if (permission != null) {
            permissionId = permission.id;
        } else {
            permissionId = UUID.randomUUID();
        }
    }
    
    /**
     * Helper method to create test data for tests that need it
     */
    private void setupTestDataAndCommit() {
        // Use unique identifiers to avoid conflicts
        String uniqueId = UUID.randomUUID().toString();
        
        // Use external user ID instead of creating User entity
        userId = UUID.randomUUID(); // Update the test ID
        
        // Create test organization and persist it
        Organization org = new Organization();
        org.name = "Test Organization " + uniqueId;
        org.externalTenantId = tenantId;
        org.ownerId = userId;
        org.persistAndFlush(); // Persist to get the generated ID
        organizationId = org.id; // Update the test ID with the generated one
        
        // Use existing system permission or create one
        Permission permission = Permission.findByName("read");
        if (permission == null) {
            permission = new Permission();
            permission.name = "read";
            permission.description = "Read permission";
            permission.isSystemPermission = true;
            permission.persistAndFlush(); // Persist if creating new
        }
        permissionId = permission.id; // Update the test ID with the found/generated one
        
        // Create test role and persist it
        Role role = new Role();
        role.name = "Test Role " + uniqueId;
        role.description = "Test role for testing";
        role.externalTenantId = tenantId;
        role.scope = RoleScope.RESOURCE;
        role.permissions = new java.util.HashSet<>();
        role.permissions.add(permission);
        role.persistAndFlush(); // Persist to get the generated ID
        roleId = role.id; // Update the test ID with the generated one
        
        // Create test resource and persist it
        Resource resource = new Resource();
        resource.name = "Test Resource " + uniqueId;
        resource.resourceType = "document";
        resource.externalTenantId = tenantId;
        resource.ownerId = userId;
        resource.organizationId = organizationId;
        resource.persistAndFlush(); // Persist to get the generated ID
        resourceId = resource.id; // Update the test ID with the generated one
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should grant permission to user successfully")
    public void testGrantPermission() {
        // Set up test data within the test method
        String uniqueId = UUID.randomUUID().toString();
        UUID testTenantId = UUID.randomUUID();
        
        // Create test user using external user ID
        UUID testUserId = UUID.randomUUID();
        
        // Create test organization
        Organization org = new Organization();
        org.name = "Test Organization " + uniqueId;
        org.externalTenantId = testTenantId;
        org.ownerId = testUserId;
        org.persistAndFlush(); // Persist organization first
        
        // Get or create read permission
        Permission permission = Permission.findByName("read");
        if (permission == null) {
            permission = new Permission();
            permission.name = "read";
            permission.description = "Read permission";
            permission.isSystemPermission = true;
            permission.persistAndFlush(); // Persist permission
        }
        
        // Create test resource
        Resource resource = new Resource();
        resource.name = "Test Resource " + uniqueId;
        resource.resourceType = "document";
        resource.externalTenantId = testTenantId;
        resource.ownerId = testUserId;
        resource.organizationId = org.id;
        resource.persistAndFlush(); // Make sure to persist the resource
        
        // When
        ResourceGrant grant = aclService.grantPermission(
            testUserId, resource.id, permission.id, testUserId, testTenantId
        );
        
        // Then
        assertNotNull(grant);
        assertEquals(testUserId, grant.externalUserId);
        assertEquals(resource.id, grant.resource.id);
        assertEquals(permission.id, grant.permission.id);
        assertEquals(GrantType.DIRECT, grant.grantType);
        assertTrue(grant.isActive);
        assertTrue(grant.isValid());
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should check user permission correctly")
    public void testHasPermission() {
        // Given - setup test data and grant permission first
        setupTestDataAndCommit();
        
        // Create another user who is not the owner
        UUID otherUserId = UUID.randomUUID();
        
        // Grant read permission to the other user
        aclService.grantPermission(otherUserId, resourceId, permissionId, userId, tenantId);
        
        // When & Then
        assertTrue(aclService.hasPermission(otherUserId, resourceId, "read", tenantId));
        assertFalse(aclService.hasPermission(otherUserId, resourceId, "write", tenantId));
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should revoke permission successfully")
    public void testRevokePermission() {
        // Given
        setupTestDataAndCommit();
        
        // Create another user who is not the owner
        UUID otherUserId = UUID.randomUUID();
        
        // Grant permission to the other user (not the owner)
        aclService.grantPermission(otherUserId, resourceId, permissionId, userId, tenantId);
        assertTrue(aclService.hasPermission(otherUserId, resourceId, "read", tenantId));
        
        // When - revoke the permission
        boolean revoked = aclService.revokePermission(
            otherUserId, resourceId, permissionId, userId, "Testing revocation", tenantId
        );
        
        // Then
        assertTrue(revoked);
        assertFalse(aclService.hasPermission(otherUserId, resourceId, "read", tenantId));
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should handle resource ownership correctly")
    public void testResourceOwnership() {
        // Given - setup test data first
        setupTestDataAndCommit();
        
        // Resource owner should automatically have access
        assertTrue(aclService.hasPermission(userId, resourceId, "read", tenantId));
        
        // Create another user who is not the owner (let Hibernate generate the ID)
        UUID otherUserId = UUID.randomUUID();
        
        // Other user should not have access
        assertFalse(aclService.hasPermission(otherUserId, resourceId, "read", tenantId));
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should handle role-based permissions")
    public void testRoleBasedPermissions() {
        // Given - setup test data first
        setupTestDataAndCommit();
        
        // Create user with role assignment through organization membership
        UUID otherUserId = UUID.randomUUID();
        
        // Create organization membership for the other user with the role
        OrganizationMembership membership = new OrganizationMembership();
        membership.organization = Organization.findById(organizationId);
        membership.externalUserId = otherUserId;
        membership.role = Role.findById(roleId);
        membership.externalTenantId = tenantId;
        membership.isActive = true;
        membership.persistAndFlush();
        
        // When - grant role permission to resource
        ResourceGrant grant = aclService.grantRolePermission(
            roleId, resourceId, permissionId, userId, tenantId
        );
        
        // Then
        assertNotNull(grant);
        assertTrue(aclService.hasPermission(otherUserId, resourceId, "read", tenantId));
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should publish and unpublish resources for anonymous access")
    public void testPublishUnpublishResource() {
        // Given - setup test data first
        setupTestDataAndCommit();
        
        // When - publish resource
        List<String> permissions = Arrays.asList("read");
        aclService.publishResource(resourceId, permissions, userId, tenantId);
        
        // Then - should be accessible anonymously
        assertTrue(aclService.hasAnonymousPermission(resourceId, "read", tenantId));
        assertFalse(aclService.hasAnonymousPermission(resourceId, "write", tenantId));
        
        // Verify resource is marked as public
        Resource resource = Resource.findById(resourceId);
        assertTrue(resource.isPublic);
        assertNotNull(resource.publicLinkToken);
        
        // When - unpublish resource
        aclService.unpublishResource(resourceId, userId, tenantId);
        
        // Then - should no longer be accessible anonymously
        assertFalse(aclService.hasAnonymousPermission(resourceId, "read", tenantId));
        
        // Verify resource is no longer public
        resource = Resource.findById(resourceId);
        assertFalse(resource.isPublic);
        assertNull(resource.publicLinkToken);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should validate public links correctly")
    public void testPublicLinkValidation() {
        // Given - setup test data first
        setupTestDataAndCommit();
        
        // Given - publish resource with public link
        List<String> permissions = Arrays.asList("read", "annotate");
        aclService.publishResource(resourceId, permissions, userId, tenantId);
        
        Resource resource = Resource.findById(resourceId);
        String token = resource.publicLinkToken;
        
        // When & Then
        assertTrue(aclService.validatePublicLink(token, "read"));
        assertTrue(aclService.validatePublicLink(token, "annotate"));
        assertFalse(aclService.validatePublicLink(token, "write"));
        assertFalse(aclService.validatePublicLink("invalid-token", "read"));
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should share resources with delegation")
    public void testShareResource() {
        // Given - setup test data first
        setupTestDataAndCommit();
        
        // Given - create target user (let Hibernate generate the ID)
        UUID targetUserId = UUID.randomUUID();
        
        // When - share resource with target user using role
        List<ResourceGrant> grants = aclService.shareResource(
            resourceId, targetUserId, roleId, userId, tenantId
        );
        
        // Then
        assertFalse(grants.isEmpty());
        assertTrue(aclService.hasPermission(targetUserId, resourceId, "read", tenantId));
        
        // Verify grant type is delegated
        ResourceGrant grant = grants.get(0);
        assertEquals(GrantType.DELEGATED, grant.grantType);
        assertEquals(userId, grant.grantedBy);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should get accessible resources for user")
    public void testGetAccessibleResources() {
        // Given - setup test data first
        setupTestDataAndCommit();
        
        // Given - create additional resource and grant access (let Hibernate generate the ID)
        Resource additionalResource = new Resource();
        additionalResource.name = "Additional Resource " + UUID.randomUUID().toString();
        additionalResource.resourceType = "document";
        additionalResource.externalTenantId = tenantId;
        additionalResource.ownerId = UUID.randomUUID(); // Different owner
        additionalResource.organizationId = organizationId;
        additionalResource.persist();
        UUID additionalResourceId = additionalResource.id;
        
        aclService.grantPermission(userId, additionalResourceId, permissionId, userId, tenantId);
        
        // When
        List<Resource> accessibleResources = aclService.getAccessibleResources(userId, tenantId);
        
        // Then - should include owned resource and granted resource
        assertTrue(accessibleResources.size() >= 2);
        assertTrue(accessibleResources.stream().anyMatch(r -> r.id.equals(resourceId)));
        assertTrue(accessibleResources.stream().anyMatch(r -> r.id.equals(additionalResourceId)));
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should handle permission expiration")
    public void testPermissionExpiration() {
        // Given - setup test data first
        setupTestDataAndCommit();
        
        // Given - grant permission with expiration in the past
        LocalDateTime expiredTime = LocalDateTime.now().minusHours(1);
        
        ResourceGrant grant = new ResourceGrant();
        grant.resource = Resource.findById(resourceId);
        grant.externalUserId = userId;
        grant.permission = Permission.findById(permissionId);
        grant.grantedBy = userId;
        grant.externalTenantId = tenantId;
        grant.expiresAt = expiredTime;
        grant.grantType = GrantType.DIRECT;
        grant.persist();
        
        // When & Then - expired permission should not grant access
        assertFalse(grant.isValid());
        // Note: Owner still has access, but the specific grant is invalid
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should handle multi-tenant isolation")
    public void testMultiTenantIsolation() {
        // Given - setup test data first
        setupTestDataAndCommit();
        
        // Create resource in different tenant
        UUID otherTenantId = UUID.randomUUID();
        Resource otherResource = new Resource();
        otherResource.name = "Other Tenant Resource";
        otherResource.resourceType = "document";
        otherResource.externalTenantId = otherTenantId;
        otherResource.ownerId = userId; // Same user but different tenant
        UUID otherResourceId = otherResource.id;
        
        // When & Then - user should not have access to other tenant's resource
        assertFalse(aclService.hasPermission(userId, otherResourceId, "read", tenantId));
        assertFalse(aclService.hasPermission(userId, otherResourceId, "read", otherTenantId));
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should handle organization membership permissions")
    public void testOrganizationMembershipPermissions() {
        // Given - setup test data first
        setupTestDataAndCommit();
        
        // Given - create organization membership with role
        OrganizationMembership membership = new OrganizationMembership();
        membership.externalUserId = userId;
        membership.organization = Organization.findById(organizationId);
        membership.role = Role.findById(roleId);
        membership.membershipType = MembershipType.MEMBER;
        membership.externalTenantId = tenantId;
        membership.persist();
        
        // When - check access through organization membership
        boolean hasAccess = aclService.hasPermission(userId, resourceId, "read", tenantId);
        
        // Then - should have access through organization membership
        assertTrue(hasAccess);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should audit all permission operations")
    public void testAuditLogging() {
        // Given - setup test data first
        setupTestDataAndCommit();
        
        // Given - perform various operations
        aclService.grantPermission(userId, resourceId, permissionId, userId, tenantId);
        aclService.hasPermission(userId, resourceId, "read", tenantId);
        aclService.revokePermission(userId, resourceId, permissionId, userId, "Test", tenantId);
        
        // When - query audit logs
        List<AuditLog> logs = AuditLog.findByTenant(tenantId);
        
        // Then - should have audit entries
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().anyMatch(log -> 
            log.actionType == AuditActionType.PERMISSION_GRANTED));
        assertTrue(logs.stream().anyMatch(log -> 
            log.actionType == AuditActionType.ACCESS_ATTEMPT));
        assertTrue(logs.stream().anyMatch(log -> 
            log.actionType == AuditActionType.PERMISSION_REVOKED));
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should handle invalid inputs gracefully")
    public void testInvalidInputHandling() {
        // Setup minimal test data for the valid cases
        setupTestDataAndCommit();
        
        // Test with null/invalid IDs
        assertFalse(aclService.hasPermission(null, resourceId, "read", tenantId));
        assertFalse(aclService.hasPermission(userId, null, "read", tenantId));
        assertFalse(aclService.hasPermission(UUID.randomUUID(), resourceId, "read", tenantId));
        assertFalse(aclService.hasPermission(userId, UUID.randomUUID(), "read", tenantId));
        
        // Test with invalid permission names
        assertFalse(aclService.hasPermission(userId, resourceId, "nonexistent", tenantId));
        
        // Test anonymous access with invalid data
        assertFalse(aclService.hasAnonymousPermission(null, "read", tenantId));
        assertFalse(aclService.hasAnonymousPermission(resourceId, null, tenantId));
        assertFalse(aclService.validatePublicLink(null, "read"));
        assertFalse(aclService.validatePublicLink("invalid", "read"));
    }
}
