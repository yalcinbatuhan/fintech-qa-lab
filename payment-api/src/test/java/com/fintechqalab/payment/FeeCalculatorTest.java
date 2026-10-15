package com.fintechqalab.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Week 1 exercise: unit tests for {@link FeeCalculator}.
 *
 * Two example tests are already written. Read the business rules in FeeCalculator's Javadoc
 * and add at least 10 more tests. Hint: the implementation contains 3 bugs.
 * A failing test that shows a real bug is a success, not a mistake.
 */
class FeeCalculatorTest {

    private final FeeCalculator calculator = new FeeCalculator();

    @Test
    @DisplayName("EUR: 0.5% of 1,000.00 is 5.00")
    void eurFeeIsHalfAPercent() {
        BigDecimal fee = calculator.calculateFee(new BigDecimal("1000.00"), "EUR", false);

        assertEquals(new BigDecimal("5.00"), fee);
    }

    @Test
    @DisplayName("Unsupported currency is rejected")
    void unsupportedCurrencyIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateFee(new BigDecimal("100.00"), "JPY", false));
    }


    @Test
    @DisplayName("Turkish Lira is rejected as unsupported currency")
    void unspportedCurrencyIsRejectedBaba() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateFee(new BigDecimal("100.00"), "TRY", false));
    }

    @Test
    @DisplayName("EUR: small amount gets the minimum fee of 0.50")
    void eurSmallAmountGetsMinimumFee() {
        BigDecimal fee = calculator.calculateFee(new BigDecimal("10.00"), "EUR", false);

        assertEquals(new BigDecimal("0.50"), fee);
    }

    @Test
    @DisplayName("EUR: large amount is capped at the maximum fee of 20.00")
    void eurLargeAmountIsCappedAtMaximumFee() {
        BigDecimal fee = calculator.calculateFee(new BigDecimal("10000.00"), "EUR", false);

        assertEquals(new BigDecimal("20.00"), fee);
    }

    @Test
    @DisplayName("Boundary: the maximum allowed amount 50,000.00 is accepted (BUG-1)")
    void maximumAllowedAmountIsAccepted() {
        BigDecimal fee = calculator.calculateFee(new BigDecimal("50000.00"), "EUR", false);

        assertEquals(new BigDecimal("20.00"), fee);
    }

    @Test
    @DisplayName("Boundary: 49,999.99 is accepted and capped at 20.00")
    void justBelowMaximumAmountIsAccepted() {
        BigDecimal fee = calculator.calculateFee(new BigDecimal("49999.99"), "EUR", false);

        assertEquals(new BigDecimal("20.00"), fee);
    }

    @Test
    @DisplayName("Boundary: 50,000.01 is rejected")
    void justAboveMaximumAmountIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateFee(new BigDecimal("50000.01"), "EUR", false));
    }

    @Test
    @DisplayName("USD: 1% of 1,000.00 is 10.00")
    void usdFeeIsOnePercent() {
        BigDecimal fee = calculator.calculateFee(new BigDecimal("1000.00"), "USD", false);

        assertEquals(new BigDecimal("10.00"), fee);
    }

    @Test
    @DisplayName("USD: small amount gets the minimum fee of 1.00")
    void usdSmallAmountGetsMinimumFee() {
        BigDecimal fee = calculator.calculateFee(new BigDecimal("50.00"), "USD", false);

        assertEquals(new BigDecimal("1.00"), fee);
    }

    @Test
    @DisplayName("USD: large amount is capped at the maximum fee of 50.00")
    void usdLargeAmountIsCappedAtMaximumFee() {
        BigDecimal fee = calculator.calculateFee(new BigDecimal("10000.00"), "USD", false);

        assertEquals(new BigDecimal("50.00"), fee);
    }

    @Test
    @DisplayName("Instant: 1.00 is added to the EUR fee")
    void instantPaymentAddsSurcharge() {
        BigDecimal fee = calculator.calculateFee(new BigDecimal("1000.00"), "EUR", true);

        assertEquals(new BigDecimal("6.00"), fee);
    }

    @Test
    @DisplayName("Instant: 1.00 is added after the minimum fee is applied")
    void instantSurchargeIsAddedAfterMinimumFee() {
        BigDecimal fee = calculator.calculateFee(new BigDecimal("10.00"), "EUR", true);

        assertEquals(new BigDecimal("1.50"), fee);
    }

    @Test
    @DisplayName("Rounding: 0.755 is rounded HALF_UP to 0.76 (BUG-3)")
    void roundingAfterMinimumFee() {
        BigDecimal fee = calculator.calculateFee(new BigDecimal("151.00"), "EUR", false);

        assertEquals(new BigDecimal("0.76"), fee);
    }


    // TODO Week 1: add your tests below.
    // Ideas (equivalence partitioning and boundary value analysis):
    //   - EUR minimum fee (very small amounts) and maximum fee (very large amounts)
    //   - USD / GBP: the same rules with different limits
    //   - instant payments
    //   - amount boundaries: 0.00, 0.01, 50,000.00, 50,000.01, null
    //   - rounding: an amount whose fee has a third decimal of 5
    //   - try @ParameterizedTest with @CsvSource to cover many cases in one test
}
