package com.nnson128.relationshipservice.model.channel;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Builder
@Table(name = "channels")
public class Channel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "channel_name")
    private String channelName;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "theme_color")
    private String themeColor; // Hex color for single color theme (e.g., "#0084FF")

    @Column(name = "theme_gradient")
    private String themeGradient; // Gradient definition (e.g., "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")

    @Column(name = "channel_type", nullable = false)
    @Builder.Default
    private String channelType = "GROUP";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static final String GROUP = "GROUP";
    public static final String DIRECT_MESSAGE = "DIRECT_MESSAGE";
}
