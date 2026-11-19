package com.brightline.loyalty.member;

import java.util.Optional;

/** Looks up loyalty scheme members. */
public interface MemberRepository {

    /**
     * @param id the member id, not null
     * @return the member, or {@link Optional#empty()} when no member has this id; never null
     */
    Optional<Member> findById(String id);
}