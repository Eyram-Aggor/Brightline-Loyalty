package com.brightline.loyalty;

import com.brightline.loyalty.exceptions.LoyaltyException;
import com.brightline.loyalty.exceptions.MemberNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class MemberNotFoundExceptionTest {

    @Test
    void carriesTheMissingMemberIdInFieldAndMessage() {
        MemberNotFoundException e = new MemberNotFoundException("m99");

        assertEquals("m99", e.getMemberId());
        assertEquals("No member found with id m99", e.getMessage());
    }

    @Test
    void isPartOfTheLoyaltyExceptionHierarchy() {
        assertInstanceOf(LoyaltyException.class, new MemberNotFoundException("m99"));
    }
}