package com.sonnguyen.chatservice.controller;

import com.sonnguyen.chatservice.dto.request.SendMessageRequest;
import com.sonnguyen.chatservice.model.ChannelMessage;
import com.sonnguyen.chatservice.service.ChannelMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/messages")
@Validated
public class ChatController {

    private final ChannelMessageService channelMessageService;

    @GetMapping("/{channelId}")
    public ResponseEntity<List<ChannelMessage>> getMessagesByChannel(@PathVariable("channelId") UUID channelId) {
        log.info("Fetching messages for channelId: {}", channelId);
        List<ChannelMessage> messages = channelMessageService.getAllMessagesOfChannel(channelId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    public ResponseEntity<ChannelMessage> sendChannelMessage(
            @Valid @RequestBody SendMessageRequest request,
            @RequestHeader("X-User-Id") @NotNull String authenticatedUserId) {
        log.info("Sending a message to channelId: {}", request);
        log.info("authenticatedUserId: {}", authenticatedUserId);
        
        UUID userId;
        try {
            userId = UUID.fromString(authenticatedUserId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID format: {}", authenticatedUserId);
            throw new IllegalArgumentException("Invalid user ID format");
        }
        
        ChannelMessage sentMessage = channelMessageService.sendMessage(request, userId);
        return new ResponseEntity<>(sentMessage, HttpStatus.CREATED);
    }

    @PostMapping("/save-only")
    public ResponseEntity<ChannelMessage> saveChannelMessage(
            @Valid @RequestBody SendMessageRequest request,
            @RequestHeader("X-User-Id") @NotNull String authenticatedUserId) {
        log.info("Saving a message (without producing event) to channelId: {}", request.getChannelId());

        UUID userId;
        try {
            userId = UUID.fromString(authenticatedUserId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID format: {}", authenticatedUserId);
            throw new IllegalArgumentException("Invalid user ID format");
        }

        ChannelMessage savedMessage = channelMessageService.saveMessage(request, userId);
        return new ResponseEntity<>(savedMessage, HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<UUID, List<ChannelMessage>>> getBatchChannelMessages(
            @RequestBody @NotEmpty List<@NotNull UUID> channelIds) {
        log.info("Fetching messages for {} channels", channelIds.size());
        
        Map<UUID, List<ChannelMessage>> messagesMap = channelMessageService.getBatchChannelMessages(channelIds);
        return ResponseEntity.ok(messagesMap);
    }

    @GetMapping("/user/{userId}/all")
    public ResponseEntity<Map<UUID, List<ChannelMessage>>> getAllMessagesByUserId(
            @PathVariable UUID userId) {
        log.info("Fetching all messages for user: {}", userId);
        
        Map<UUID, List<ChannelMessage>> messagesMap = channelMessageService.getAllMessagesByUserId(userId);
        return ResponseEntity.ok(messagesMap);
    }

}