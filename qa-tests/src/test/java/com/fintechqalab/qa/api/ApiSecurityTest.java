package com.fintechqalab.qa.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;


/**
 * API security tests: authentication, authorisation and input validation (OWASP basics).
 *
 * Every REST Assured test has the same shape:
 *   given()  -> what we send   (headers, body)
 *   when()   -> the request    (post / get and the path)
 *   then()   -> what we expect (status code, response body)
 *
 * Expected behaviour (the "spec" you test against):
 *   - Every /api/payments endpoint needs a valid "Authorization: Bearer <token>" header.
 *     Without one it must answer 401 {"error":"Missing or invalid token"}.
 *   - POST /api/auth/token with wrong credentials answers 401 and gives NO token.
 *   - Invalid input answers 400 with an error message, never 500.
 *
 * Write the tests against this spec. If a test fails, do not change the expectation:
 * first check whether your test is wrong; if the test is right, you have found a bug.
 *
 * YOUR TURN: for each TODO test, delete @Disabled, write the code, run it.
 */
class ApiSecurityTest extends BaseApiTest {

    // ---- Example: read this first --------------------------------------------------------

    @Test
    @DisplayName("Creating a payment without a token is rejected with 401")
    void createPaymentWithoutTokenIsRejected() {
        given()
                .contentType(ContentType.JSON)
                .body(validPayment())
        .when()
                .post("/api/payments")
        .then()
                .statusCode(401)
                .body("error", equalTo("Missing or invalid token"));
    }

    // ---- Your tests ----------------------------------------------------------------------

    @Test

    @DisplayName("Creating a payment with a fake token is rejected with 401")
    void createPaymentWithFakeTokenIsRejected() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer not-a-real-token")
                .body(validPayment())
                .when()
                .post("/api/payments")
                .then()
                .statusCode(401)
                .body("error", equalTo("Missing or invalid token"));
    }

    @Test

    @DisplayName("Wrong password does not issue a token")
    void wrongPasswordDoesNotIssueToken() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("username", "demo", "password", "wrong"))
                .when()
                .post("/api/auth/token")
                .then()
                .statusCode(401)
                .body("token", nullValue());
    }

    @Test
    @Tag("known-bug")
    @DisplayName("Listing payments without a token is rejected with 401 (BUG-4)")
    void listPaymentsWithoutTokenIsRejected() {
        given()
                .when()
                .get("/api/payments")
                .then()
                .statusCode(401);
    }

    @Test

    @DisplayName("An injection attempt in the IBAN field is rejected with 400")
    void injectionInIbanIsRejected() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + obtainToken())
                .body(Map.of("amount", 100.00, "currency", "EUR", "iban", "' OR 1=1 --"))
                .when()
                .post("/api/payments")
                .then()
                .statusCode(400)
                .body("error", equalTo("iban is missing or invalid"));
    }

    @Test
    @Tag("known-bug")
    @DisplayName("A non-numeric payment id answers 400, not a server error (BUG-5)")
    void nonNumericPaymentIdIsRejected() {
        given()
                .header("Authorization", "Bearer " + obtainToken())
                .when()
                .get("/api/payments/abc")
                .then()
                .statusCode(400);
    }
}
