package com.example.userservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;

@Configuration
public class FeignClientConfiguration {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String username = request.getHeader("X-Authenticated-User-Name");
                    String userRoles = request.getHeader("X-Authenticated-User-Roles");
                    if (username != null) {
                        template.header("X-Authenticated-User-Name", username);
                    }
                    if (userRoles != null) {
                        template.header("X-Authenticated-User-Roles", userRoles);
                    }
                }
            }
        };
    }
}
