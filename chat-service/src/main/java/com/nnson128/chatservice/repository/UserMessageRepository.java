package com.nnson128.chatservice.repository;

import com.nnson128.chatservice.model.UserMessage;
import com.nnson128.chatservice.model.UserMessageKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserMessageRepository extends CassandraRepository<UserMessage, UserMessageKey> {
    @Query("SELECT * FROM user_message WHERE user_id = :userId ORDER BY timestamp DESC ALLOW FILTERING")
    List<UserMessage> findAllByKeyUserIdOrderByKeyTimestampDesc(@Param("userId") UUID userId);
}

