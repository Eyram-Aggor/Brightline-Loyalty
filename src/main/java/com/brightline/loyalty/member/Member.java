package com.brightline.loyalty.member;

import java.util.Objects;

/**
 * A loyalty scheme member.
 * A Member can only exist in a valid state: construction rejects missing data.
 */
public record Member(String id, String name, Tier tier) {

    public Member {
        requireNonBlank(id, "id");
        requireNonBlank(name, "name");
        Objects.requireNonNull(tier, "tier must not be null");
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
    }
}