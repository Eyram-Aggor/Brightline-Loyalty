package com.brightline.loyalty;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurchaseTest {

    @Test
    void keepsMemberIdAndAmountOfAValidPurchase() {
        Purchase purchase = new Purchase("m1", new BigDecimal("100.50"));

        assertEquals("m1", purchase.memberId());
        assertEquals(new BigDecimal("100.50"), purchase.amount());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void rejectsNullOrBlankMemberId(String memberId) {
        InvalidPurchaseException e = assertThrows(InvalidPurchaseException.class,
                () -> new Purchase(memberId, new BigDecimal("100")));

        assertTrue(e.getMessage().contains("Member id"));
    }

    @Test
    void rejectsNullAmount() {
        InvalidPurchaseException e = assertThrows(InvalidPurchaseException.class,
                () -> new Purchase("m1", null));

        assertTrue(e.getMessage().contains("must not be null"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0.01", "-150"})
    void rejectsZeroOrNegativeAmount(String amount) {
        InvalidPurchaseException e = assertThrows(InvalidPurchaseException.class,
                () -> new Purchase("m1", new BigDecimal(amount)));

        assertTrue(e.getMessage().contains("greater than zero"));
    }

    @Test
    void acceptsSmallestPositiveAmount() {
        assertEquals(new BigDecimal("0.01"), new Purchase("m1", new BigDecimal("0.01")).amount());
    }

    @Test
    void rejectsMoreThanTwoDecimalPlaces() {
        InvalidPurchaseException e = assertThrows(InvalidPurchaseException.class,
                () -> new Purchase("m1", new BigDecimal("10.001")));

        assertTrue(e.getMessage().contains("decimal places"));
    }

    @Test
    void acceptsTrailingZerosBeyondTwoDecimalPlaces() {
        // 10.000 is numerically 10.00, so it is not a sub-pesewa amount
        assertEquals(new BigDecimal("10.000"), new Purchase("m1", new BigDecimal("10.000")).amount());
    }

    @Test
    void isAnInstanceOfTheLoyaltyExceptionHierarchy() {
        assertThrows(LoyaltyException.class, () -> new Purchase("m1", BigDecimal.ZERO));
    }
}