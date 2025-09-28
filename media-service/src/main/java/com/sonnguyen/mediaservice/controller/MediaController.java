package com.sonnguyen.mediaservice.controller;

import com.sonnguyen.mediaservice.dto.ApiResponse;
import com.sonnguyen.mediaservice.dto.UploadResponse;
import com.sonnguyen.mediaservice.exception.FileValidationException;
import com.sonnguyen.mediaservice.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            UploadResponse response = mediaService.uploadFile(file);
            return ResponseEntity.ok(ApiResponse.<UploadResponse>builder()
                    .success(true)
                    .message("File uploaded successfully")
                    .data(response)
                    .build());
        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<UploadResponse>builder()
                            .success(false)
                            .message("Failed to upload file: " + e.getMessage())
                            .build());
        } catch (FileValidationException e) {
            log.error("File validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<UploadResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (RuntimeException e) {
            log.error("Error uploading file: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<UploadResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    @GetMapping("/url/{publicId}")
    public ResponseEntity<ApiResponse<String>> getFileUrl(@PathVariable String publicId) {
        try {
            String url = mediaService.getFileUrl(publicId);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(true)
                    .message("File URL retrieved successfully")
                    .data(url)
                    .build());
        } catch (Exception e) {
            log.error("Error getting file URL for publicId {}: {}", publicId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<String>builder()
                            .success(false)
                            .message("Failed to get file URL: " + e.getMessage())
                            .build());
        }
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable String publicId) {
        try {
            mediaService.deleteFile(publicId);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("File deleted successfully")
                    .build());
        } catch (IOException e) {
            log.error("Error deleting file with publicId {}: {}", publicId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Failed to delete file: " + e.getMessage())
                            .build());
        }
    }
}