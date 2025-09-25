package com.sonnguyen.channelservice.model.membership;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode // rất quan trọng cho composite key
@Embeddable
@Builder
public class MembershipKey implements Serializable {
    private UUID userId;
    private UUID channelId;
}
