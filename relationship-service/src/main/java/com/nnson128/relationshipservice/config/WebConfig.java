package com.nnson128.relationshipservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfig {

    @Bean
    @LoadBalanced // Quan trọng: Giúp RestTemplate phân giải tên service (user-service) qua Eureka
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    public AuthenticationManager authenticationManager() {
        return authentication -> null;
    }
}
