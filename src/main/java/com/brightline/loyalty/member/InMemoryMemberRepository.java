package com.brightline.loyalty.member;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** A MemberRepository backed by a fixed set of members held in memory. */
public final class InMemoryMemberRepository implements MemberRepository {

    private final Map<String, Member> membersById;

    public InMemoryMemberRepository(Collection<Member> members) {
        Objects.requireNonNull(members, "members must not be null");
        Map<String, Member> byId = new HashMap<>();
        for (Member member : members) {
            Objects.requireNonNull(member, "members must not contain null");
            if (byId.putIfAbsent(member.id(), member) != null) {
                throw new IllegalArgumentException("Duplicate member id " + member.id());
            }
        }
        this.membersById = Map.copyOf(byId);
    }

    @Override
    public Optional<Member> findById(String id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(membersById.get(id));
    }
}