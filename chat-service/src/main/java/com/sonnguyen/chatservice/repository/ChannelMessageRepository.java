package com.sonnguyen.chatservice.repository;

import com.sonnguyen.chatservice.model.ChannelMessage;
import com.sonnguyen.chatservice.model.ChannelMessageKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ChannelMessageRepository extends CassandraRepository<ChannelMessage, ChannelMessageKey> {
    List<ChannelMessage> findAllByKeyChannelIdOrderByKeyMessageIdAsc(UUID channelId);
}
