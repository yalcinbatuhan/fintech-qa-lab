package com.fintechqalab.qa.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Example UI tests: read these first, then write your own in PaymentUiTest.
 *
 * Pattern of every UI test:
 *   1. Arrange: get the page into the right state (for example, sign in)
 *   2. Act:     do what the user does (fill fields, click a button)
 *   3. Assert:  check what the user should see (assertThat(...).isVisible() / hasText(...))
 *
 * Elements are found by their data-testid attribute: page.getByTestId("login-button").
 * The test ids are in payment-api/src/main/resources/web/index.html.
 */
class LoginUiTest extends BaseUiTest {

    @Test
    @DisplayName("Valid credentials open the payment form")
    void validLoginShowsPaymentForm() {
        // Act
        page.getByTestId("username").fill("demo");
        page.getByTestId("password").fill("demo123");
        page.getByTestId("login-button").click();

        // Assert
        assertThat(page.getByTestId("payment-form")).isVisible();
        assertThat(page.getByTestId("login-form")).isHidden();
    }

    @Test
    @DisplayName("Wrong password shows an error and keeps the user on the sign-in form")
    void wrongPasswordShowsError() {
        // Act
        page.getByTestId("username").fill("demo");
        page.getByTestId("password").fill("wrong-password");
        page.getByTestId("login-button").click();

        // Assert
        assertThat(page.getByTestId("login-error")).hasText("Invalid username or password");
        assertThat(page.getByTestId("payment-form")).isHidden();
    }
}
