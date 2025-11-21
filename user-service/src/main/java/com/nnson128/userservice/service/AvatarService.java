package com.nnson128.userservice.service;

import com.nnson128.chatapps_base.exception.CommonException;
import com.nnson128.userservice.client.MediaServiceClient;
import com.nnson128.chatapps_base.dto.res.ApiResponse;
import com.nnson128.userservice.dto.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
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
            } else {
            }
        } catch (Exception e) {
            // Don't throw exception for cleanup failures
        }
    }
}
