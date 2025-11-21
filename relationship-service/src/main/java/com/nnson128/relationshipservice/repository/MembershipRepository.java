package com.nnson128.relationshipservice.repository;

import com.nnson128.relationshipservice.model.membership.Membership;
import com.nnson128.relationshipservice.model.membership.MembershipKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, MembershipKey> {
    List<Membership> findByMembershipKeyUserId(UUID userId);
    List<Membership> findByMembershipKeyChannelId(UUID channelId);
    boolean existsByMembershipKeyChannelIdAndMembershipKeyUserId(UUID channelId, UUID userId);

    @org.springframework.data.jpa.repository.Query("SELECT m1.membershipKey.channelId FROM Membership m1 " +
           "JOIN Membership m2 ON m1.membershipKey.channelId = m2.membershipKey.channelId " +
           "WHERE m1.membershipKey.userId = :user1 AND m2.membershipKey.userId = :user2 " +
           "AND (SELECT COUNT(m3) FROM Membership m3 WHERE m3.membershipKey.channelId = m1.membershipKey.channelId) = 2")
    List<UUID> findDirectChannelIds(@org.springframework.data.repository.query.Param("user1") UUID user1, @org.springframework.data.repository.query.Param("user2") UUID user2);
}
