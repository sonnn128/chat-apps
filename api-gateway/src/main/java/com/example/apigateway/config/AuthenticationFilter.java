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

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final IdentityService identityService;
    private final ObjectMapper objectMapper;

    // Danh sách các API REST công khai, không yêu cầu xác thực.
    private final List<String> publicApiPaths = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/introspect"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // BƯỚC 1: BỎ QUA HOÀN TOÀN CÁC REQUEST WEBSOCKET
        // Việc định tuyến và xử lý sẽ do các route trong application.yml đảm nhiệm.
        // Việc bảo mật cho WebSocket (nếu cần) sẽ được xử lý ở một lớp khác.
        if (path.startsWith("/ws")) {
            log.trace("WebSocket request detected, skipping authentication filter for path: {}", path);
            return chain.filter(exchange);
        }

        // BƯỚC 2: KIỂM TRA CÁC API REST PUBLIC
        if (isPublicApi(path)) {
            log.trace("Public API request detected, skipping authentication filter for path: {}", path);
            return chain.filter(exchange);
        }

        // BƯỚC 3: XÁC THỰC TẤT CẢ CÁC API CÒN LẠI

        // 3a. Kiểm tra sự tồn tại của header Authorization
        List<String> authHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeaders)) {
            log.warn("Unauthenticated request to '{}': missing Authorization header", path);
            return unauthenticated(exchange.getResponse());
        }

        // 3b. Kiểm tra định dạng 'Bearer '
        String authHeader = authHeaders.get(0);
        if (!authHeader.startsWith("Bearer ")) {
            log.warn("Unauthenticated request to '{}': Authorization header is not a Bearer token", path);
            return unauthenticated(exchange.getResponse());
        }

        String token = authHeader.substring(7);

        // 3c. Gọi đến Identity Service để xác thực token
        return identityService.introspect(token)
                .flatMap(introspectResponse -> {
                    // 3d. Xử lý kết quả từ Identity Service
                    if (introspectResponse == null || !introspectResponse.active()) {
                        log.warn("Token is invalid or inactive for request to '{}'", path);
                        return unauthenticated(exchange.getResponse());
                    }

                    // 3e. Gắn thông tin user vào header và chuyển tiếp request
                    ServerHttpRequest newRequest = exchange.getRequest().mutate()
                            .header("X-Authenticated-User-Username", introspectResponse.username())
                            .header("X-Authenticated-User-Roles", String.join(",", introspectResponse.roles()))
                            .header("X-Authenticated-User-Id", introspectResponse.userId().toString())
                            .build();

                    log.info("Authenticated user '{}', forwarding request to service for path '{}'", introspectResponse.username(), path);
                    return chain.filter(exchange.mutate().request(newRequest).build());
                });
    }

    /**
     * Đặt độ ưu tiên cho filter này.
     * Giá trị âm có độ ưu tiên cao hơn. -1 đảm bảo nó chạy trước hầu hết các filter khác.
     */
    @Override
    public int getOrder() {
        return -1;
    }

    /**
     * Kiểm tra xem một đường dẫn có nằm trong danh sách các API public hay không.
     * @param path Đường dẫn của request.
     * @return true nếu là API public, ngược lại là false.
     */
    private boolean isPublicApi(String path) {
        return publicApiPaths.contains(path);
    }

    /**
     * Xây dựng và trả về một response lỗi 401 Unauthorized.
     * @param response Đối tượng ServerHttpResponse.
     * @return Mono<Void> để kết thúc chuỗi xử lý.
     */
    private Mono<Void> unauthenticated(ServerHttpResponse response) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .message("Unauthenticated")
                .build();

        byte[] body;
        try {
            body = objectMapper.writeValueAsBytes(apiResponse);
        } catch (JsonProcessingException e) {
            log.error("Error writing JSON response", e);
            body = "{\"code\":401,\"message\":\"Unauthenticated\"}".getBytes();
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }
}