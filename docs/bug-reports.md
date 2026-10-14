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


## BUG-2: USD fee is not capped at the maximum of 50.00

| Field | Value                                          |
| --- |------------------------------------------------|
| Component | payment-api / FeeCalculator                    |
| Severity | Major                                          |
| Priority | High                                           |
| Found by | Unit test `usdLargeAmountIsCappedAtMaximumFee` |
| Status | Open                                           |

**Steps to reproduce**
1. Call `calculateFee(10000.00, "USD", false)`.

**Expected result**
The fee is 50.00, because the rule says USD fees are "never more than 50.00".
(1% of 10,000.00 = 100.00, capped at the USD maximum of 50.00.)

**Actual result**
The fee is 100.00. The maximum is not applied.
Test output: '<50.00> but was <100.00>'

**Impact**
Every USD and GPB payment above 5,000.00 is overcharged.
For example, a customer sending 10,000.00 USD pays 100.00 instead of 50.00
This can cause customer complaints and regulatory risk.

**Root cause (suspected)**
This information was discovered whilst carrying out a test involving a payment of $1,0000.00;