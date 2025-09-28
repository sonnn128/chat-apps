package com.sonnguyen.channelservice.dto.response;

import com.sonnguyen.channelservice.dto.message.ChannelMessageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPeopleResponse {
    private UUID channelId;
    private String channelName;
    private List<UserResponse> newMembers;
    private ChannelMessageDto message;
}
