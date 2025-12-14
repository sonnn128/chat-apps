package com.nnson128.chatservice.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkPreviewDto {
    private String url;
    private String title;
    private String description;
    private String image;
    private String domain;
    private String siteName;
}
