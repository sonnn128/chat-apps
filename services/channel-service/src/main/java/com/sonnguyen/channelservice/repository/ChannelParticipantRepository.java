package com.sonnguyen.userservice.repository;

import com.sonnguyen.userservice.model.ChannelParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChannelParticipantRepository extends JpaRepository<ChannelParticipant, Long> {

    List<ChannelParticipant> findByUserId(UUID userId);
    List<ChannelParticipant> findByChannelId(UUID channelId);
    boolean existsByChannelIdAndUserId(UUID channelId, UUID userId);
}