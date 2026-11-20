package com.brightline.loyalty;

import com.brightline.loyalty.exceptions.InvalidPurchaseException;
import com.brightline.loyalty.exceptions.MemberLookupException;
import com.brightline.loyalty.exceptions.MemberNotFoundException;
import com.brightline.loyalty.member.Member;
import com.brightline.loyalty.member.MemberRepository;
import com.brightline.loyalty.member.Tier;
import com.brightline.loyalty.points.PointsCalculator;
import com.brightline.loyalty.points.PointsService;
import com.brightline.loyalty.rules.BigSpendBonusRule;
import com.brightline.loyalty.rules.MultiplierTierRule;
import com.brightline.loyalty.rules.TierRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointsServiceTest {

    @Mock
    private MemberRepository repository;

    private PointsCalculator calculator;
    private PointsService service;

    @BeforeEach
    void setUp() {
        // The real calculator: it is pure, so mocking it would only test the mock
        calculator = new PointsCalculator(
                new TierRules(List.of(
                        new MultiplierTierRule(Tier.STANDARD, BigDecimal.ONE),
                        new MultiplierTierRule(Tier.SILVER, new BigDecimal("1.5")),
                        new MultiplierTierRule(Tier.GOLD, BigDecimal.TWO))),
                List.of(new BigSpendBonusRule(new BigDecimal("500"), 50)));
        service = new PointsService(repository, calculator);
    }

    @ParameterizedTest(name = "{0} member, GHS {1} -> {2} points")
    @CsvSource({
            "STANDARD, 100, 10",
            "SILVER,   200, 30",
            "GOLD,     250, 50",
            "GOLD,     600, 170"
    })
    void returnsThePointsForTheMembersTier(Tier tier, String amount, int expectedPoints) {
        when(repository.findById("m1")).thenReturn(Optional.of(new Member("m1", "Kofi Mensah", tier)));

        assertEquals(expectedPoints, service.calculatePoints("m1", new BigDecimal(amount)));
    }

    @Test
    void looksUpTheMemberNamedInThePurchase() {
        when(repository.findById("m2")).thenReturn(Optional.of(new Member("m2", "Ama Owusu", Tier.GOLD)));

        int points = service.calculatePoints("m2", new BigDecimal("250"));

        assertEquals(50, points);
        verify(repository).findById("m2");
    }

    @Test
    void throwsMemberNotFoundForAnUnknownMember() {
        when(repository.findById("unknown")).thenReturn(Optional.empty());

        MemberNotFoundException e = assertThrows(MemberNotFoundException.class,
                () -> service.calculatePoints("unknown", new BigDecimal("100")));

        assertEquals("unknown", e.getMemberId());
    }

    @ParameterizedTest(name = "member \"{0}\", amount {1}")
    @CsvSource(value = {
            "m1,   -50",
            "m1,   0",
            "m1,   10.001",
            "m1,   NULL",
            "NULL, 100",
            "'  ', 100"
    }, nullValues = "NULL")
    void rejectsAMalformedPurchaseWithoutLookingUpTheMember(String memberId, BigDecimal amount) {
        assertThrows(InvalidPurchaseException.class, () -> service.calculatePoints(memberId, amount));

        verifyNoInteractions(repository);
    }

    @Test
    void wrapsARepositoryFailureAndKeepsItsCause() {
        IllegalStateException storageFailure = new IllegalStateException("connection lost");
        when(repository.findById("m1")).thenThrow(storageFailure);

        MemberLookupException e = assertThrows(MemberLookupException.class,
                () -> service.calculatePoints("m1", new BigDecimal("100")));

        assertSame(storageFailure, e.getCause());
        assertEquals("m1", e.getMemberId());
    }

    @Test
    void rejectsNullRepository() {
        assertThrows(NullPointerException.class, () -> new PointsService(null, calculator));
    }

    @Test
    void rejectsNullCalculator() {
        assertThrows(NullPointerException.class, () -> new PointsService(repository, null));
    }
}