package com.nnson128.relationshipservice.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Configuration
public class FeignConfig {
    
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // Full logging to debug user service calls
    }

    /**
     * Feign request interceptor to propagate JWT token to internal service calls
     */
    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return requestTemplate -> {
            // Get JWT token from current security context
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                // Add Authorization header with Bearer token
                requestTemplate.header("Authorization", "Bearer " + jwt.getTokenValue());
            }
        };
    }
}