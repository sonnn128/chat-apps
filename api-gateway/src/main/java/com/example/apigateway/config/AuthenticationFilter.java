package com.example.apigateway.config;

import com.example.apigateway.dto.ApiResponse;
import com.example.apigateway.service.IdentityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final IdentityService identityService;
    private final ObjectMapper objectMapper;
    private final String[] publicEndpoints = {
            "/api/auth/login",
            "/api/auth/introspect",
            "/api/products/admin",
            "/ws/.*",
    };


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (isPublicEndpoint(request)) {
            return chain.filter(exchange);
        }

        List<String> authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeader)) {
            return unauthenticated(exchange.getResponse());
        }

        String token = authHeader.get(0).replace("Bearer ", "");

        return identityService.introspect(token)
                .flatMap(response -> {
                    if (!response.valid()) {
                        return unauthenticated(exchange.getResponse());
                    }
                    String username = response.username();
                    List<String> roles = response.roles();
                    ServerHttpRequest newRequest = exchange.getRequest().mutate()
                            .header("X-Authenticated-User-Name", username)
                            .header("X-Authenticated-User-Roles", String.join(",", roles))
                            .build();
                    ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
                    log.info("Authenticated user {}, forwarding request to service", username);
                    return chain.filter(newExchange);
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return Arrays.stream(publicEndpoints).anyMatch(path::matches);
    }

    private Mono<Void> unauthenticated(ServerHttpResponse response) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(401)
                .message("Unauthenticated")
                .build();
        String body;
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            log.error("Error writing JSON response", e);
            body = "";
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}
