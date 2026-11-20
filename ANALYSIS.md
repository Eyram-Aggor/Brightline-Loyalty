BRIGHTLINE LOYALTY - POINTS SERVICE
Analysis of the inherited code: code smells, anti-patterns and SOLID violations


1. FAILURE CHAIN (unknown member)
-------------------------------------------------------------------------------
findById("unknown") returns null -> member.getTier() throws NPE
-> catch (Exception e) returns 0 -> Demo prints 0 as a normal award.
"Member not found" is indistinguishable from "earned 0 points".


2. PointsCalculator
-------------------------------------------------------------------------------
2.1  SRP violation (lines 7-34): lookup, base points, tier multiplier, bonus,
console output and error handling in one method.
2.2  DIP violation (line 7): instantiates InMemoryMemberRepository itself;
nothing can be injected, so it cannot be tested in isolation.
2.3  Open/Closed Principle violation / Switch Statements smell (lines 17-23): a new tier         means editing this method.
2.4  Swallowed catch-all exception (lines 32-34): catches everything,
including bugs, logs nothing, returns 0. Non-descriptive exception handling makes debugging more difficult. A failure can't be told apart from a valid award.
2.5  Unknown or null tier (lines 17-23): no else branch, so any unrecognised tier value (“gold”, “PLATINUM”, a typo) silently gets the Standard rate.
2.6  Magic numbers/strings (lines 14-27): 10, 1.5, 2, 500, 50 and the tier
names are unnamed literals.
2.7  No input validation (lines 9, 14): -150 -> -15 points, NaN -> 0,
Infinity -> Integer.MAX_VALUE; null/blank memberId not checked.
2.8  double for money (line 9): inexact for currency, BigDecimal is a better type choice.
2.9  Implicit rounding (lines 14, 20): (int) truncates - GHS 99.99 -> 9,
Silver 3 base -> 4. Rounding is a cast side effect, not a stated rule.
2.10 Hardcoded bonus (lines 26-27): also an OCP issue. "> 500" excludes
GHS 500; bonus is added after the multiplier. Both undocumented.
2.11 Side effect in a calculation (line 30): System.out.println breaks SRP
and Command-Query Separation, is untestable, and logs a member's name.
2.12 Feature Envy (lines 17-21): tier behaviour lives in the calculator,
not in the tier; Member is an anaemic data holder.
2.13 Dead code (line 18): points * 1.
2.14 Comments narrating defects (lines 5-6, 33) instead of fixing them.


3. MemberRepository
-------------------------------------------------------------------------------
3.1  Undefined missing-member contract (line 4): should return
Optional<Member> or throw a documented MemberNotFoundException.


4. InMemoryMemberRepository
-------------------------------------------------------------------------------
4.1  Null return for a missing member (line 18): root of the failure chain.
4.2  Hardcoded seed data in the constructor (lines 10-14): mixes storage
with data setup; cannot be created empty or with other data.
4.3  Duplicated ids (lines 11-13): map key and Member id can drift apart.
4.4  findById(null) silently returns null (line 18).


5. Member
-------------------------------------------------------------------------------
5.1  Primitive Obsession (line 6): tier is a String documented by a comment;
use an enum or Tier type so invalid tiers cannot exist.
5.2  No constructor validation (lines 8-12): null/blank values accepted.


6. Demo
-------------------------------------------------------------------------------
6.1  No composition root (line 6): cannot inject dependencies (from 2.2).
6.2  Prints a failure as a valid "0" result (line 10, from 2.4 and 4.1).


7. PointsCalculatorTest
-------------------------------------------------------------------------------
7.1  awardsPointsToAMember (lines 11-15): weak. assertTrue(points > 0)
passes for any positive value; the correct result, 10, is never
checked, so a wrong rate or multiplier goes undetected. Depends on the
hardcoded seed data in InMemoryMemberRepository (from 2.2, 4.2).
7.2  calculatesGoldPoints (lines 18-23): tests nothing. Mocks the class
under test, so no production code runs; it asserts that Mockito returns
the stubbed 50. Passes even if calculatePoints is wrong.
7.3  awardsPointsForAPurchase (lines 26-30): tests nothing.
assertNotNull on a primitive int autoboxes to a non-null Integer, so it
always passes; the catch-all (2.4) means no exception can fail it
either. The correct result, 30, is never checked.
7.4  Missing coverage: unknown member, exact Silver/Gold results, bonus
above GHS 500 and the 500 boundary, rounding, negative/zero/NaN/
Infinity amounts, unrecognised tier.
7.5  Why the suite is green: no failure path is tested and two of three
assertions cannot fail, so the swallowed exception (2.4) goes unseen.


8. SOLID SUMMARY
-------------------------------------------------------------------------------
S  Violated   PointsCalculator (2.1, 2.11); InMemoryMemberRepository (4.2).
O  Violated   Tier chain and bonus require edits for new rules (2.3, 2.10).
L  No issue   No substantial use of subclasses to invoke an issue.
I  No issue   Single cohesive interface method.
D  Violated   Concrete repository and inline tier rules (2.2).


9. MOST SERIOUS PROBLEM
-------------------------------------------------------------------------------
The swallowed catch-all exception (2.4) combined with the null return (4.1).
It turns failures into plausible wrong results: an unknown member, a null
tier and a programming bug all return 0, a valid points value. Customers
silently lose points, nothing is logged, and it masks 2.5 and 2.7. The
test suite never exercises it (7.5).

Runner-up: the OCP violation (2.3), since the new tier is the announced change.
