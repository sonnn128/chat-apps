package com.sonnguyen.chatservice.controller;

import com.sonnguyen.chatservice.dto.request.SendMessageRequest;
import com.sonnguyen.chatservice.dto.response.ChannelMessageDto;
import com.sonnguyen.chatservice.dto.response.ChannelMessageKeyDto;
import com.sonnguyen.chatservice.model.ChannelMessage;
import com.sonnguyen.chatservice.service.ChannelMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/messages")
@Validated
public class ChatController {

    private final ChannelMessageService channelMessageService;

    @GetMapping("/{channelId}")
    public ResponseEntity<List<ChannelMessageDto>> getMessagesByChannel(@PathVariable("channelId") UUID channelId) {
        log.info("Fetching messages for channelId: {}", channelId);
        List<ChannelMessage> messages = channelMessageService.getAllMessagesOfChannel(channelId);
        List<ChannelMessageDto> messageDtos = messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(messageDtos);
    }

    @PostMapping
    public ResponseEntity<ChannelMessageDto> sendChannelMessage(
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        ChannelMessage sentMessage = channelMessageService.sendMessage(request, userId);
        ChannelMessageDto messageDto = convertToDto(sentMessage);
        return new ResponseEntity<>(messageDto, HttpStatus.CREATED);
    }

    @PostMapping("/save-only")
    public ResponseEntity<ChannelMessageDto> saveChannelMessage(
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Saving a message (without producing event) to channelId: {}", request.getChannelId());
        UUID userId = UUID.fromString(jwt.getSubject());

        ChannelMessage savedMessage = channelMessageService.saveMessage(request, userId);
        ChannelMessageDto messageDto = convertToDto(savedMessage);
        return new ResponseEntity<>(messageDto, HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<UUID, List<ChannelMessageDto>>> getBatchChannelMessages(
            @RequestBody List<@NotNull UUID> channelIds) {
        Map<UUID, List<ChannelMessageDto>> messagesMap = channelMessageService.getBatchChannelMessages(channelIds);

        return ResponseEntity.ok(messagesMap);
    }

    @GetMapping("/user/{userId}/all")
    public ResponseEntity<Map<UUID, List<ChannelMessageDto>>> getAllMessagesByUserId(
            @PathVariable UUID userId) {
        log.info("Fetching all messages for user: {}", userId);
        
        Map<UUID, List<ChannelMessage>> messagesMap = channelMessageService.getAllMessagesByUserId(userId);
        Map<UUID, List<ChannelMessageDto>> dtoMap = messagesMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(this::convertToDto)
                                .collect(Collectors.toList())
                ));
        return ResponseEntity.ok(dtoMap);
    }

    private ChannelMessageDto convertToDto(ChannelMessage message) {
        if (message == null) {
            return null;
        }
        
        return ChannelMessageDto.builder()
                .key(ChannelMessageKeyDto.builder()
                        .channelId(message.getKey().getChannelId())
                        .messageId(message.getKey().getMessageId())
                        .build())
                .userId(message.getUserId())
                .content(message.getContent())
                .type(message.getType())
                .timestamp(message.getTimestamp())
                .build();
    }

}