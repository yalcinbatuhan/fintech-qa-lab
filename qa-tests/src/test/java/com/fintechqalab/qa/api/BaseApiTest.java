package com.fintechqalab.qa.api;

import com.fintechqalab.payment.PaymentApiApplication;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Shared setup for every REST Assured API test.
 *
 * Before the tests of a class: starts the payment API on a free port and points REST Assured at it.
 * After: stops the API again.
 */
public abstract class BaseApiTest {

    protected static final String VALID_IBAN = "IE29AIBK93115212345678";

    private static PaymentApiApplication app;

    @BeforeAll
    static void startApi() throws IOException {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        app = new PaymentApiApplication();
        app.start(port);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterAll
    static void stopApi() {
        if (app != null) {
            app.stop();
        }
        RestAssured.reset();
    }

    /** Signs in with the demo user and returns a valid bearer token. */
    protected static String obtainToken() {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of("username", "demo", "password", "demo123"))
        .when()
                .post("/api/auth/token")
        .then()
                .statusCode(200)
                .extract().path("token");
    }

    /** A valid payment request body. */
    protected static Map<String, Object> validPayment() {
        return Map.of("amount", 100.00, "currency", "EUR", "iban", VALID_IBAN, "instant", false);
    }
}
