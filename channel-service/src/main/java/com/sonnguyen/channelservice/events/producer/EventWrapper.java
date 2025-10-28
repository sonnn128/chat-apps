package com.sonnguyen.channelservice.legacy.events.producer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventWrapper<T> {
    private String eventType;
    private T payload;
}

