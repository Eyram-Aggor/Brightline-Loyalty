package com.brightline.loyalty.rules;

import com.brightline.loyalty.exceptions.RuleConfigurationException;
import com.brightline.loyalty.member.Tier;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Looks up the points rule for a tier.
 *
 * Construction fails unless every {@link Tier} has exactly one rule, so a tier
 * added without a rule is caught at startup, not on a customer's purchase.
 */
public final class TierRules {

    private final Map<Tier, TierRule> rulesByTier = new EnumMap<>(Tier.class);

    public TierRules(Collection<? extends TierRule> rules) {
        Objects.requireNonNull(rules, "rules must not be null");
        for (TierRule rule : rules) {
            Objects.requireNonNull(rule, "rules must not contain null");
            Tier tier = Objects.requireNonNull(rule.tier(), "rule tier must not be null");
            if (rulesByTier.putIfAbsent(tier, rule) != null) {
                throw new RuleConfigurationException("More than one rule configured for tier " + tier);
            }
        }
        Set<Tier> missing = EnumSet.allOf(Tier.class);
        missing.removeAll(rulesByTier.keySet());
        if (!missing.isEmpty()) {
            throw new RuleConfigurationException("No rule configured for tier(s) " + missing);
        }
    }

    /** Returns the rule for the given tier; every tier has one by construction. */
    public TierRule ruleFor(Tier tier) {
        Objects.requireNonNull(tier, "tier must not be null");
        return rulesByTier.get(tier);
    }
}