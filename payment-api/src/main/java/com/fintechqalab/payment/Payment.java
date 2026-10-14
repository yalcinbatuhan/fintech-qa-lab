package com.fintechqalab.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** A payment as stored and returned by the API. */
public record Payment(
        long id,
        BigDecimal amount,
        String currency,
        String iban,
        boolean instant,
        BigDecimal fee,
        String status,
        Instant createdAt) {

    Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("amount", amount);
        map.put("currency", currency);
        map.put("iban", iban);
        map.put("instant", instant);
        map.put("fee", fee);
        map.put("status", status);
        map.put("createdAt", createdAt.toString());
        return map;
    }
}
