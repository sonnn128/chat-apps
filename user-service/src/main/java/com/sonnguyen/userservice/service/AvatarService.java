package com.sonnguyen.userservice.service;

import com.sonnguyen.userservice.client.MediaServiceClient;
import com.sonnguyen.userservice.dto.ApiResponse;
import com.sonnguyen.userservice.dto.UploadResponse;
import com.sonnguyen.userservice.exception.CommonException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarService {

    private final MediaServiceClient mediaServiceClient;
    private final UserService userService;

    public String uploadAvatar(UUID userId, MultipartFile file) {
        try {
            // Get current avatar publicId before uploading new one
            String currentAvatarPublicId = userService.getUserAvatarPublicId(userId);
            
            // Upload new file to media service
            ApiResponse<UploadResponse> uploadResponse = mediaServiceClient.uploadFile(file);
            
            if (!uploadResponse.isSuccess()) {
                throw new CommonException("Failed to upload file: " + uploadResponse.getMessage(), 
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
            UploadResponse uploadData = uploadResponse.getData();
            
            // Update user with new avatar
            userService.updateUserAvatar(userId, uploadData.getSecureUrl(), uploadData.getPublicId());
            
            // Clean up old avatar asynchronously
            if (currentAvatarPublicId != null && !currentAvatarPublicId.equals(uploadData.getPublicId())) {
                cleanupOldAvatar(currentAvatarPublicId);
            }
            
            return uploadData.getSecureUrl();
            
        } catch (Exception e) {
            log.error("Error uploading avatar for user {}: {}", userId, e.getMessage());
            if (e.getMessage().contains("Connection refused") || e.getMessage().contains("timeout")) {
                throw new CommonException("Media service is currently unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
            }
            throw new CommonException("Failed to upload avatar: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteAvatar(UUID userId) {
        try {
            // Get current avatar publicId
            String currentAvatarPublicId = userService.getUserAvatarPublicId(userId);
            
            // Update user (remove avatar from database)
            userService.updateUserAvatar(userId, null, null);
            
            // Delete from media service
            if (currentAvatarPublicId != null) {
                cleanupOldAvatar(currentAvatarPublicId);
            }
            
        } catch (Exception e) {
            log.error("Error deleting avatar for user {}: {}", userId, e.getMessage());
            if (e.getMessage().contains("Connection refused") || e.getMessage().contains("timeout")) {
                throw new CommonException("Media service is currently unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
            }
            throw new CommonException("Failed to delete avatar: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void cleanupOldAvatar(String publicId) {
        try {
            ApiResponse<Void> deleteResponse = mediaServiceClient.deleteFile(publicId);
            if (!deleteResponse.isSuccess()) {
                log.warn("Failed to cleanup old avatar with publicId {}: {}", 
                        publicId, deleteResponse.getMessage());
            } else {
                log.info("Successfully cleaned up old avatar with publicId: {}", publicId);
            }
        } catch (Exception e) {
            log.warn("Failed to cleanup old avatar {}: {}", publicId, e.getMessage());
            // Don't throw exception for cleanup failures
        }
    }
}
