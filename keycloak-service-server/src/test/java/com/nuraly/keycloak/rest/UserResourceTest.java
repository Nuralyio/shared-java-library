package com.nuraly.keycloak.rest;

import com.nuraly.keycloak.service.KeycloakService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class UserResourceTest {

    @Test
    public void testHealthEndpoint() {
        given()
                .when().get("/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    public void testUserNotFound() {
        given()
                .when().get("/api/v1/users/by-email/nonexistent@example.com")
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("error", is("User not found"));
    }
}
