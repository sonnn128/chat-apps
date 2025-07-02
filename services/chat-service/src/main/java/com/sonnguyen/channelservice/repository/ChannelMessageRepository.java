package com.sonnguyen.channelservice.repository;

import com.sonnguyen.channelservice.model.ChannelMessage;
import com.sonnguyen.channelservice.model.ChannelMessageKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.List;
import java.util.UUID;

public interface ChannelMessageRepository extends CassandraRepository<ChannelMessage, ChannelMessageKey> {
    List<ChannelMessage> findAllByKeyChannelIdOrderByKeyMessageIdAsc(UUID channelId);
}
