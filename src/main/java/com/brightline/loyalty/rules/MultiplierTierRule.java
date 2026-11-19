package com.brightline.loyalty.rules;

import com.brightline.loyalty.member.Tier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** A tier rule that multiplies base points by a fixed factor. */
public final class MultiplierTierRule implements TierRule {

    /** Fractional points are truncated, as in the original scheme. */
    private static final RoundingMode ROUNDING = RoundingMode.DOWN;

    private final Tier tier;
    private final BigDecimal multiplier;

    public MultiplierTierRule(Tier tier, BigDecimal multiplier) {
        this.tier = Objects.requireNonNull(tier, "tier must not be null");
        Objects.requireNonNull(multiplier, "multiplier must not be null");
        if (multiplier.signum() <= 0) {
            throw new IllegalArgumentException("multiplier must be greater than zero, was " + multiplier);
        }
        this.multiplier = multiplier;
    }

    @Override
    public Tier tier() {
        return tier;
    }

    @Override
    public int apply(int basePoints) {
        if (basePoints < 0) {
            throw new IllegalArgumentException("basePoints must not be negative, was " + basePoints);
        }
        return BigDecimal.valueOf(basePoints)
                .multiply(multiplier)
                .setScale(0, ROUNDING)
                .intValueExact();
    }
}