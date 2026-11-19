package com.brightline.loyalty.rules;


import com.brightline.loyalty.member.Tier;

public interface TierRule {
    Tier tier();

    int apply(int basePoints);
}