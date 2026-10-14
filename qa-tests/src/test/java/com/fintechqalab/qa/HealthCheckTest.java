package com.fintechqalab.qa;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.net.URI;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Week 2 preview: the first API test with REST Assured.
 * Start PaymentApiApplication first. If the API is not running, this test is skipped, not failed.
 */
class HealthCheckTest {

    private static final String BASE_URI = System.getProperty("baseUri", "http://localhost:8080");

    @BeforeAll
    static void setUp() {
        assumeTrue(isApiRunning(), "Payment API is not running on " + BASE_URI + " - start PaymentApiApplication first");
        RestAssured.baseURI = BASE_URI;
    }

    @Test
    @DisplayName("Health endpoint reports UP")
    void healthEndpointIsUp() {
        given()
        .when()
                .get("/health")
        .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    private static boolean isApiRunning() {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(BASE_URI + "/health").toURL().openConnection();
            connection.setConnectTimeout(1000);
            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
