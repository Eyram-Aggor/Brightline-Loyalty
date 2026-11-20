package com.brightline.loyalty.points;

import com.brightline.loyalty.exceptions.InvalidPurchaseException;
import com.brightline.loyalty.exceptions.MemberLookupException;
import com.brightline.loyalty.exceptions.MemberNotFoundException;
import com.brightline.loyalty.member.Member;
import com.brightline.loyalty.member.MemberRepository;
import com.brightline.loyalty.purchase.Purchase;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * Entry point for working out the points earned on a purchase:
 * validates the purchase, finds the member and delegates the calculation.
 */
public final class PointsService {

    private final MemberRepository memberRepository;
    private final PointsCalculator calculator;

    public PointsService(MemberRepository memberRepository, PointsCalculator calculator) {
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
        this.calculator = Objects.requireNonNull(calculator, "calculator must not be null");
    }

    /**
     * @return the points the member earns on this purchase
     * @throws InvalidPurchaseException if the member id or amount is malformed
     * @throws MemberNotFoundException  if no member has this id
     * @throws MemberLookupException    if the member store fails
     */
    public int calculatePoints(String memberId, BigDecimal amount) {
        Purchase purchase = new Purchase(memberId, amount);
        Member member = findMember(purchase.memberId());
        return calculator.calculate(member, purchase);
    }

    private Member findMember(String memberId) {
        Optional<Member> member;
        try {
            member = memberRepository.findById(memberId);
        } catch (RuntimeException e) {
            // Translate a storage failure, keeping its cause, so it is never mistaken for "not found"
            throw new MemberLookupException(memberId, e);
        }
        return member.orElseThrow(() -> new MemberNotFoundException(memberId));
    }
}