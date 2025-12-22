package com.nnson128.relationshipservice.model.poll;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "poll_votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"poll_id", "option_id", "user_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollVote {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "poll_id", nullable = false)
    private UUID pollId;

    @Column(name = "option_id", nullable = false)
    private UUID optionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @CreationTimestamp
    private LocalDateTime votedAt;
}
