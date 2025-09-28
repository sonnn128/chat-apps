package com.sonnguyen.mediaservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sonnguyen.mediaservice.dto.UploadResponse;
import com.sonnguyen.mediaservice.exception.FileValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    private static final String PUBLIC_ID_KEY = "public_id";
    private static final String RESOURCE_TYPE_AUTO = "auto";
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final Cloudinary cloudinary;

    public UploadResponse uploadFile(MultipartFile file) throws IOException {
        // Validate Cloudinary credentials
        validateCloudinaryCredentials();
        
        // Validate file
        validateFile(file);

        // Generate unique filename
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String fileName = UUID.randomUUID().toString() + fileExtension;

        // Upload to Cloudinary
        Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        PUBLIC_ID_KEY, fileName,
                        "resource_type", RESOURCE_TYPE_AUTO
                )
        );

        // Determine media type
        MediaType mediaType = determineMediaType(file.getContentType());

        // Get URL from upload result (includes version)
        String publicId = (String) uploadResult.get(PUBLIC_ID_KEY);
        String secureUrl = (String) uploadResult.get("secure_url");

        // Create response
        UploadResponse response = new UploadResponse();
        response.setPublicId(publicId);
        response.setSecureUrl(secureUrl);
        response.setOriginalFileName(originalFileName);
        response.setFileType(fileExtension);
        response.setFileSize(file.getSize());
        response.setMimeType(file.getContentType());
        response.setMediaType(mediaType);

        log.info("File uploaded successfully: {}", originalFileName);
        return response;
    }

    public String getFileUrl(String publicId) {
        return cloudinary.url().generate(publicId);
    }

    public void deleteFile(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        log.info("File deleted successfully: {}", publicId);
    }

    private void validateCloudinaryCredentials() {
        try {
            // Test Cloudinary connection with a simple ping
            cloudinary.api().ping(ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.warn("Cloudinary credentials are invalid or service is unavailable. Using demo mode.");
            log.warn("Please set valid CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET environment variables.");
        }
    }

    private boolean isDemoMode() {
        try {
            cloudinary.api().ping(ObjectUtils.emptyMap());
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private UploadResponse createDemoResponse(MultipartFile file) {
        String publicId = "demo_" + UUID.randomUUID().toString();
        String secureUrl = "https://via.placeholder.com/300x300?text=Demo+Image";
        
        log.info("Demo mode: File {} simulated as {}", file.getOriginalFilename(), publicId);
        
        UploadResponse response = new UploadResponse();
        response.setPublicId(publicId);
        response.setSecureUrl(secureUrl);
        response.setMediaType(determineMediaType(file.getContentType()));
        
        return response;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileValidationException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileValidationException("File size exceeds 10MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isValidContentType(contentType)) {
            throw new FileValidationException("Invalid file type. Only images, videos, audio, and documents are allowed");
        }
    }

    private boolean isValidContentType(String contentType) {
        return contentType.startsWith("image/") ||
               contentType.startsWith("video/") ||
               contentType.startsWith("audio/") ||
               contentType.equals("application/pdf") ||
               contentType.equals("application/msword") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
               contentType.equals("text/plain");
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private MediaType determineMediaType(String contentType) {
        if (contentType.startsWith("image/")) {
            return MediaType.IMAGE;
        } else if (contentType.startsWith("video/")) {
            return MediaType.VIDEO;
        } else if (contentType.startsWith("audio/")) {
            return MediaType.AUDIO;
        } else {
            return MediaType.DOCUMENT;
        }
    }

    public enum MediaType {
        IMAGE, VIDEO, AUDIO, DOCUMENT
    }
}