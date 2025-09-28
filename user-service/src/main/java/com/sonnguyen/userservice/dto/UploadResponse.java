package com.sonnguyen.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    private String publicId;
    private String secureUrl;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private String mimeType;
    private String mediaType;
}
