package com.nnson128.relationshipservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateChannelThemeRequest {
    private String themeColor;      // Single color (e.g., "#0084FF")
    private String themeGradient;   // Gradient CSS (e.g., "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
}
