//// src/main/java/com/sonnguyen/channelservice/config/WebConfig.java
//
//package com.sonnguyen.userservice.config;
//
//import org.springframework.cloud.client.loadbalancer.LoadBalanced;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.filter.CorsFilter;
//
//import java.util.List;
//// import org.springframework.web.filter.CorsFilter; // <-- No longer needed
//
//@Configuration
//public class WebConfig {
//
//    @Bean
//    @LoadBalanced
//    public RestTemplate restTemplate() {
//        return new RestTemplate();
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        // It's better to be explicit than to use "*"
//         configuration.setAllowedOrigins(List.of("http://localhost:5173"));
//        configuration.addAllowedOriginPattern("*"); // For development, "*" is fine.
//        configuration.addAllowedMethod("*");
//        configuration.addAllowedHeader("*");
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//
//    @Bean
//    public CorsFilter corsFilter(CorsConfigurationSource corsConfigurationSource) {
//        return new CorsFilter(corsConfigurationSource);
//    }
//}