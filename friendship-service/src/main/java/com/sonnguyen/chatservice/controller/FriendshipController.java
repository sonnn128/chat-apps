package com.sonnguyen.chatservice.controller;

import com.sonnguyen.chatservice.dto.request.SendMessageRequest;
import com.sonnguyen.chatservice.model.ChannelMessage;
import com.sonnguyen.chatservice.service.ChannelMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/messages")
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
            @RequestBody SendMessageRequest request,
            @RequestHeader("X-User-Id") String authenticatedUserId) {
        log.info("Sending a message to channelId: {}", request);
        log.info("authenticatedUserId: {}", authenticatedUserId);
        ChannelMessage sentMessage = channelMessageService.sendMessage(request, UUID.fromString(authenticatedUserId));
        return new ResponseEntity<>(sentMessage, HttpStatus.CREATED);
    }

    @PostMapping("/save-only")
    public ResponseEntity<ChannelMessage> saveChannelMessage(
            @RequestBody SendMessageRequest request,
            @RequestHeader("X-User-Id") String authenticatedUserId) {
        log.info("Saving a message (without producing event) to channelId: {}", request.getChannelId());

        ChannelMessage savedMessage = channelMessageService.saveMessage(request, UUID.fromString(authenticatedUserId));
        return new ResponseEntity<>(savedMessage, HttpStatus.CREATED);
    }

}