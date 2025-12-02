package com.nnson128.relationshipservice.dto.response;

import com.nnson128.chatapps_base.dto.res.UserResponse;
import com.nnson128.relationshipservice.dto.message.ChannelMessageDto;
import com.nnson128.relationshipservice.model.channel.Channel;
import com.nnson128.relationshipservice.model.membership.MembershipRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ChannelResponse {
    private UUID id;
    private String channelName;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private List<ChannelMessageDto> messages; // Messages from chat-service
    private List<UUID> memberIds; // Member IDs
    private List<ChannelParticipantResponse> participants; // Detailed participant info
    private ChannelMessageDto message; // Notice message for channel creation
    private MembershipRole role; // Current user's role in the channel

    public static ChannelResponse from(Channel channel) {
        return ChannelResponse.builder()
                .id(channel.getId())
                .channelName(channel.getChannelName())
                .createdAt(channel.getCreatedAt())
                .messages(List.<ChannelMessageDto>of()) // Initialize empty messages list
                .memberIds(List.<UUID>of()) // Initialize empty memberIds list
                .build();
    }

    public static ChannelResponse fromChannelAndMessage(ChannelResponse channelResponse) {
        return ChannelResponse.builder()
                .id(channelResponse.getId())
                .channelName(channelResponse.getChannelName())
                .createdBy(channelResponse.getCreatedBy())
                .createdAt(channelResponse.getCreatedAt())
                .messages(channelResponse.getMessages())
                .memberIds(channelResponse.getMemberIds())
                .build();
    }

}
