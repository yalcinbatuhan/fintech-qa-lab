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
    // TODO Week 1: add your tests below.
    // Ideas (equivalence partitioning and boundary value analysis):
    //   - EUR minimum fee (very small amounts) and maximum fee (very large amounts)
    //   - USD / GBP: the same rules with different limits
    //   - instant payments
    //   - amount boundaries: 0.00, 0.01, 50,000.00, 50,000.01, null
    //   - rounding: an amount whose fee has a third decimal of 5
    //   - try @ParameterizedTest with @CsvSource to cover many cases in one test
}
