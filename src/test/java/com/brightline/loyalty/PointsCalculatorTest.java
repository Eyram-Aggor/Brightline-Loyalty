package com.brightline.loyalty;

import com.brightline.loyalty.member.Member;
import com.brightline.loyalty.member.Tier;
import com.brightline.loyalty.points.PointsCalculator;
import com.brightline.loyalty.purchase.Purchase;
import com.brightline.loyalty.rules.BigSpendBonusRule;
import com.brightline.loyalty.rules.BonusRule;
import com.brightline.loyalty.rules.MultiplierTierRule;
import com.brightline.loyalty.rules.TierRule;
import com.brightline.loyalty.rules.TierRules;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PointsCalculatorTest {

    private static final TierRule STANDARD = new MultiplierTierRule(Tier.STANDARD, BigDecimal.ONE);
    private static final TierRule SILVER = new MultiplierTierRule(Tier.SILVER, new BigDecimal("1.5"));
    private static final TierRule GOLD = new MultiplierTierRule(Tier.GOLD, BigDecimal.TWO);
    private static final BonusRule BIG_SPEND = new BigSpendBonusRule(new BigDecimal("500"), 50);

    private final TierRules schemeTierRules = new TierRules(List.of(STANDARD, SILVER, GOLD));
    private final PointsCalculator calculator = new PointsCalculator(schemeTierRules, List.of(BIG_SPEND));

    @ParameterizedTest(name = "{0} member, GHS {1} -> {2} points")
    @CsvSource({
            // tier rates
            "STANDARD, 100,    10",
            "SILVER,   200,    30",
            "GOLD,     250,    50",
            // base points: one per full GHS 10, partial units truncated
            "STANDARD, 0.01,   0",
            "STANDARD, 9.99,   0",
            "STANDARD, 10,     1",
            "STANDARD, 99.99,  9",
            // fractional tier points truncated: 3 x 1.5 = 4.5 -> 4
            "SILVER,   30,     4",
            // big-spend bonus applies strictly above GHS 500
            "STANDARD, 500,    50",
            "STANDARD, 500.01, 100",
            // bonus is added after the multiplier, so it is not multiplied
            "GOLD,     600,    170",
            "SILVER,   600,    140"
    })
    void calculatesPointsForTheSchemeRules(Tier tier, String amount, int expectedPoints) {
        Member member = new Member("m1", "Kofi Mensah", tier);
        Purchase purchase = new Purchase("m1", new BigDecimal(amount));

        assertEquals(expectedPoints, calculator.calculate(member, purchase));
    }

    @Test
    void addsEveryBonusRule() {
        PointsCalculator twoBonuses = new PointsCalculator(schemeTierRules, List.of(p -> 5, p -> 7));

        assertEquals(10 + 5 + 7, twoBonuses.calculate(member(Tier.STANDARD), purchase("100")));
    }

    @Test
    void awardsOnlyTierPointsWhenThereAreNoBonusRules() {
        PointsCalculator noBonuses = new PointsCalculator(schemeTierRules, List.of());

        assertEquals(120, noBonuses.calculate(member(Tier.GOLD), purchase("600")));
    }

    @Test
    void usesWhicheverTierRuleIsConfigured() {
        // A rule of a different kind plugs in without any change to the calculator
        TierRule flatGold = new TierRule() {
            @Override
            public Tier tier() {
                return Tier.GOLD;
            }

            @Override
            public int apply(int basePoints) {
                return basePoints + 100;
            }
        };
        PointsCalculator custom = new PointsCalculator(
                new TierRules(List.of(STANDARD, SILVER, flatGold)), List.of());

        assertEquals(110, custom.calculate(member(Tier.GOLD), purchase("100")));
    }

    @Test
    void isNotAffectedByLaterChangesToTheBonusList() {
        List<BonusRule> bonuses = new ArrayList<>(List.of(BIG_SPEND));
        PointsCalculator calc = new PointsCalculator(schemeTierRules, bonuses);

        bonuses.clear();

        assertEquals(110, calc.calculate(member(Tier.STANDARD), purchase("600")));
    }

    @Test
    void rejectsATotalLargerThanAnInt() {
        PointsCalculator overflowing = new PointsCalculator(schemeTierRules, List.of(p -> Integer.MAX_VALUE));

        assertThrows(ArithmeticException.class,
                () -> overflowing.calculate(member(Tier.STANDARD), purchase("100")));
    }

    @Test
    void rejectsNullMember() {
        assertThrows(NullPointerException.class, () -> calculator.calculate(null, purchase("100")));
    }

    @Test
    void rejectsNullPurchase() {
        assertThrows(NullPointerException.class, () -> calculator.calculate(member(Tier.GOLD), null));
    }

    @Test
    void rejectsNullTierRules() {
        assertThrows(NullPointerException.class, () -> new PointsCalculator(null, List.of()));
    }

    @Test
    void rejectsNullBonusRules() {
        assertThrows(NullPointerException.class, () -> new PointsCalculator(schemeTierRules, null));
    }

    private static Member member(Tier tier) {
        return new Member("m1", "Kofi Mensah", tier);
    }

    private static Purchase purchase(String amount) {
        return new Purchase("m1", new BigDecimal(amount));
    }
}