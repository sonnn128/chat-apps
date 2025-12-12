package com.nnson128.chatservice.controller;

import com.nnson128.chatservice.dto.req.SendMessageRequest;
import com.nnson128.chatservice.dto.res.ChannelMessageDto;
import com.nnson128.chatservice.model.ChannelMessage;
import com.nnson128.chatservice.service.ChannelMessageService;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/messages")
@Validated
public class ChatController {

    private final ChannelMessageService channelMessageService;

    @GetMapping("/{channelId}")
    public ResponseEntity<List<ChannelMessageDto>> getMessagesByChannel(@PathVariable("channelId") UUID channelId) {
        List<ChannelMessage> messages = channelMessageService.getAllMessagesOfChannel(channelId);
        List<ChannelMessageDto> messageDtos = messages.stream()
                .map(ChannelMessageDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(messageDtos);
    }

    @PostMapping
    public ResponseEntity<ChannelMessageDto> sendChannelMessage(
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        ChannelMessage sentMessage = channelMessageService.sendMessage(request, userId);
        ChannelMessageDto messageDto = ChannelMessageDto.from(sentMessage);
        return new ResponseEntity<>(messageDto, HttpStatus.CREATED);
    }

    @DeleteMapping("/{channelId}/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable("channelId") UUID channelId,
            @PathVariable("messageId") UUID messageId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject()); // Get authenticated user ID
        channelMessageService.deleteMessage(channelId, messageId, userId);
        return ResponseEntity.noContent().build();
    }
    
    // Internal endpoint for service-to-service communication (no JWT required)
    @PostMapping("/internal")
    public ResponseEntity<ChannelMessageDto> sendInternalMessage(
            @Valid @RequestBody SendMessageRequest request,
            @RequestParam("userId") UUID userId) {
        ChannelMessage sentMessage = channelMessageService.sendMessage(request, userId);
        ChannelMessageDto messageDto = ChannelMessageDto.from(sentMessage);
        return new ResponseEntity<>(messageDto, HttpStatus.CREATED);
    }
   
    @PostMapping("/batch")
    public ResponseEntity<Map<UUID, List<ChannelMessageDto>>> getBatchChannelMessages(
            @RequestBody List<@NotNull UUID> channelIds) {
        Map<UUID, List<ChannelMessageDto>> messagesMap = channelMessageService.getBatchChannelMessages(channelIds);
        return ResponseEntity.ok(messagesMap);
    }

}