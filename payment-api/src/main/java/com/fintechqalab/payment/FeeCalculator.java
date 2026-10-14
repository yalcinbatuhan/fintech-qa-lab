package com.fintechqalab.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates the fee charged for an outgoing payment.
 *
 * <h2>Business rules (the specification you test against)</h2>
 * <ol>
 *   <li>The amount must be greater than 0.00 and at most 50,000.00 (inclusive).
 *       Otherwise an {@link IllegalArgumentException} is thrown.</li>
 *   <li>EUR payments: fee is 0.5% of the amount, never less than 0.50 and never more than 20.00.</li>
 *   <li>USD and GBP payments: fee is 1% of the amount, never less than 1.00 and never more than 50.00.</li>
 *   <li>Any other currency (or a missing currency) throws an {@link IllegalArgumentException}.</li>
 *   <li>Instant payments add a flat 1.00 on top of the fee, after the minimum/maximum rules are applied.</li>
 *   <li>The result always has 2 decimal places and is rounded HALF_UP (0.755 becomes 0.76).</li>
 * </ol>
 *
 * The implementation below may not follow the specification exactly. Your job is to find out where.
 */
public class FeeCalculator {

    static final BigDecimal MAX_AMOUNT = new BigDecimal("50000.00");

    private static final BigDecimal EUR_RATE = new BigDecimal("0.005");
    private static final BigDecimal EUR_MIN_FEE = new BigDecimal("0.50");
    private static final BigDecimal EUR_MAX_FEE = new BigDecimal("20.00");

    private static final BigDecimal FX_RATE = new BigDecimal("0.01");
    private static final BigDecimal FX_MIN_FEE = new BigDecimal("1.00");
    private static final BigDecimal FX_MAX_FEE = new BigDecimal("50.00");

    private static final BigDecimal INSTANT_SURCHARGE = new BigDecimal("1.00");

    public BigDecimal calculateFee(BigDecimal amount, String currency, boolean instant) {
        if (amount == null || amount.signum() <= 0 || amount.compareTo(MAX_AMOUNT) >= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0 and at most " + MAX_AMOUNT);
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency is required");
        }

        BigDecimal fee = switch (currency) {
            case "EUR" -> clamp(amount.multiply(EUR_RATE), EUR_MIN_FEE, EUR_MAX_FEE);
            case "USD", "GBP" -> amount.multiply(FX_RATE).max(FX_MIN_FEE);
            default -> throw new IllegalArgumentException("Unsupported currency: " + currency);
        };

        if (instant) {
            fee = fee.add(INSTANT_SURCHARGE);
        }
        return fee.setScale(2, RoundingMode.DOWN);
    }

    private static BigDecimal clamp(BigDecimal value, BigDecimal min, BigDecimal max) {
        return value.max(min).min(max);
    }
}
