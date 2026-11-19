package com.brightline.loyalty;

import com.brightline.loyalty.purchase.Purchase;
import com.brightline.loyalty.rules.BigSpendBonusRule;
import com.brightline.loyalty.rules.BonusRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BigSpendBonusRuleTest {

    private final BonusRule rule = new BigSpendBonusRule(new BigDecimal("500"), 50);

    @ParameterizedTest(name = "GHS {0} earns a bonus of {1}")
    @CsvSource({
            "100,    0",
            "499.99, 0",
            "500,    0",
            "500.00, 0",
            "500.01, 50",
            "1000,   50"
    })
    void awardsBonusOnlyStrictlyAboveThreshold(String amount, int expectedBonus) {
        Purchase purchase = new Purchase("m1", new BigDecimal(amount));

        assertEquals(expectedBonus, rule.bonusFor(purchase));
    }

    @Test
    void rejectsNullPurchase() {
        assertThrows(NullPointerException.class, () -> rule.bonusFor(null));
    }

    @Test
    void rejectsNullThreshold() {
        assertThrows(NullPointerException.class, () -> new BigSpendBonusRule(null, 50));
    }

    @Test
    void rejectsNegativeThreshold() {
        assertThrows(IllegalArgumentException.class,
                () -> new BigSpendBonusRule(new BigDecimal("-1"), 50));
    }

    @Test
    void acceptsZeroThreshold() {
        BonusRule everyPurchase = new BigSpendBonusRule(BigDecimal.ZERO, 5);

        assertEquals(5, everyPurchase.bonusFor(new Purchase("m1", new BigDecimal("0.01"))));
    }

    @Test
    void rejectsZeroBonusPoints() {
        assertThrows(IllegalArgumentException.class,
                () -> new BigSpendBonusRule(new BigDecimal("500"), 0));
    }

    @Test
    void rejectsNegativeBonusPoints() {
        assertThrows(IllegalArgumentException.class,
                () -> new BigSpendBonusRule(new BigDecimal("500"), -50));
    }
}