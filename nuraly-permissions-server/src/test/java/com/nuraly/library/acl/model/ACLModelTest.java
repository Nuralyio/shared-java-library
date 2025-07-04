package com.nuraly.library.acl.model;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ACL model entities
 * Tests entity validation, persistence, and relationships
 */
@QuarkusTest
public class ACLModelTest {
    
    
    @Test
    @TestTransaction
    @DisplayName("Should create and persist Permission entity")
    public void testPermissionEntity() {
        // Given
        Permission permission = new Permission();
        permission.name = "test-permission";
        permission.description = "Test permission";
        permission.resourceType = "document";
        permission.isSystemPermission = false;
        
        // When
        permission.persist();
        
        // Then
        assertNotNull(permission.id);
        
        Permission foundPermission = Permission.findById(permission.id);
        assertNotNull(foundPermission);
        assertEquals("test-permission", foundPermission.name);
        assertEquals("Test permission", foundPermission.description);
        assertEquals("document", foundPermission.resourceType);
        assertFalse(foundPermission.isSystemPermission);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should create and persist Role entity with permissions")
    public void testRoleEntity() {
        // Given
        UUID tenantId = UUID.randomUUID();
        
        // Create permission first
        Permission permission = new Permission();
        permission.name = "read";
        permission.description = "Read permission";
        permission.isSystemPermission = true;
        permission.persist();
        
        // Create role
        Role role = new Role();
        role.name = "Test Role";
        role.description = "Test role description";
        role.externalTenantId = tenantId;
        role.scope = RoleScope.RESOURCE;
        role.isSystemRole = false;
        role.permissions = new HashSet<>();
        role.permissions.add(permission);
        
        // When
        role.persist();
        
        // Then
        assertNotNull(role.id);
        assertNotNull(role.createdAt);
        assertNotNull(role.updatedAt);
        
        Role foundRole = Role.findById(role.id);
        assertNotNull(foundRole);
        assertEquals("Test Role", foundRole.name);
        assertEquals(RoleScope.RESOURCE, foundRole.scope);
        assertEquals(tenantId, foundRole.externalTenantId);
        assertFalse(foundRole.isSystemRole);
        assertEquals(1, foundRole.permissions.size());
        assertTrue(foundRole.permissions.contains(permission));
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should create and persist Resource entity")
    public void testResourceEntity() {
        // Given
        UUID tenantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        
        Resource resource = new Resource();
        resource.name = "Test Resource";
        resource.description = "Test resource description";
        resource.resourceType = "document";
        resource.externalTenantId = tenantId;
        resource.ownerId = ownerId;
        resource.organizationId = organizationId;
        resource.isPublic = false;
        resource.isActive = true;
        
        // When
        resource.persist();
        
        // Then
        assertNotNull(resource.id);
        assertNotNull(resource.createdAt);
        assertNotNull(resource.updatedAt);
        
        Resource foundResource = Resource.findById(resource.id);
        assertNotNull(foundResource);
        assertEquals("Test Resource", foundResource.name);
        assertEquals("document", foundResource.resourceType);
        assertEquals(tenantId, foundResource.externalTenantId);
        assertEquals(ownerId, foundResource.ownerId);
        assertEquals(organizationId, foundResource.organizationId);
        assertFalse(foundResource.isPublic);
        assertTrue(foundResource.isActive);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should create and persist Organization entity")
    public void testOrganizationEntity() {
        // Given
        UUID tenantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        
        Organization organization = new Organization();
        organization.name = "Test Organization";
        organization.description = "Test organization description";
        organization.externalTenantId = tenantId;
        organization.ownerId = ownerId;
        organization.isActive = true;
        
        // When
        organization.persist();
        
        // Then
        assertNotNull(organization.id);
        assertNotNull(organization.createdAt);
        assertNotNull(organization.updatedAt);
        
        Organization foundOrg = Organization.findById(organization.id);
        assertNotNull(foundOrg);
        assertEquals("Test Organization", foundOrg.name);
        assertEquals(tenantId, foundOrg.externalTenantId);
        assertEquals(ownerId, foundOrg.ownerId);
        assertTrue(foundOrg.isActive);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should create and persist ResourceGrant entity")
    public void testResourceGrantEntity() {
        // Given - create dependencies
        UUID tenantId = UUID.randomUUID();
        UUID externalUserId = UUID.randomUUID();
        
        Resource resource = new Resource();
        resource.name = "Test Resource";
        resource.resourceType = "document";
        resource.externalTenantId = tenantId;
        resource.ownerId = externalUserId;
        resource.persist();
        
        Permission permission = new Permission();
        permission.name = "read";
        permission.description = "Read permission";
        permission.persist();
        
        // Create resource grant
        ResourceGrant grant = new ResourceGrant();
        grant.externalUserId = externalUserId;
        grant.resource = resource;
        grant.permission = permission;
        grant.grantedBy = externalUserId;
        grant.externalTenantId = tenantId;
        grant.grantType = GrantType.DIRECT;
        grant.expiresAt = LocalDateTime.now().plusDays(30);
        
        // When
        grant.persist();
        
        // Then
        assertNotNull(grant.id);
        assertNotNull(grant.createdAt);
        assertTrue(grant.isValid());
        assertTrue(grant.isActive);
        
        ResourceGrant foundGrant = ResourceGrant.findById(grant.id);
        assertNotNull(foundGrant);
        assertEquals(externalUserId, foundGrant.externalUserId);
        assertEquals(resource.id, foundGrant.resource.id);
        assertEquals(permission.id, foundGrant.permission.id);
        assertEquals(GrantType.DIRECT, foundGrant.grantType);
        assertEquals(tenantId, foundGrant.externalTenantId);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should validate expired ResourceGrant")
    public void testExpiredResourceGrant() {
        // Given - create expired grant
        UUID tenantId = UUID.randomUUID();
        UUID externalUserId = UUID.randomUUID();
        
        Resource resource = new Resource();
        resource.name = "Test Resource";
        resource.resourceType = "document";
        resource.externalTenantId = tenantId;
        resource.ownerId = externalUserId;
        resource.persist();
        
        Permission permission = new Permission();
        permission.name = "read";
        permission.description = "Read permission";
        permission.persist();
        
        ResourceGrant grant = new ResourceGrant();
        grant.externalUserId = externalUserId;
        grant.resource = resource;
        grant.permission = permission;
        grant.grantedBy = externalUserId;
        grant.externalTenantId = tenantId;
        grant.grantType = GrantType.DIRECT;
        grant.expiresAt = LocalDateTime.now().minusHours(1); // Expired
        grant.persist();
        
        // When/Then
        assertFalse(grant.isValid());
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should create and persist OrganizationMembership entity")
    public void testOrganizationMembershipEntity() {
        // Given - create dependencies
        UUID tenantId = UUID.randomUUID();
        UUID externalUserId = UUID.randomUUID();
        
        Organization organization = new Organization();
        organization.name = "Test Organization";
        organization.externalTenantId = tenantId;
        organization.ownerId = externalUserId;
        organization.persist();
        
        Role role = new Role();
        role.name = "Member";
        role.description = "Organization member";
        role.externalTenantId = tenantId;
        role.scope = RoleScope.ORGANIZATION;
        role.persist();
        
        // Create membership
        OrganizationMembership membership = new OrganizationMembership();
        membership.externalUserId = externalUserId;
        membership.organization = organization;
        membership.role = role;
        membership.membershipType = MembershipType.MEMBER;
        membership.externalTenantId = tenantId;
        membership.isActive = true;
        
        // When
        membership.persist();
        
        // Then
        assertNotNull(membership.id);
        assertNotNull(membership.joinedAt);
        assertTrue(membership.isActive);
        
        OrganizationMembership foundMembership = OrganizationMembership.findById(membership.id);
        assertNotNull(foundMembership);
        assertEquals(externalUserId, foundMembership.externalUserId);
        assertEquals(organization.id, foundMembership.organization.id);
        assertEquals(role.id, foundMembership.role.id);
        assertEquals(MembershipType.MEMBER, foundMembership.membershipType);
        assertEquals(tenantId, foundMembership.externalTenantId);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should create and persist AuditLog entity")
    public void testAuditLogEntity() {
        // Given
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        
        AuditLog auditLog = new AuditLog();
        auditLog.externalTenantId = tenantId;
        auditLog.actionType = AuditActionType.PERMISSION_GRANTED;
        auditLog.userId = userId;
        auditLog.resourceId = resourceId;
        auditLog.permissionId = permissionId;
        auditLog.success = true;
        auditLog.ipAddress = "127.0.0.1";
        auditLog.details = "Test audit log";
        
        // When
        auditLog.persist();
        
        // Then
        assertNotNull(auditLog.id);
        assertNotNull(auditLog.createdAt);
        
        AuditLog foundLog = AuditLog.findById(auditLog.id);
        assertNotNull(foundLog);
        assertEquals(AuditActionType.PERMISSION_GRANTED, foundLog.actionType);
        assertEquals(tenantId, foundLog.externalTenantId);
        assertEquals(userId, foundLog.userId);
        assertEquals(resourceId, foundLog.resourceId);
        assertEquals(permissionId, foundLog.permissionId);
        assertTrue(foundLog.success);
        assertEquals("127.0.0.1", foundLog.ipAddress);
        assertEquals("Test audit log", foundLog.details);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should create and persist SharePolicy entity")
    public void testSharePolicyEntity() {
        // Given
        UUID tenantId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        
        SharePolicy sharePolicy = new SharePolicy();
        sharePolicy.name = "Test Policy";
        sharePolicy.description = "Test share policy";
        sharePolicy.tenantId = tenantId;
        sharePolicy.policyType = SharePolicyType.PUBLIC_LINK;
        sharePolicy.accessLevel = AccessLevel.READ_ONLY;
        sharePolicy.permissions = "[\"read\"]";
        sharePolicy.requireAuthentication = false;
        sharePolicy.allowAnonymous = true;
        sharePolicy.maxUses = 100;
        sharePolicy.expiresAfterHours = 24;
        sharePolicy.isSystemPolicy = false;
        sharePolicy.isActive = true;
        sharePolicy.createdBy = createdBy;
        
        // When
        sharePolicy.persist();
        
        // Then
        assertNotNull(sharePolicy.id);
        assertNotNull(sharePolicy.createdAt);
        assertNotNull(sharePolicy.updatedAt);
        
        SharePolicy foundPolicy = SharePolicy.findById(sharePolicy.id);
        assertNotNull(foundPolicy);
        assertEquals("Test Policy", foundPolicy.name);
        assertEquals(SharePolicyType.PUBLIC_LINK, foundPolicy.policyType);
        assertEquals(AccessLevel.READ_ONLY, foundPolicy.accessLevel);
        assertEquals("[\"read\"]", foundPolicy.permissions);
        assertFalse(foundPolicy.requireAuthentication);
        assertTrue(foundPolicy.allowAnonymous);
        assertEquals(100, foundPolicy.maxUses);
        assertEquals(24, foundPolicy.expiresAfterHours);
        assertFalse(foundPolicy.isSystemPolicy);
        assertTrue(foundPolicy.isActive);
        assertEquals(createdBy, foundPolicy.createdBy);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should find entities by tenant ID")
    public void testTenantIsolation() {
        // Given
        UUID tenant1Id = UUID.randomUUID();
        UUID tenant2Id = UUID.randomUUID();
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();
        
        // Create entities for tenant 1
        Resource resource1 = new Resource();
        resource1.name = "Resource 1";
        resource1.resourceType = "document";
        resource1.externalTenantId = tenant1Id;
        resource1.ownerId = user1Id;
        resource1.persist();
        
        // Create entities for tenant 2
        Resource resource2 = new Resource();
        resource2.name = "Resource 2";
        resource2.resourceType = "document";
        resource2.externalTenantId = tenant2Id;
        resource2.ownerId = user2Id;
        resource2.persist();
        
        // When
        List<Resource> tenant1Resources = Resource.findByTenant(tenant1Id);
        List<Resource> tenant2Resources = Resource.findByTenant(tenant2Id);
        
        // Then
        assertEquals(1, tenant1Resources.size());
        assertEquals(1, tenant2Resources.size());
        assertEquals("Resource 1", tenant1Resources.get(0).name);
        assertEquals("Resource 2", tenant2Resources.get(0).name);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should test Permission finder methods")
    public void testPermissionFinders() {
        // Given
        Permission systemPermission = new Permission();
        systemPermission.name = "system-read";
        systemPermission.description = "System read permission";
        systemPermission.isSystemPermission = true;
        systemPermission.persist();
        
        Permission customPermission = new Permission();
        customPermission.name = "custom-read";
        customPermission.description = "Custom read permission";
        customPermission.resourceType = "document";
        customPermission.isSystemPermission = false;
        customPermission.persist();
        
        // When
        Permission foundByName = Permission.findByName("system-read");
        List<Permission> systemPermissions = Permission.findSystemPermissions();
        List<Permission> documentPermissions = Permission.findByResourceType("document");
        
        // Then
        assertNotNull(foundByName);
        assertEquals("system-read", foundByName.name);
        
        assertTrue(systemPermissions.stream().anyMatch(p -> p.name.equals("system-read")));
        assertTrue(documentPermissions.stream().anyMatch(p -> p.name.equals("custom-read")));
    }
}
