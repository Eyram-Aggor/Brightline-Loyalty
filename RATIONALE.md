BRIGHTLINE LOYALTY - POINTS SERVICE
Refactoring rationale


1. KEY DECISIONS
-------------------------------------------------------------------------------
1.1  One job per class (SRP): PointsCalculator only calculates; PointsService
validates, looks up the member and delegates; Demo wires everything.
1.2  Dependencies injected (DIP): the repository, tier rules and bonus rules
are passed in through constructors, so tests can supply their own.
1.3  Invalid states cannot be built: Purchase and Member validate in their
constructors; Tier is an enum, not a String.
1.4  BigDecimal for money; truncation is a named RoundingMode, not an
(int) cast.
1.5  Business results preserved: same rate, multipliers, truncation and
bonus (> GHS 500, added after the multiplier).
1.6  Code grouped into feature packages (member, purchase, rules, points);
exceptions kept together in one exception package.


2. EXCEPTION HANDLING
-------------------------------------------------------------------------------
- Catch-all that returned 0 removed. Failures are now reported, never hidden.
- Custom unchecked exceptions under LoyaltyException:
  InvalidPurchaseException, MemberNotFoundException, MemberLookupException,
  RuleConfigurationException.
- One try-catch in PointsService, around the repository call only: a storage
  failure becomes MemberLookupException with its cause kept, so it is never
  mistaken for "not found".
- Demo catches LoyaltyException at the application boundary and reports it.


3. HOW THE NEW TIER IS ADDED (OCP)
-------------------------------------------------------------------------------
1. Add the tier to the Tier enum.
2. Register a rule for it in Demo: a MultiplierTierRule, or a new TierRule
   class if the rule is not a multiplier.
   PointsCalculator, PointsService and existing rules are not edited. New
   bonuses work the same way through BonusRule.


4. KEPT, FIXED, REWRITTEN
-------------------------------------------------------------------------------
Code
- Kept:      MemberRepository interface (contract fixed, see below); the
  points rules themselves.
- Fixed:     MemberRepository returns Optional instead of null;
  InMemoryMemberRepository takes its members via the constructor;
  Member uses Tier and validates.
- Rewritten: PointsCalculator (if/else chain, hard-wired repository,
  println and catch-all removed); Demo (now the composition root).

Tests
- awardsPointsToAMember: intent kept, rewritten with exact values.
- calculatesGoldPoints: deleted. It mocked the class under test, so it only
  tested Mockito.
- awardsPointsForAPurchase: deleted. assertNotNull on an int cannot fail.
- New suite: 110 JUnit 5 + Mockito tests covering happy paths, boundaries
  (GHS 9.99/10, 500/500.01, rounding) and every failure case. Mockito mocks
  only the repository; each test asserts real results or exceptions.
- PIT mutation testing: 33 of 33 mutants killed (100%).


5. IMPROVEMENT BEYOND THE BRIEF
-------------------------------------------------------------------------------
Fail-fast rule completeness check in TierRules: the app refuses to start if
any tier has no rule or more than one.

Value: if the new tier is added without a rule, it fails at startup and in
tests, not on a customer's purchase. It prevents a repeat of the original
defect, where an unrecognised tier silently got the Standard rate.


6. DELIBERATE ADDITION
-------------------------------------------------------------------------------
Purchase rejects amounts with more than 2 decimal places (sub-pesewa
amounts). The original code did not check this.
