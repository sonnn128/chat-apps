package com.nnson128.relationshipservice.repository;

import com.nnson128.relationshipservice.model.poll.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PollOptionRepository extends JpaRepository<PollOption, UUID> {
}
