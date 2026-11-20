package com.brightline.loyalty;

import com.brightline.loyalty.exceptions.LoyaltyException;
import com.brightline.loyalty.member.InMemoryMemberRepository;
import com.brightline.loyalty.member.Member;
import com.brightline.loyalty.member.MemberRepository;
import com.brightline.loyalty.member.Tier;
import com.brightline.loyalty.points.PointsCalculator;
import com.brightline.loyalty.points.PointsService;
import com.brightline.loyalty.rules.BigSpendBonusRule;
import com.brightline.loyalty.rules.BonusRule;
import com.brightline.loyalty.rules.MultiplierTierRule;
import com.brightline.loyalty.rules.TierRules;

import java.math.BigDecimal;
import java.util.List;

/**
 * Composition root: the one place that chooses concrete implementations and
 * wires them together. A runner to see the service work end to end.
 */
public final class Demo {

    private Demo() {
    }

    public static void main(String[] args) {
        PointsService service = createService();

        printPoints(service, "m1", "100");      // Standard
        printPoints(service, "m2", "250");      // Gold
        printPoints(service, "m3", "600");      // Silver, with big-spend bonus
        printPoints(service, "unknown", "100"); // member not found
        printPoints(service, "m1", "-50");      // malformed purchase
    }

    static PointsService createService() {
        MemberRepository members = new InMemoryMemberRepository(List.of(
                new Member("m1", "Kofi Mensah", Tier.STANDARD),
                new Member("m2", "Ama Owusu", Tier.GOLD),
                new Member("m3", "Yaa Boateng", Tier.SILVER)));

        // A new tier is added here: one more rule, no change to existing code
        TierRules tierRules = new TierRules(List.of(
                new MultiplierTierRule(Tier.STANDARD, BigDecimal.ONE),
                new MultiplierTierRule(Tier.SILVER, new BigDecimal("1.5")),
                new MultiplierTierRule(Tier.GOLD, BigDecimal.TWO)));

        List<BonusRule> bonusRules = List.of(
                new BigSpendBonusRule(new BigDecimal("500"), 50));

        return new PointsService(members, new PointsCalculator(tierRules, bonusRules));
    }

    private static void printPoints(PointsService service, String memberId, String amount) {
        String request = "Member " + memberId + ", GHS " + amount + " -> ";
        try {
            int points = service.calculatePoints(memberId, new BigDecimal(amount));
            System.out.println(request + points + " points");
        } catch (LoyaltyException e) {
            System.out.println(request + "rejected: " + e.getMessage());
        }
    }
}