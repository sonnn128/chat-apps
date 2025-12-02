package com.nnson128.relationshipservice.dto.response;

import com.nnson128.relationshipservice.model.membership.MembershipRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelParticipantResponse {
    private UUID userId;
    private String firstname;
    private String lastname;
    private String email;
    private String avatarUrl;
    private MembershipRole role;
}
