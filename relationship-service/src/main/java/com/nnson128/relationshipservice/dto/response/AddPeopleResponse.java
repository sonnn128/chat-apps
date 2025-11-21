package com.nnson128.relationshipservice.dto.response;

import com.nnson128.chatapps_base.dto.res.UserResponse;
import com.nnson128.relationshipservice.dto.message.ChannelMessageDto;
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
