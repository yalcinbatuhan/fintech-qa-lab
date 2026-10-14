# Bug Reports – FeeCalculator

## BUG-1: Maximum allowed amount (50,000.00) is rejected

| Field | Value |
| --- | --- |
| Component | payment-api / FeeCalculator |
| Severity | Major |
| Priority | High |
| Found by | Unit test `maximumAllowedAmountIsAccepted` |
| Status | Open |

**Steps to reproduce**
1. Call `calculateFee(50000.00, "EUR", false)`.

**Expected result**
The amount is accepted, because the rule says the amount must be "at most 50,000.00 (inclusive)".
The fee is 20.00 (0.5% of 50,000.00 = 250.00, capped at the EUR maximum of 20.00).

**Actual result**
`IllegalArgumentException: Amount must be greater than 0 and at most 50000.00`

**Impact**
Customers cannot send the maximum amount that the product allows.
The error message itself says 50,000.00 is allowed, which contradicts the behaviour.

**Root cause (suspected)**
The upper limit check uses `>=` instead of `>`:
`amount.compareTo(MAX_AMOUNT) >= 0`