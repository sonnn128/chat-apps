package com.nnson128.relationshipservice.repository;

import com.nnson128.relationshipservice.model.poll.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PollRepository extends JpaRepository<Poll, UUID> {
    List<Poll> findByChannelIdOrderByCreatedAtDesc(UUID channelId);
}
