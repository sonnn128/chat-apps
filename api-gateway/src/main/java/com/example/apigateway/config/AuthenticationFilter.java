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

    private final List<String> publicPaths = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/introspect"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (isPublicEndpoint(request)) {
            return chain.filter(exchange);
        }

        List<String> authHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeaders)) {
            log.warn("Unauthenticated request: missing Authorization header");
            return unauthenticated(exchange.getResponse());
        }

        String authHeader = authHeaders.get(0);
        if (!authHeader.startsWith("Bearer ")) {
            log.warn("Unauthenticated request: Authorization header is not a Bearer token");
            return unauthenticated(exchange.getResponse());
        }

        String token = authHeader.substring(7);

        return identityService.introspect(token)
                .flatMap(introspectResponse -> {
                    if (introspectResponse == null || !introspectResponse.active()) {
                        log.warn("Token is invalid or inactive");
                        return unauthenticated(exchange.getResponse());
                    }

                    // Thêm thông tin user và roles vào header
                    ServerHttpRequest newRequest = exchange.getRequest().mutate()
                            .header("X-Authenticated-User-Username", introspectResponse.username())
                            .header("X-Authenticated-User-Roles", String.join(",", introspectResponse.roles()))
                            .build();

                    log.info("Authenticated user {}, forwarding request to service", introspectResponse.username());
                    return chain.filter(exchange.mutate().request(newRequest).build());
                });
    }

    @Override
    public int getOrder() {
        return -1; // Đặt độ ưu tiên cao
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        boolean isPublic = publicPaths.contains(path);

        // Xử lý riêng cho WebSocket
        if (!isPublic && path.startsWith("/ws")) {
            return true;
        }

        return isPublic;
    }

    private Mono<Void> unauthenticated(ServerHttpResponse response) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(HttpStatus.UNAUTHORIZED.value()) // Dùng HttpStatus cho nhất quán
                .message("Unauthenticated")
                .build();
        String body;
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            log.error("Error writing JSON response", e);
            body = "{\"code\":401,\"message\":\"Unauthenticated\"}";
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}