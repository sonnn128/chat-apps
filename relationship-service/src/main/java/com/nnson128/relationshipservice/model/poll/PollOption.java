package com.nnson128.relationshipservice.model.poll;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "poll_options")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollOption {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Poll poll;

    @Column(name = "option_text", nullable = false)
    private String optionText;
    
    @Transient
    private int voteCount;
    
    @Transient
    private java.util.List<com.nnson128.chatapps_base.dto.res.UserResponse> voters; // For display avatars
}
