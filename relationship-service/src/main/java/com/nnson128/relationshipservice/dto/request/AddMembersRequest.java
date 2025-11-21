package com.nnson128.relationshipservice.dto.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AddMembersRequest {
    private List<UUID> userIds;
}
