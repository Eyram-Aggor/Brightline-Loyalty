package com.brightline.loyalty.rules;

import com.brightline.loyalty.purchase.Purchase;

import java.math.BigDecimal;
import java.util.Objects;

/** Awards a fixed bonus when a purchase amount is strictly above a threshold. */
public final class BigSpendBonusRule implements BonusRule {

    private final BigDecimal threshold;
    private final int bonusPoints;

    public BigSpendBonusRule(BigDecimal threshold, int bonusPoints) {
        Objects.requireNonNull(threshold, "threshold must not be null");
        if (threshold.signum() < 0) {
            throw new IllegalArgumentException("threshold must not be negative, was " + threshold);
        }
        if (bonusPoints <= 0) {
            throw new IllegalArgumentException("bonusPoints must be greater than zero, was " + bonusPoints);
        }
        this.threshold = threshold;
        this.bonusPoints = bonusPoints;
    }

    @Override
    public int bonusFor(Purchase purchase) {
        Objects.requireNonNull(purchase, "purchase must not be null");
        return purchase.amount().compareTo(threshold) > 0 ? bonusPoints : 0;
    }
}