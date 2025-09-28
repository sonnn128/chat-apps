package com.sonnguyen.userservice.client;

import com.sonnguyen.userservice.dto.ApiResponse;
import com.sonnguyen.userservice.dto.UploadResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "media-service")
public interface MediaServiceClient {
    
    @PostMapping(value = "/api/v1/media/upload", consumes = "multipart/form-data")
    ApiResponse<UploadResponse> uploadFile(@RequestPart("file") MultipartFile file);
    
    @GetMapping("/api/v1/media/url/{publicId}")
    ApiResponse<String> getFileUrl(@PathVariable String publicId);
    
    @DeleteMapping("/api/v1/media/{publicId}")
    ApiResponse<Void> deleteFile(@PathVariable String publicId);
}
