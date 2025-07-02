package com.sonnguyen.channelservice.controller;

import com.sonnguyen.channelservice.dto.request.SendMessageRequest;
import com.sonnguyen.channelservice.model.ChannelMessage;
import com.sonnguyen.channelservice.repository.ChannelMessageRepository;
import com.sonnguyen.channelservice.service.ChannelMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/messages")
public class ChatController {
    private final ChannelMessageService channelMessageService;
    private final ChannelMessageRepository repository;

    @GetMapping("/{channelId}")
    public List<ChannelMessage> getMessagesByChannel(@PathVariable("channelId") UUID channelId) {
        return channelMessageService.getAllMessagesOfChannel(channelId);
    }

    @PostMapping
    public ChannelMessage sendChannelMessage(
            @RequestBody SendMessageRequest sendMessageRequest,
            @RequestHeader("X-Authenticated-User-Id") String authenticatedUserId // Lấy ID user từ header do Gateway thêm vào
    ) {
        return channelMessageService.sendMessage(sendMessageRequest, UUID.fromString(authenticatedUserId));
    }
}
