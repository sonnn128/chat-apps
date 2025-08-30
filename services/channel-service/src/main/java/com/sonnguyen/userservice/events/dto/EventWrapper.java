package com.sonnguyen.userservice.events.dto;

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

