package com.sonnguyen.chatservice.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;

import java.util.Date;
import java.util.UUID;

public class Message {
    @PrimaryKey
    private UUID id;
    @Column("message_from")
    private UUID message_from;
    @Column("message_to")
    private UUID message_to;
    @Column("content")
    private String content;
    @Column("timestamp")
    private Date timestamp;
}
