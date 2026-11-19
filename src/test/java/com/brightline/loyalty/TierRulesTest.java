package com.brightline.loyalty;

import com.brightline.loyalty.exceptions.RuleConfigurationException;
import com.brightline.loyalty.member.Tier;
import com.brightline.loyalty.rules.MultiplierTierRule;
import com.brightline.loyalty.rules.TierRule;
import com.brightline.loyalty.rules.TierRules;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TierRulesTest {

    private final TierRule standard = new MultiplierTierRule(Tier.STANDARD, BigDecimal.ONE);
    private final TierRule silver = new MultiplierTierRule(Tier.SILVER, new BigDecimal("1.5"));
    private final TierRule gold = new MultiplierTierRule(Tier.GOLD, BigDecimal.TWO);

    @Test
    void returnsTheRuleConfiguredForEachTier() {
        TierRules rules = new TierRules(List.of(standard, silver, gold));

        assertSame(standard, rules.ruleFor(Tier.STANDARD));
        assertSame(silver, rules.ruleFor(Tier.SILVER));
        assertSame(gold, rules.ruleFor(Tier.GOLD));
    }

    @Test
    void acceptsACustomRuleImplementation() {
        // A rule that is not a multiplier plugs in without changing TierRules
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

        TierRules rules = new TierRules(List.of(standard, silver, flatGold));

        assertEquals(110, rules.ruleFor(Tier.GOLD).apply(10));
    }

    @Test
    void failsFastWhenATierHasNoRule() {
        RuleConfigurationException e = assertThrows(RuleConfigurationException.class,
                () -> new TierRules(List.of(standard, silver)));

        assertTrue(e.getMessage().contains("GOLD"));
    }

    @Test
    void namesEveryTierThatHasNoRule() {
        RuleConfigurationException e = assertThrows(RuleConfigurationException.class,
                () -> new TierRules(List.of(standard)));

        assertTrue(e.getMessage().contains("SILVER"));
        assertTrue(e.getMessage().contains("GOLD"));
    }

    @Test
    void failsFastWhenNoRulesAreGiven() {
        assertThrows(RuleConfigurationException.class, () -> new TierRules(List.of()));
    }

    @Test
    void failsFastWhenATierHasTwoRules() {
        TierRule secondGold = new MultiplierTierRule(Tier.GOLD, new BigDecimal("3"));

        RuleConfigurationException e = assertThrows(RuleConfigurationException.class,
                () -> new TierRules(List.of(standard, silver, gold, secondGold)));

        assertTrue(e.getMessage().contains("GOLD"));
    }

    @Test
    void isNotAffectedByLaterChangesToTheInputList() {
        List<TierRule> input = new ArrayList<>(List.of(standard, silver, gold));
        TierRules rules = new TierRules(input);

        input.clear();

        assertSame(gold, rules.ruleFor(Tier.GOLD));
    }

    @Test
    void rejectsNullRuleList() {
        assertThrows(NullPointerException.class, () -> new TierRules(null));
    }

    @Test
    void rejectsNullRuleInList() {
        assertThrows(NullPointerException.class,
                () -> new TierRules(Arrays.asList(standard, null, gold)));
    }

    @Test
    void rejectsNullTierLookup() {
        TierRules rules = new TierRules(List.of(standard, silver, gold));

        assertThrows(NullPointerException.class, () -> rules.ruleFor(null));
    }
}