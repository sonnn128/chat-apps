package com.sonnguyen.channelservice.repository;

import com.sonnguyen.channelservice.model.ChannelParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChannelParticipantRepository extends JpaRepository<ChannelParticipant, Long> {

    List<ChannelParticipant> findByUserId(UUID userId);

    // Thêm phương thức này để kiểm tra quyền
    boolean existsByChannelIdAndUserId(UUID channelId, UUID userId);
}