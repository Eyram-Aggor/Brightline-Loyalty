package com.brightline.loyalty;

import com.brightline.loyalty.member.Tier;
import com.brightline.loyalty.rules.MultiplierTierRule;
import com.brightline.loyalty.rules.TierRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MultiplierTierRuleTest {

    @ParameterizedTest(name = "{0} base points x {1} = {2}")
    @CsvSource({
            "10, 1,   10",
            "25, 2,   50",
            "20, 1.5, 30",
            "0,  2,   0"
    })
    void multipliesBasePoints(int basePoints, String multiplier, int expected) {
        TierRule rule = new MultiplierTierRule(Tier.GOLD, new BigDecimal(multiplier));

        assertEquals(expected, rule.apply(basePoints));
    }

    @ParameterizedTest(name = "{0} base points x 1.5 truncates to {1}")
    @CsvSource({
            "1, 1",
            "3, 4",
            "5, 7"
    })
    void truncatesFractionalPoints(int basePoints, int expected) {
        TierRule rule = new MultiplierTierRule(Tier.SILVER, new BigDecimal("1.5"));

        assertEquals(expected, rule.apply(basePoints));
    }

    @Test
    void reportsItsTier() {
        assertEquals(Tier.SILVER, new MultiplierTierRule(Tier.SILVER, BigDecimal.ONE).tier());
    }

    @Test
    void rejectsNegativeBasePoints() {
        TierRule rule = new MultiplierTierRule(Tier.GOLD, BigDecimal.TWO);

        assertThrows(IllegalArgumentException.class, () -> rule.apply(-1));
    }

    @Test
    void rejectsResultLargerThanAnInt() {
        TierRule rule = new MultiplierTierRule(Tier.GOLD, BigDecimal.TWO);

        assertThrows(ArithmeticException.class, () -> rule.apply(Integer.MAX_VALUE));
    }

    @Test
    void rejectsNullTier() {
        assertThrows(NullPointerException.class, () -> new MultiplierTierRule(null, BigDecimal.ONE));
    }

    @Test
    void rejectsNullMultiplier() {
        assertThrows(NullPointerException.class, () -> new MultiplierTierRule(Tier.GOLD, null));
    }

    @Test
    void rejectsZeroMultiplier() {
        assertThrows(IllegalArgumentException.class,
                () -> new MultiplierTierRule(Tier.GOLD, BigDecimal.ZERO));
    }

    @Test
    void rejectsNegativeMultiplier() {
        assertThrows(IllegalArgumentException.class,
                () -> new MultiplierTierRule(Tier.GOLD, new BigDecimal("-1")));
    }
}