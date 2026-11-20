package com.brightline.loyalty.points;

import com.brightline.loyalty.member.Member;
import com.brightline.loyalty.purchase.Purchase;
import com.brightline.loyalty.rules.BonusRule;
import com.brightline.loyalty.rules.TierRules;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Calculates the points a member earns on a purchase.
 * Pure calculation: no lookup, no I/O, no error hiding.
 */
public final class PointsCalculator {

    /** One base point is earned for every GHS 10 spent; partial units earn nothing. */
    private static final BigDecimal GHS_PER_BASE_POINT = BigDecimal.TEN;

    private final TierRules tierRules;
    private final List<BonusRule> bonusRules;

    public PointsCalculator(TierRules tierRules, Collection<? extends BonusRule> bonusRules) {
        this.tierRules = Objects.requireNonNull(tierRules, "tierRules must not be null");
        this.bonusRules = List.copyOf(Objects.requireNonNull(bonusRules, "bonusRules must not be null"));
    }

    /**
     * Base points, then the member's tier rule, then every bonus rule.
     * Bonuses are added after the tier rule, so they are not multiplied.
     */
    public int calculate(Member member, Purchase purchase) {
        Objects.requireNonNull(member, "member must not be null");
        Objects.requireNonNull(purchase, "purchase must not be null");

        int basePoints = purchase.amount()
                .divide(GHS_PER_BASE_POINT, 0, RoundingMode.DOWN)
                .intValueExact();
        int points = tierRules.ruleFor(member.tier()).apply(basePoints);
        for (BonusRule bonusRule : bonusRules) {
            points = Math.addExact(points, bonusRule.bonusFor(purchase));
        }
        return points;
    }
}