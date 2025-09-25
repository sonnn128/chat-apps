package com.sonnguyen.channelservice.repository;

import com.sonnguyen.channelservice.model.membership.Membership;
import com.sonnguyen.channelservice.model.membership.MembershipKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, MembershipKey> {
    List<Membership> findByMembershipKeyUserId(UUID userId);
    List<Membership> findByMembershipKeyChannelId(UUID channelId);
    boolean existsByMembershipKeyChannelIdAndMembershipKeyUserId(UUID channelId, UUID userId);
}
