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
| Found by | Parameterized test `usdFeeTable`, row `10000.00 -> 50.00` |
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

## BUG-3: Fee is rounded down instead of HALF_UP”


| Field | Value                                          |
| --- |------------------------------------------------|
| Component | payment-api / FeeCalculator                    |
| Severity | Major                                          |
| Priority | High                                           |
| Found by | Unit test `roundingAfterMinimumFee` |
| Status | Open                                           |

**Steps to reproduce**
1. Call `calculateFee(151.00, "EUR", false)`.

**Expected result**
The fee should have been rounded to 0.76 from 0.755 instead of 0.75. Given that rules specifies that 
fees falling exactly a whole number should be rounded to next whole number.

**Actual result**
The fee is 0.75. The rounding is not applied.
Test output: '<0.76> but was <0.75>'

**Impact**
Every EUR payment's fee is not able to rounded to next whole number. 
Bearing in mind that millions of transactions are carried out every day, this gap might be so much.
This can cause customer complaints and regulatory risk.

**Root cause (suspected)**
The final rounding uses `RoundingMode.DOWN` instead of `RoundingMode.HALF_UP`:
`return fee.setScale(2, RoundingMode.DOWN);`