package com.nnson128.chatservice.controller;

import com.nnson128.chatservice.dto.res.LinkPreviewDto;
import com.nnson128.chatservice.service.LinkPreviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/messages/link-preview")
@RequiredArgsConstructor
public class LinkPreviewController {

    private final LinkPreviewService linkPreviewService;

    @PostMapping
    public ResponseEntity<LinkPreviewDto> getLinkPreview(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(linkPreviewService.getLinkPreview(url));
    }
}
