package com.nnson128.chatservice.repository;

import com.nnson128.chatservice.model.ChannelMessage;
import com.nnson128.chatservice.model.ChannelMessageKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.List;
import java.util.UUID;

public interface ChannelMessageRepository extends CassandraRepository<ChannelMessage, ChannelMessageKey> {
    List<ChannelMessage> findAllByKeyChannelIdOrderByKeyMessageIdAsc(UUID channelId);
}
