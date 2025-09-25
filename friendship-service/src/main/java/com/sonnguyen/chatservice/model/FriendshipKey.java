package com.sonnguyen.chatservice.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendshipKey implements Serializable {

    private UUID userId;
    private UUID friendId;
}
