package com.brightline.loyalty;

import java.math.BigDecimal;


public record Purchase(String memberId, BigDecimal amount) {

    private static final int MAX_DECIMAL_PLACES = 2;

    public Purchase {
        if (memberId == null || memberId.isBlank()) {
            throw new InvalidPurchaseException("Member id must not be null or blank");
        }
        if (amount == null) {
            throw new InvalidPurchaseException("Amount must not be null");
        }
        if (amount.signum() <= 0) {
            throw new InvalidPurchaseException("Amount must be greater than zero, was " + amount);
        }
        if (amount.stripTrailingZeros().scale() > MAX_DECIMAL_PLACES) {
            throw new InvalidPurchaseException(
                    "Amount must have at most " + MAX_DECIMAL_PLACES + " decimal places, was " + amount);
        }
    }
}