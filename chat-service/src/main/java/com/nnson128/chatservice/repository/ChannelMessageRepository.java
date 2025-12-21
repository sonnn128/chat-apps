package com.nnson128.chatservice.repository;

import com.nnson128.chatservice.model.ChannelMessage;
import com.nnson128.chatservice.model.ChannelMessageKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ChannelMessageRepository extends CassandraRepository<ChannelMessage, ChannelMessageKey> {
    List<ChannelMessage> findAllByKeyChannelIdOrderByKeyMessageIdAsc(UUID channelId);

    Slice<ChannelMessage> findByKeyChannelId(UUID channelId, Pageable pageable);
}
