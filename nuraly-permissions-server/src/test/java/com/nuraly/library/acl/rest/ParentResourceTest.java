package com.nuraly.library.acl.rest;

import com.nuraly.library.acl.model.*;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import jakarta.transaction.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

/**
 * Test suite for parent resource assignment functionality
 */
@QuarkusTest
public class ParentResourceTest {
    
    @Inject
    EntityManager em;
    
    private UUID tenantId;
    private UUID userId;
    private String resourceId;
    private UUID permissionId;
    private UUID roleId;
    
    @Transactional
    public void setupTestDataAndCommit() {
        // Use unique identifiers to avoid conflicts
        String uniqueId = UUID.randomUUID().toString();
        tenantId = UUID.randomUUID();
        
        // Use external user ID instead of creating User entity
        userId = UUID.randomUUID();
        
        // Create permission with unique name (let Hibernate generate the ID)
        Permission permission = Permission.findByName("read");
        if (permission == null) {
            // Fallback: create a permission if system permissions aren't initialized
            permission = new Permission();
            permission.name = "read";
            permission.description = "Read permission";
            permission.isSystemPermission = true;
            permission.persistAndFlush();
        }
        permissionId = permission.id;
        
        // Create role with unique name
        Role role = new Role();
        role.name = "Test Role " + uniqueId;
        role.description = "Test role for testing";
        role.externalTenantId = tenantId;
        role.scope = RoleScope.RESOURCE;
        role.permissions = new java.util.HashSet<>();
        role.permissions.add(permission);
        role.persistAndFlush();
        roleId = role.id;
        
        // Create resource 
        Resource resource = new Resource();
        resource.id = "test-resource-" + uniqueId;
        resource.name = "Test Resource " + uniqueId;
        resource.resourceType = "document";
        resource.externalTenantId = tenantId;
        resource.ownerId = userId;
        resource.persistAndFlush();
        resourceId = resource.id;
        
        // Grant necessary permissions to the test user for authorization checks
        // Since the user is the owner of the resource, they should have admin/share/publish permissions
        Permission adminPermission = Permission.findByName("admin");
        Permission sharePermission = Permission.findByName("share");
        Permission publishPermission = Permission.findByName("publish");
        
        if (adminPermission != null) {
            ResourceGrant adminGrant = new ResourceGrant();
            adminGrant.externalUserId = userId;
            adminGrant.resource = resource;
            adminGrant.permission = adminPermission;
            adminGrant.grantType = GrantType.DIRECT;
            adminGrant.grantedBy = userId;
            adminGrant.externalTenantId = tenantId;
            adminGrant.isActive = true;
            adminGrant.persistAndFlush();
        }
        
        if (sharePermission != null) {
            ResourceGrant shareGrant = new ResourceGrant();
            shareGrant.externalUserId = userId;
            shareGrant.resource = resource;
            shareGrant.permission = sharePermission;
            shareGrant.grantType = GrantType.DIRECT;
            shareGrant.grantedBy = userId;
            shareGrant.externalTenantId = tenantId;
            shareGrant.isActive = true;
            shareGrant.persistAndFlush();
        }
        
        if (publishPermission != null) {
            ResourceGrant publishGrant = new ResourceGrant();
            publishGrant.externalUserId = userId;
            publishGrant.resource = resource;
            publishGrant.permission = publishPermission;
            publishGrant.grantType = GrantType.DIRECT;
            publishGrant.grantedBy = userId;
            publishGrant.externalTenantId = tenantId;
            publishGrant.isActive = true;
            publishGrant.persistAndFlush();
        }
        
        // Ensure all data is committed to database
        em.flush();
        em.clear();
    }
    @BeforeEach
    public void setup() {
        // No-op - data is now created within each test transaction
    }
    
    // Helper method to create test requests with common headers
    private io.restassured.specification.RequestSpecification createTestRequest() {
        return given()
            .header("X-USER", "{\"uuid\":\"" + userId.toString() + "\",\"tenantId\":\"" + tenantId.toString() + "\"}")
            .header("X-Test-Mode", "true")
            .contentType("application/json");
    }

    @Test
    @DisplayName("Test create resource with parent")
    public void testCreateResourceWithParent() {
        setupTestDataAndCommit();
        
        // First create a parent resource (exactly like ACLResourceTest)
        ParentTestRegisterResourceRequest parentRequest = new ParentTestRegisterResourceRequest();
        parentRequest.name = "Parent Document";
        parentRequest.description = "Parent document for testing hierarchy";
        parentRequest.resourceType = "document";
        parentRequest.externalId = "parent-" + UUID.randomUUID();
        // parentResourceId is null - not setting it
        
        Response parentResponse = given()
            .header("X-USER", "{\"uuid\":\"" + userId.toString() + "\",\"tenantId\":\"" + tenantId.toString() + "\"}")
            .header("X-Test-Mode", "true")
            .contentType("application/json")
            .body(parentRequest)
        .when()
            .post("/api/v1/acl/resources")
        .then()
            .statusCode(201)
            .extract().response();
        
        String parentResourceId = parentResponse.jsonPath().getString("id");
        
        // Now create a child resource with the parent
        ParentTestRegisterResourceRequest childRequest = new ParentTestRegisterResourceRequest();
        childRequest.name = "Child Document";
        childRequest.description = "Child document for testing hierarchy";
        childRequest.resourceType = "document";
        childRequest.externalId = "child-" + UUID.randomUUID();
        childRequest.parentResourceId = parentResourceId;
        
        given()
            .header("X-USER", "{\"uuid\":\"" + userId.toString() + "\",\"tenantId\":\"" + tenantId.toString() + "\"}")
            .header("X-Test-Mode", "true")
            .contentType("application/json")
            .body(childRequest)
        .when()
            .post("/api/v1/acl/resources")
        .then()
            .statusCode(201)
            .body("name", equalTo("Child Document"))
            .body("description", equalTo("Child document for testing hierarchy"));
    }

    @Test
    @DisplayName("Test set parent resource after creation")
    public void testSetParentResource() {
        setupTestDataAndCommit();
        
        // Create parent resource
        ParentTestRegisterResourceRequest parentRequest = new ParentTestRegisterResourceRequest();
        parentRequest.name = "Parent Folder";
        parentRequest.resourceType = "folder";
        parentRequest.externalId = "parent-folder-" + UUID.randomUUID();
        
        Response parentResponse = given()
            .header("X-USER", "{\"uuid\":\"" + userId.toString() + "\",\"tenantId\":\"" + tenantId.toString() + "\"}")
            .header("X-Test-Mode", "true")
            .contentType("application/json")
            .body(parentRequest)
        .when()
            .post("/api/v1/acl/resources")
        .then()
            .statusCode(201)
            .extract().response();
        
        String parentResourceId = parentResponse.jsonPath().getString("id");
        
        // Create child resource without parent
        ParentTestRegisterResourceRequest childRequest = new ParentTestRegisterResourceRequest();
        childRequest.name = "Child File";
        childRequest.resourceType = "file";
        childRequest.externalId = "child-file-" + UUID.randomUUID();
        
        Response childResponse = given()
            .header("X-USER", "{\"uuid\":\"" + userId.toString() + "\",\"tenantId\":\"" + tenantId.toString() + "\"}")
            .header("X-Test-Mode", "true")
            .contentType("application/json")
            .body(childRequest)
        .when()
            .post("/api/v1/acl/resources")
        .then()
            .statusCode(201)
            .extract().response();
        
        String childResourceId = childResponse.jsonPath().getString("id");
        
        // Now set the parent resource
        ParentTestSetParentResourceRequest setParentRequest = new ParentTestSetParentResourceRequest();
        setParentRequest.parentResourceId = parentResourceId;
        setParentRequest.reason = "Organizing files into folders";
        
        given()
            .header("X-USER", "{\"uuid\":\"" + userId.toString() + "\",\"tenantId\":\"" + tenantId.toString() + "\"}")
            .header("X-Test-Mode", "true")
            .contentType("application/json")
            .body(setParentRequest)
        .when()
            .put("/api/v1/acl/resources/" + childResourceId + "/parent")
        .then()
            .statusCode(200)
            .body("name", equalTo("Child File"));
    }

    @Test
    @DisplayName("Test get child resources")
    public void testGetChildResources() {
        setupTestDataAndCommit();
        
        // Create parent resource
        ParentTestRegisterResourceRequest parentRequest = new ParentTestRegisterResourceRequest();
        parentRequest.name = "Project Folder";
        parentRequest.resourceType = "project";
        parentRequest.externalId = "project-" + UUID.randomUUID();
        
        Response parentResponse = given()
            .header("X-USER", "{\"uuid\":\"" + userId.toString() + "\",\"tenantId\":\"" + tenantId.toString() + "\"}")
            .header("X-Test-Mode", "true")
            .contentType("application/json")
            .body(parentRequest)
        .when()
            .post("/api/v1/acl/resources")
        .then()
            .statusCode(201)
            .extract().response();
        
        String parentResourceId = parentResponse.jsonPath().getString("id");
        
        // Create multiple child resources
        for (int i = 1; i <= 3; i++) {
            ParentTestRegisterResourceRequest childRequest = new ParentTestRegisterResourceRequest();
            childRequest.name = "Task " + i;
            childRequest.resourceType = "task";
            childRequest.externalId = "task-" + i + "-" + UUID.randomUUID();
            childRequest.parentResourceId = parentResourceId;
            
            given()
                .header("X-USER", "{\"uuid\":\"" + userId.toString() + "\",\"tenantId\":\"" + tenantId.toString() + "\"}")
                .header("X-Test-Mode", "true")
                .contentType("application/json")
                .body(childRequest)
            .when()
                .post("/api/v1/acl/resources")
            .then()
                .statusCode(201);
        }
        
        // Get child resources
        given()
            .header("X-USER", "{\"uuid\":\"" + userId.toString() + "\",\"tenantId\":\"" + tenantId.toString() + "\"}")
            .header("X-Test-Mode", "true")
        .when()
            .get("/api/v1/acl/resources/" + parentResourceId + "/children")
        .then()
            .statusCode(200)
            .body("size()", equalTo(3))
            .body("[0].resourceType", equalTo("task"))
            .body("[1].resourceType", equalTo("task"))
            .body("[2].resourceType", equalTo("task"));
    }

    @Test
    @DisplayName("Test prevent circular reference")
    public void testPreventCircularReference() {
        setupTestDataAndCommit();
        
        // Create two resources
        ParentTestRegisterResourceRequest resource1Request = new ParentTestRegisterResourceRequest();
        resource1Request.name = "Resource 1";
        resource1Request.resourceType = "document";
        resource1Request.externalId = "resource1-" + UUID.randomUUID();
        
        Response resource1Response = given()
            .header("X-USER", "{\"uuid\":\"" + userId.toString() + "\",\"tenantId\":\"" + tenantId.toString() + "\"}")
            .header("X-Test-Mode", "true")
            .contentType("application/json")
            .body(resource1Request)
        .when()
            .post("/api/v1/acl/resources")
        .then()
            .statusCode(201)
            .extract().response();
        
        String resource1Id = resource1Response.jsonPath().getString("id");
        
        ParentTestRegisterResourceRequest resource2Request = new ParentTestRegisterResourceRequest();
        resource2Request.name = "Resource 2";
        resource2Request.resourceType = "document";
        resource2Request.externalId = "resource2-" + UUID.randomUUID();
        resource2Request.parentResourceId = resource1Id; // Set resource1 as parent
        
        Response resource2Response = given()
            .header("X-USER", "{\"uuid\":\"" + userId.toString() + "\",\"tenantId\":\"" + tenantId.toString() + "\"}")
            .header("X-Test-Mode", "true")
            .contentType("application/json")
            .body(resource2Request)
        .when()
            .post("/api/v1/acl/resources")
        .then()
            .statusCode(201)
            .extract().response();
        
        String resource2Id = resource2Response.jsonPath().getString("id");
        
        // Now try to set resource2 as parent of resource1 (would create circular reference)
        ParentTestSetParentResourceRequest setParentRequest = new ParentTestSetParentResourceRequest();
        setParentRequest.parentResourceId = resource2Id;
        setParentRequest.reason = "Testing circular reference prevention";
        
        given()
            .header("X-USER", "{\"uuid\":\"" + userId.toString() + "\",\"tenantId\":\"" + tenantId.toString() + "\"}")
            .header("X-Test-Mode", "true")
            .contentType("application/json")
            .body(setParentRequest)
        .when()
            .put("/api/v1/acl/resources/" + resource1Id + "/parent")
        .then()
            .statusCode(400); // Should fail with bad request
    }

    @Test
    @DisplayName("Test update resource with parent change")
    public void testUpdateResourceWithParentChange() {
        setupTestDataAndCommit();
        
        // Create parent resources
        ParentTestRegisterResourceRequest parent1Request = new ParentTestRegisterResourceRequest();
        parent1Request.name = "Parent 1";
        parent1Request.resourceType = "folder";
        parent1Request.externalId = "parent1-" + UUID.randomUUID();
        
        Response parent1Response = given()
            .header("X-USER", "{\"uuid\":\"" + userId.toString() + "\",\"tenantId\":\"" + tenantId.toString() + "\"}")
            .header("X-Test-Mode", "true")
            .contentType("application/json")
            .body(parent1Request)
        .when()
            .post("/api/v1/acl/resources")
        .then()
            .statusCode(201)
            .extract().response();
        
        String parent1Id = parent1Response.jsonPath().getString("id");
        
        ParentTestRegisterResourceRequest parent2Request = new ParentTestRegisterResourceRequest();
        parent2Request.name = "Parent 2";
        parent2Request.resourceType = "folder";
        parent2Request.externalId = "parent2-" + UUID.randomUUID();
        
        Response parent2Response = given()
            .header("X-USER", "{\"uuid\":\"" + userId.toString() + "\",\"tenantId\":\"" + tenantId.toString() + "\"}")
            .header("X-Test-Mode", "true")
            .contentType("application/json")
            .body(parent2Request)
        .when()
            .post("/api/v1/acl/resources")
        .then()
            .statusCode(201)
            .extract().response();
        
        String parent2Id = parent2Response.jsonPath().getString("id");
        
        // Create child resource with parent1
        ParentTestRegisterResourceRequest childRequest = new ParentTestRegisterResourceRequest();
        childRequest.name = "Child Resource";
        childRequest.resourceType = "file";
        childRequest.externalId = "child-" + UUID.randomUUID();
        childRequest.parentResourceId = parent1Id;
        
        Response childResponse = given()
            .header("X-USER", "{\"uuid\":\"" + userId.toString() + "\",\"tenantId\":\"" + tenantId.toString() + "\"}")
            .header("X-Test-Mode", "true")
            .contentType("application/json")
            .body(childRequest)
        .when()
            .post("/api/v1/acl/resources")
        .then()
            .statusCode(201)
            .extract().response();
        
        String childId = childResponse.jsonPath().getString("id");
        
        // Update child to have parent2 instead
        ParentTestUpdateResourceRequest updateRequest = new ParentTestUpdateResourceRequest();
        updateRequest.name = "Updated Child Resource";
        updateRequest.parentResourceId = parent2Id;
        
        given()
            .header("X-USER", "{\"uuid\":\"" + userId.toString() + "\",\"tenantId\":\"" + tenantId.toString() + "\"}")
            .header("X-Test-Mode", "true")
            .contentType("application/json")
            .body(updateRequest)
        .when()
            .put("/api/v1/acl/resources/" + childId)
        .then()
            .statusCode(200)
            .body("name", equalTo("Updated Child Resource"));
    }
}

// Test DTOs for parent resource tests
class ParentTestSetParentResourceRequest {
    public String parentResourceId;
    public String reason;
}

class ParentTestRegisterResourceRequest {
    public String name;
    public String description;
    public String resourceType;
    public String externalId;
    public UUID ownerId;
    public String parentResourceId;
}

class ParentTestUpdateResourceRequest {
    public String name;
    public String description;
    public String parentResourceId;
}
