package com.nnson128.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResponse {
    private String publicId;
    private String secureUrl;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private String mimeType;
    private String mediaType;
}
