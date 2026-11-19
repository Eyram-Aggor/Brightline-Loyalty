package com.brightline.loyalty;

import com.brightline.loyalty.member.InMemoryMemberRepository;
import com.brightline.loyalty.member.Member;
import com.brightline.loyalty.member.MemberRepository;
import com.brightline.loyalty.member.Tier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryMemberRepositoryTest {

    private final Member kofi = new Member("m1", "Kofi Mensah", Tier.STANDARD);
    private final Member ama = new Member("m2", "Ama Owusu", Tier.GOLD);

    @Test
    void findsAnExistingMemberById() {
        MemberRepository repository = new InMemoryMemberRepository(List.of(kofi, ama));

        assertEquals(Optional.of(ama), repository.findById("m2"));
    }

    @Test
    void returnsEmptyForAnUnknownId() {
        MemberRepository repository = new InMemoryMemberRepository(List.of(kofi, ama));

        assertEquals(Optional.empty(), repository.findById("unknown"));
    }

    @Test
    void returnsEmptyWhenCreatedWithNoMembers() {
        MemberRepository repository = new InMemoryMemberRepository(List.of());

        assertTrue(repository.findById("m1").isEmpty());
    }

    @Test
    void rejectsNullId() {
        MemberRepository repository = new InMemoryMemberRepository(List.of(kofi));

        assertThrows(NullPointerException.class, () -> repository.findById(null));
    }

    @Test
    void rejectsDuplicateMemberIds() {
        Member kofiAgain = new Member("m1", "Someone Else", Tier.GOLD);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new InMemoryMemberRepository(List.of(kofi, kofiAgain)));

        assertTrue(e.getMessage().contains("m1"));
    }

    @Test
    void isNotAffectedByLaterChangesToTheInputList() {
        List<Member> input = new ArrayList<>(List.of(kofi));
        MemberRepository repository = new InMemoryMemberRepository(input);

        input.clear();

        assertEquals(Optional.of(kofi), repository.findById("m1"));
    }

    @Test
    void rejectsNullMemberList() {
        assertThrows(NullPointerException.class, () -> new InMemoryMemberRepository(null));
    }

    @Test
    void rejectsNullMemberInList() {
        assertThrows(NullPointerException.class,
                () -> new InMemoryMemberRepository(Arrays.asList(kofi, null)));
    }
}