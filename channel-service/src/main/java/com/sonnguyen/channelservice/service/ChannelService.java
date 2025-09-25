package com.sonnguyen.channelservice.service;

import com.sonnguyen.channelservice.client.UserServiceClient;
import com.sonnguyen.channelservice.client.ChatServiceClient;
import com.sonnguyen.channelservice.dto.request.CreateChannelRequest;
import com.sonnguyen.channelservice.dto.request.SendMessageRequest;
import com.sonnguyen.channelservice.dto.response.ChannelMessageDto;
import com.sonnguyen.channelservice.dto.response.ChannelResponse;
import com.sonnguyen.channelservice.dto.response.MessageResponse;
import com.sonnguyen.channelservice.dto.response.UserResponse;
import com.sonnguyen.channelservice.dto.response.ApiResponse;
import com.sonnguyen.channelservice.exception.ChannelNotFoundException;
import com.sonnguyen.channelservice.exception.ExternalServiceException;
import com.sonnguyen.channelservice.events.producer.EventWrapper;
import com.sonnguyen.channelservice.events.producer.MembersAddedToChannelEvent;
import com.sonnguyen.channelservice.events.producer.ChannelCreatedEvent;
import com.sonnguyen.channelservice.model.channel.Channel;
import com.sonnguyen.channelservice.model.membership.Membership;
import com.sonnguyen.channelservice.dto.message.ChannelMessageType;
import com.sonnguyen.channelservice.model.membership.MembershipKey;
import com.sonnguyen.channelservice.model.membership.MembershipRole;
import com.sonnguyen.channelservice.repository.ChannelRepository;
import com.sonnguyen.channelservice.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {

    private static final String NOTIFICATION_TOPIC = "notifications-topic";

    private final ChannelRepository channelRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserServiceClient userServiceClient;
    private final ChatServiceClient chatServiceClient;
    private final MembershipRepository membershipRepository;
    

    public ChannelResponse createChannel(CreateChannelRequest request, UUID creatorId) {
        // 1. create channel
        Channel newChannel = Channel.builder()
                .channelName(request.getChannelName())
                .build();

        // 2. save and get channel
        Channel savedChannel = channelRepository.save(newChannel);

        // 3. create relationship
        membershipRepository.save(Membership.builder()
                        .membershipKey(MembershipKey.builder()
                                .channelId(savedChannel.getId())
                                .userId(creatorId)
                                .build())
                        .role(MembershipRole.ADMIN)
                .build());

        // 4. publish event for chat-service to create notice message
        List<UUID> memberIds = List.of(creatorId);
        produceNewChannelEvent(savedChannel, creatorId, memberIds);

        return ChannelResponse.from(savedChannel);
    }

    public List<ChannelResponse> getChannelsForUser(UUID userId) {
        log.info("🔍 ChannelService: Getting channels for user: {}", userId);
        
        List<Membership> memberships = membershipRepository.findByMembershipKeyUserId(userId);
        log.info("📋 ChannelService: Found {} memberships for user: {}", memberships.size(), userId);
        
        List<ChannelResponse> channels = memberships.stream()
                .map(membership -> {
                    UUID channelId = membership.getMembershipKey().getChannelId();
                    return channelRepository.findById(channelId)
                            .map(ChannelResponse::from)
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        log.info("✅ ChannelService: Returning {} channels for user: {}", channels.size(), userId);
        return channels;
    }

    public List<ChannelResponse> getChannelsWithMessagesForUser(UUID userId) {
        log.info("🔍 ChannelService: Getting channels with messages for user: {}", userId);
        
        List<Membership> memberships = membershipRepository.findByMembershipKeyUserId(userId);
        log.info("📋 ChannelService: Found {} memberships for user: {}", memberships.size(), userId);
        
        // Get all channel IDs
        List<UUID> channelIds = memberships.stream()
                .map(membership -> membership.getMembershipKey().getChannelId())
                .collect(Collectors.toList());
        
        // Get all channels
        List<Channel> channels = channelRepository.findAllById(channelIds);
        
        // Get all messages for all channels in one API call
        Map<UUID, List<ChannelMessageDto>> rawMessagesMap = chatServiceClient.getAllMessagesByUserId(userId);
        log.info("📨 ChannelService: Received {} message groups from chat-service", rawMessagesMap.size());
        
        // Convert ChannelMessageDto objects to MessageResponse objects
        Map<UUID, List<MessageResponse>> messagesMap = rawMessagesMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(this::convertToMessageResponse)
                                .collect(Collectors.toList())
                ));
        
        // Get all member IDs for all channels
        Map<UUID, List<UUID>> memberIdsMap = getMemberIdsForAllChannels(channelIds);
        log.info("👥 ChannelService: Retrieved member IDs for {} channels", memberIdsMap.size());
        
        // Build response using stream map
        List<ChannelResponse> channelResponses = channels.stream()
                .map(channel -> {
                    List<MessageResponse> channelMessages = messagesMap.getOrDefault(channel.getId(), List.of());
                    List<UUID> channelMembers = memberIdsMap.getOrDefault(channel.getId(), List.of());
                    
                    log.info("📋 ChannelService: Channel {} has {} messages and {} members", 
                            channel.getId(), channelMessages.size(), channelMembers.size());
                    
                    return ChannelResponse.builder()
                            .id(channel.getId())
                            .channelName(channel.getChannelName())
                            .createdAt(channel.getCreatedAt())
                            .messages(channelMessages)
                            .memberIds(channelMembers)
                            .build();
                })
                .collect(Collectors.toList());
        
        log.info("✅ ChannelService: Returning {} channels with messages for user: {}", channelResponses.size(), userId);
        return channelResponses;
    }

    public boolean isUserParticipant(UUID channelId, UUID userId) {
        // Check if channel exists
        if (!channelRepository.existsById(channelId)) {
            throw new ChannelNotFoundException("Channel with id " + channelId + " not found");
        }
        
        return membershipRepository.existsByMembershipKeyChannelIdAndMembershipKeyUserId(channelId, userId);
    }

    public List<UUID> getParticipantIdsByChannelId(UUID channelId) {
        // Check if channel exists
        if (!channelRepository.existsById(channelId)) {
            throw new ChannelNotFoundException("Channel with id " + channelId + " not found");
        }
        
        return membershipRepository.findByMembershipKeyChannelId(channelId).stream()
                .map(membership -> membership.getMembershipKey().getUserId())
                .collect(Collectors.toList());
    }


    private void produceNewChannelEvent(Channel channel, UUID creatorId, List<UUID> memberIds) {
        UserResponse creatorProfile = userServiceClient.getUserById(creatorId);
        String creatorName = creatorProfile != null ? (creatorProfile.getFirstname() + " " + creatorProfile.getLastname()) : "A user";

        ChannelCreatedEvent event = ChannelCreatedEvent.builder()
                .channelId(channel.getId())
                .channelName(channel.getChannelName())
                .creatorId(creatorId)
                .creatorName(creatorName)
                .createdAt(channel.getCreatedAt())
                .memberIds(memberIds)
                .build();
        log.info("Publishing ChannelCreatedEvent: {}", event);
        kafkaTemplate.send(NOTIFICATION_TOPIC, new EventWrapper<>(ChannelCreatedEvent.EVENT_TYPE, event));
        log.info("Produced ChannelCreatedEvent for channel {}", channel.getId());
    }



    /**
     * Get member IDs for all channels
     */
    private Map<UUID, List<UUID>> getMemberIdsForAllChannels(List<UUID> channelIds) {
        log.info("👥 ChannelService: Getting member IDs for {} channels", channelIds.size());
        
        return channelIds.stream()
                .collect(Collectors.toMap(
                        channelId -> channelId,
                        channelId -> {
                            try {
                                return membershipRepository.findByMembershipKeyChannelId(channelId)
                                        .stream()
                                        .map(membership -> membership.getMembershipKey().getUserId())
                                        .collect(Collectors.toList());
                            } catch (Exception e) {
                                log.error("❌ ChannelService: Error getting members for channel {}: {}", channelId, e.getMessage());
                                return List.<UUID>of();
                            }
                        }
                ));
    }

    /**
     * Get channel IDs for a user (without messages)
     * Used by chat-service to get all channels for batch message retrieval
     */
    public List<UUID> getChannelIdsByUserId(UUID userId) {
        log.info("🔍 ChannelService: Getting channel IDs for user: {}", userId);
        
        List<Membership> memberships = membershipRepository.findByMembershipKeyUserId(userId);
        List<UUID> channelIds = memberships.stream()
                .map(membership -> membership.getMembershipKey().getChannelId())
                .collect(Collectors.toList());
        
        log.info("✅ ChannelService: Found {} channels for user: {}", channelIds.size(), userId);
        return channelIds;
    }
    
    /**
     * Convert ChannelMessageDto to MessageResponse
     */
    private MessageResponse convertToMessageResponse(ChannelMessageDto messageDto) {
        try {
            log.info("🔍 ChannelService: Converting message DTO: {}", messageDto);
            
            if (messageDto == null) {
                log.warn("⚠️ ChannelService: Message DTO is null");
                return MessageResponse.builder().build();
            }
            
            if (messageDto.getKey() == null) {
                log.warn("⚠️ ChannelService: Message DTO key is null");
                return MessageResponse.builder().build();
            }
            
            MessageResponse.MessageKey key = MessageResponse.MessageKey.builder()
                    .channelId(messageDto.getKey().getChannelId())
                    .messageId(messageDto.getKey().getMessageId())
                    .build();
            
            MessageResponse response = MessageResponse.builder()
                    .key(key)
                    .userId(messageDto.getUserId())
                    .content(messageDto.getContent())
                    .type(messageDto.getType())
                    .timestamp(messageDto.getTimestamp())
                    .build();
            
            log.info("🔍 ChannelService: Converted to MessageResponse: {}", response);
            return response;
        } catch (Exception e) {
            log.error("❌ ChannelService: Error converting message DTO: {}", e.getMessage(), e);
            return MessageResponse.builder().build();
        }
    }
    
}
