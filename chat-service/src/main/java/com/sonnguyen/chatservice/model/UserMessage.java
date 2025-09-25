package com.sonnguyen.chatservice.model;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("user_message")
public class UserMessage {
    @PrimaryKey
    private UserMessageKey key;
    @Column("content")
    private String content;
    @Column("type")
    private ChannelMessageType type;
}

