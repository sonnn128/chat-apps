package com.sonnguyen.chatservice.model;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED;
import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.CLUSTERED;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@PrimaryKeyClass
public class UserMessageKey {
    @PrimaryKeyColumn(name = "user_id", type = PARTITIONED)
    private UUID userId;
    
    @PrimaryKeyColumn(name = "timestamp", type = CLUSTERED, ordering = org.springframework.data.cassandra.core.cql.Ordering.DESCENDING)
    private Instant timestamp;
    
    @PrimaryKeyColumn(name = "message_id", type = CLUSTERED)
    private UUID messageId;
    
    @PrimaryKeyColumn(name = "channel_id", type = CLUSTERED)
    private UUID channelId;
}

