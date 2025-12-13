package com.nnson128.chatapps_base.models.events.call.payloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallSignalPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type; // OFFER, ANSWER, ICE_CANDIDATE, HANGUP
    private String sdp;
    private Object candidate; // Using Object to be flexible with different ICE structures if needed, usually Map or Class
    
    private UUID callerId;
    private UUID calleeId;
    private UUID channelId;
    
    private UUID senderId;
    
    private boolean isVideo;
}
