package com.nnson128.relationshipservice.model.poll;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "polls")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Poll {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "channel_id", nullable = false)
    private UUID channelId;

    @Column(name = "creator_id", nullable = false)
    private UUID creatorId;

    @Column(nullable = false)
    private String question;

    @Builder.Default
    @Column(name = "allow_multiple_votes")
    private boolean allowMultipleVotes = false;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PollStatus status = PollStatus.OPEN;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PollOption> options;
}
