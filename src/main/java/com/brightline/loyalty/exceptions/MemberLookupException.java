package com.brightline.loyalty.exceptions;

import com.brightline.loyalty.exceptions.LoyaltyException;

/**
 * Thrown when the member store fails while looking up a member.
 * Distinct from {@link MemberNotFoundException}: the member may exist, but could not be read.
 */
public class MemberLookupException extends LoyaltyException {

    private final String memberId;

    public MemberLookupException(String memberId, Throwable cause) {
        super("Failed to look up member with id " + memberId, cause);
        this.memberId = memberId;
    }

    public String getMemberId() {
        return memberId;
    }
}