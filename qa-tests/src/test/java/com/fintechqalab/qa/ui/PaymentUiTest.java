package com.fintechqalab.qa.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * YOUR TURN: Playwright UI tests for the payment form.
 *
 * For each test:
 *   1. Delete the @Disabled line.
 *   2. Replace the TODO comments with code. LoginUiTest shows every command you need.
 *   3. Run the test (right-click the method in IntelliJ, choose Run) and make it pass.
 *
 * Useful commands:
 *   page.getByTestId("amount").fill("1000.00");
 *   page.getByTestId("currency").selectOption("USD");
 *   page.getByTestId("instant").check();
 *   page.getByTestId("pay-button").click();
 *   assertThat(page.getByTestId("result-fee")).hasText("5.00 EUR");
 *   assertThat(page.getByTestId("payment-row").last()).containsText("250.00");
 *
 * Note: all tests in this class share one running API, so payments created by other tests
 * stay in the list. Check the row you created, not the total number of rows.
 *
 * Fee rules: docs/ucret-kurallari-ozet.md
 * A valid IBAN to use: IE29AIBK93115212345678
 */
class PaymentUiTest extends BaseUiTest {

    @BeforeEach
    void signIn() {
        loginAsDemoUser();
    }

    @Test
    @DisplayName("A 1,000.00 EUR payment is created with a 5.00 EUR fee")
    void eurPaymentShowsCorrectFee() {
        page.getByTestId("amount").fill("1000.00");
        page.getByTestId("iban").fill("IE29AIBK93115212345678");
        page.getByTestId("pay-button").click();

        assertThat(page.getByTestId("result-fee")).hasText("5.00 EUR");
        assertThat(page.getByTestId("result-status")).hasText("CREATED");
    }

    @Test
    @DisplayName("An invalid IBAN shows an error and no payment is created")
    void invalidIbanShowsError() {
      page.getByTestId("amount").fill("100.00");
      page.getByTestId("iban").fill("IE00INVALID");
      page.getByTestId("pay-button").click();

        assertThat(page.getByTestId("payment-error")).hasText("iban is missing or invalid");
        assertThat(page.getByTestId("payment-result")).isHidden();
    }

    @Test
    @DisplayName("An instant USD payment adds the 1.00 surcharge")
    void instantUsdPaymentAddsSurcharge() {
        page.getByTestId("amount").fill("1000.00");
        page.getByTestId("currency").selectOption("USD");
        page.getByTestId("iban").fill("IE29AIBK93115212345678");
        page.getByTestId("instant").check();
        page.getByTestId("pay-button").click();


        assertThat(page.getByTestId("result-fee")).hasText("11.00 USD");
    }

    @Test
    @DisplayName("A created payment appears in the payments list")
    void createdPaymentAppearsInList() {
      page.getByTestId("amount").fill("250.00");
      page.getByTestId("iban").fill("IE29AIBK93115212345678");
      page.getByTestId("pay-button").click();


        assertThat(page.getByTestId("payment-row").last()).containsText("250.00");
    }
}
