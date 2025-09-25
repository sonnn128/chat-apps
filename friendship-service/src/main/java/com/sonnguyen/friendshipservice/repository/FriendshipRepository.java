package com.sonnguyen.friendshipservice.repository;

import com.sonnguyen.friendshipservice.model.Friendship;
import com.sonnguyen.friendshipservice.model.FriendshipKey;
import com.sonnguyen.friendshipservice.model.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipKey> {

    List<Friendship> findByFriendshipKey_RequesterIdAndStatusOrFriendshipKey_FriendIdAndStatus(
            UUID requesterId, FriendshipStatus status1,
            UUID friendId, FriendshipStatus status2);

    @Query("SELECT f FROM Friendship f WHERE f.friendshipKey.friendId = :friendId AND f.status = :status")
    List<Friendship> findByFriendIdAndStatus(UUID friendId, FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE (f.friendshipKey.requesterId = :userId OR f.friendshipKey.friendId = :userId) AND f.status = :status")
    List<Friendship> findFriendshipsByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") FriendshipStatus status);
}