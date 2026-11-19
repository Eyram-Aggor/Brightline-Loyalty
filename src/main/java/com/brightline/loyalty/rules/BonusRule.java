package com.brightline.loyalty.rules;

import com.brightline.loyalty.purchase.Purchase;

public interface BonusRule {

    int bonusFor(Purchase purchase);
}