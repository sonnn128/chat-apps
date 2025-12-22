package com.nnson128.relationshipservice.repository;

import com.nnson128.relationshipservice.model.poll.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, UUID> {
    List<PollVote> findByPollId(UUID pollId);
    
    Optional<PollVote> findByOptionIdAndUserId(UUID optionId, UUID userId);
    
    void deleteByOptionIdAndUserId(UUID optionId, UUID userId);
    
    // Count votes for an option
    long countByOptionId(UUID optionId);
}
