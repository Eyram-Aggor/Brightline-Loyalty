package com.brightline.loyalty.points;

import com.brightline.loyalty.exceptions.MemberNotFoundException;
import com.brightline.loyalty.member.InMemoryMemberRepository;
import com.brightline.loyalty.member.Member;
import com.brightline.loyalty.member.MemberRepository;
import com.brightline.loyalty.member.Tier;

import java.util.List;

public class PointsCalculator {

    // The calculator makes its own repository. Convenient, but it means callers
    // and tests cannot give it a different one.
    private final MemberRepository repository = new InMemoryMemberRepository(List.of(
            new Member("m1", "Kofi Mensah", Tier.STANDARD),
            new Member("m2", "Ama Owusu", Tier.GOLD),
            new Member("m3", "Yaa Boateng", Tier.SILVER)));

    public int calculatePoints(String memberId, double amount) {
        try {
            Member member = repository.findById(memberId)
                    .orElseThrow(() -> new MemberNotFoundException(memberId));

            // base points: one point for every GHS 10 spent
            int points = (int) (amount / 10);

            // apply the member's tier multiplier
            if (member.tier() == Tier.STANDARD) {
                points = points * 1;
            } else if (member.tier() == Tier.SILVER) {
                points = (int) (points * 1.5);
            } else if (member.tier() == Tier.GOLD) {
                points = points * 2;
            }

            // loyalty bonus for big spenders
            if (amount > 500) {
                points = points + 50;
            }

            System.out.println("Awarded " + points + " points to " + member.name());
            return points;
        } catch (Exception e) {
            // something went wrong (for example the member was not found) - award nothing
            return 0;
        }
    }
}