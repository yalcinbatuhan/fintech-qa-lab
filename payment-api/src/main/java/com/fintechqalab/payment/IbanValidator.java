package com.fintechqalab.payment;

import java.util.regex.Pattern;

/**
 * Basic IBAN format check: 2 letters (country), 2 digits (check digits),
 * then 11 to 30 letters or digits. Total length 15 to 34. Spaces are ignored.
 */
public class IbanValidator {

    private static final Pattern IBAN = Pattern.compile("^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$");

    public boolean isValid(String iban) {
        if (iban == null) {
            return false;
        }
        return IBAN.matcher(iban.replace(" ", "")).matches();
    }
}
