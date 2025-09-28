package com.sonnguyen.userservice.client;

import com.sonnguyen.userservice.dto.ApiResponse;
import com.sonnguyen.userservice.dto.UploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
@Slf4j
public class MediaServiceTestClient {

    private final MediaServiceClient mediaServiceClient;

    public boolean testConnection() {
        try {
            // Try to get a file URL (this will test the connection)
            ApiResponse<String> response = mediaServiceClient.getFileUrl("test-connection");
            
            // Even if the file doesn't exist, if we get a response, the service is reachable
            log.info("Media service connection test - Success: {}, Message: {}", 
                    response.isSuccess(), response.getMessage());
            
            return true;
        } catch (Exception e) {
            log.error("Media service connection test failed: {}", e.getMessage());
            return false;
        }
    }

    public void logServiceStatus() {
        boolean isConnected = testConnection();
        if (isConnected) {
            log.info("✅ Media service is reachable");
        } else {
            log.warn("❌ Media service is not reachable");
        }
    }
}
