package com.brightline.loyalty.exceptions;

/** Thrown when no member exists with the requested id. */
public class MemberNotFoundException extends LoyaltyException {

    private final String memberId;

    public MemberNotFoundException(String memberId) {
        super("No member found with id " + memberId);
        this.memberId = memberId;
    }

    public String getMemberId() {
        return memberId;
    }
}