package com.brightline.loyalty;

import com.brightline.loyalty.member.Member;
import com.brightline.loyalty.member.Tier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberTest {

    @Test
    void keepsIdNameAndTierOfAValidMember() {
        Member member = new Member("m1", "Kofi Mensah", Tier.STANDARD);

        assertEquals("m1", member.id());
        assertEquals("Kofi Mensah", member.name());
        assertEquals(Tier.STANDARD, member.tier());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsNullOrBlankId(String id) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Member(id, "Kofi Mensah", Tier.STANDARD));

        assertTrue(e.getMessage().startsWith("id"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsNullOrBlankName(String name) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Member("m1", name, Tier.STANDARD));

        assertTrue(e.getMessage().startsWith("name"));
    }

    @Test
    void rejectsNullTier() {
        assertThrows(NullPointerException.class, () -> new Member("m1", "Kofi Mensah", null));
    }
}