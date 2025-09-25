package com.sonnguyen.friendshipservice.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FriendshipKey implements Serializable {

    private UUID requesterId;
    private UUID friendId;
}
