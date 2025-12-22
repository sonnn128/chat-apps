package com.nnson128.chatapps_base.models.events.channel.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nnson128.chatapps_base.models.events.channel.ChannelEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelUpdatedPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("event_type")
    private ChannelEventType eventType;
    private UUID channelId;
    private String newChannelName;
    private UUID updaterId;
    private String updaterName;
    private String updatedAt;
    
    @JsonProperty("member_ids")
    private java.util.List<java.util.UUID> memberIds;
    
    @JsonProperty("theme_color")
    private String themeColor;
    
    @JsonProperty("theme_gradient")
    private String themeGradient;
}
